package com.factorsofx.stattrack.persist;

import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.security.UserProfile;
import com.factorsofx.stattrack.stat.MessageStat;
import com.factorsofx.stattrack.stat.OptedInUser;
import com.google.common.primitives.Longs;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import gnu.trove.set.hash.THashSet;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.Code;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

public class MongoPersistenceService implements PersistenceService
{
    private MongoCollection<MessageStat> statCollection;
    private MongoCollection<OptedInUser> userCollection;
    private MongoCollection<UserProfile> profileCollection;

    public MongoPersistenceService(String host, int port, String dbName)
    {
        List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
        conventions.add(0, Conventions.SET_PRIVATE_FIELDS_CONVENTION);

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new OffsetDateTimeCodecProvider.OffsetDateTimeCodec<>()), CodecRegistries.fromProviders(new EnumCodecProvider(), PojoCodecProvider.builder().conventions(conventions).automatic(true).build()));

        MongoClient client = new MongoClient(new ServerAddress(host, port), MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        MongoDatabase database = client.getDatabase(dbName);

        statCollection    = database.getCollection("msgStats", MessageStat.class);
        userCollection    = database.getCollection("users", OptedInUser.class);
        profileCollection = database.getCollection("profiles", UserProfile.class);
    }

    @Override
    public List<MessageStat> getGuildStats(List<Guild> guilds)
    {
        return getChannelStats(guilds.stream().map(Guild::getTextChannels).flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public List<MessageStat> getChannelStats(List<TextChannel> channels)
    {
        return getMessageSetFromAnyMatch("channelId", channels, TextChannel::getIdLong);
    }

    @Override
    public List<MessageStat> getTimeLimitedChannelStats(List<TextChannel> channels, OffsetDateTime beginning, OffsetDateTime end)
    {
        return getTimeLimitedMessageSetFromAnyMatch("channelId", channels, TextChannel::getIdLong, beginning, end);
    }

    @Override
    public List<MessageStat> getTimeLimitedUserStats(User user, Guild guild, OffsetDateTime beginning, OffsetDateTime end)
    {
        if(!isUserOptedIn(user)) return Collections.emptyList();

        List<MessageStat> result = new ArrayList<>();
        statCollection.find(and(
                eq("hashedSenderId", DigestUtils.sha256Hex(Longs.toByteArray(user.getIdLong()))),
                lt("time", end),
                gte("time", beginning)))
                .sort(Sorts.ascending("time"))
                .into(result);
        result.removeIf(messageStat -> guild.getTextChannelById(messageStat.getChannelId()) == null);
        return result;
    }

    @Override
    public List<MessageStat> getTimeLimitedGuildStats(List<Guild> guilds, OffsetDateTime beginning, OffsetDateTime end)
    {
        return getTimeLimitedChannelStats(guilds.stream().map(Guild::getTextChannels).flatMap(Collection::stream).collect(Collectors.toList()), beginning, end);
    }

    @Override
    public List<MessageStat> getUserStats(User user, Guild guild)
    {
        if(!isUserOptedIn(user)) return Collections.emptyList();

        List<MessageStat> result = new ArrayList<>();
        statCollection.find(eq("hashedSenderId", DigestUtils.sha256Hex(Longs.toByteArray(user.getIdLong()))))
                .sort(Sorts.ascending("time"))
                .into(result);
        result.removeIf(messageStat -> guild.getTextChannelById(messageStat.getChannelId()) == null);
        return result;
    }

    @Override
    public Set<OptedInUser> getOptedInUsers()
    {
        THashSet<OptedInUser> found = new THashSet<>();
        userCollection.find().into(found);
        return found;
    }

    @Override
    public OptedInUser findOptedInUserWithHashedId(String hashedId)
    {
        return userCollection.find(eq("hashedId", hashedId)).first();
    }

    @Override
    public UserProfile getUserProfile(User user, Guild guild)
    {
        return Optional.ofNullable(profileCollection.find(and(eq("userId", user.getIdLong()), eq("guildId", guild.getIdLong()))).first())
                .orElseGet(() ->
                {
                    UserProfile profile = new UserProfile(user.getIdLong(), guild.getIdLong(), Arrays.asList(Permission.DEFAULT_PERMS));
                    profileCollection.insertOne(profile);
                    return profile;
                });
    }

    private boolean isUserOptedIn(User user)
    {
        return getOptedInUsers().stream().anyMatch((usr) -> usr.getId() == user.getIdLong());
    }

    @Override
    public MessageStat getLatestMessage()
    {
        return statCollection.find().sort(Sorts.descending("time")).first();
    }

    private <T, K> List<MessageStat> getMessageSetFromAnyMatch(String field, Collection<T> matches, Function<T, K> toKeyFunc)
    {
        List<MessageStat> found = new ArrayList<>();
        List<Bson> eqFilters = matches.stream().map((obj) -> eq(field, toKeyFunc.apply(obj))).collect(Collectors.toList());
        Bson query = Filters.or(eqFilters);
        MongoIterable<MessageStat> result = statCollection.find(query).sort(Sorts.ascending("time"));
        result.into(found);
        return found;
    }

    private <T, K> List<MessageStat> getTimeLimitedMessageSetFromAnyMatch(String field, Collection<T> matches, Function<T, K> toKeyFunc, OffsetDateTime beginning, OffsetDateTime end)
    {
        List<Bson> eqFilters = matches.stream().map((obj) -> eq(field, toKeyFunc.apply(obj))).collect(Collectors.toList());
        Bson dateLimit = and(Filters.lte("time", end), Filters.gte("time", beginning));
        Bson query = and(dateLimit, Filters.or(eqFilters));
        MongoIterable<MessageStat> result = statCollection.find(query).sort(Sorts.ascending("time"));
        List<MessageStat> found = new ArrayList<>();
        result.into(found);
        return found;
    }

    @Override
    public void persistMessage(Message message)
    {
        statCollection.insertOne(MessageStat.from(message));
    }

    @Override
    public void persistMessages(Collection<Message> messages)
    {
        statCollection.insertMany(messages.stream().map(MessageStat::from).collect(Collectors.toList()));
    }

    @Override
    public long messagesStored()
    {
        return statCollection.count();
    }

    @Override
    public long usersStored()
    {
        return userCollection.count();
    }

    @Override
    public void optInUser(User user)
    {
        userCollection.insertOne(OptedInUser.fromUser(user));
    }

    @Override
    public void optOutUser(User user)
    {
        userCollection.deleteOne(eq("_id", user.getIdLong()));
    }
}
