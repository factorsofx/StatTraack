package com.factorsofx.stattrack.persist.mongo;

import com.factorsofx.stattrack.persist.MessageStatStore;
import com.factorsofx.stattrack.stat.MessageStat;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import gnu.trove.set.hash.THashSet;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;
import java.util.Collection;

import static com.mongodb.client.model.Filters.*;

public class MongoMessageStatStore extends AbstractMongoDataStore<MessageStat> implements MessageStatStore
{
    public MongoMessageStatStore(MongoDatabase db, String collectionName)
    {
        super(db, collectionName, MessageStat.class);
    }

    @Override
    public Collection<MessageStat> getAllInGuild(Guild guild)
    {
        return collection.find(eq("guildId", guild.getIdLong())).into(new THashSet<>());
    }

    @Override
    public Collection<MessageStat> getAllBetweenDates(Guild guild, OffsetDateTime start, OffsetDateTime end)
    {
        return collection.find(and(eq("guildId", guild.getIdLong()), lte("time", end), gte("time", start))).into(new THashSet<>());
    }

    @Override
    public Collection<MessageStat> getInChannel(Guild guild, Channel channel)
    {
        return collection.find(and(eq("guildId", guild.getIdLong()), eq("channelId", channel.getIdLong()))).into(new THashSet<>());
    }

    @Override
    public Collection<MessageStat> getInChannelBetweenDates(Guild guild, Channel channel, OffsetDateTime start, OffsetDateTime end)
    {
        return collection.find(and(
                eq("guildId", guild.getIdLong()),
                eq("channelId", channel.getIdLong()),
                lte("time", end),
                gte("time", start)))
                .into(new THashSet<>());
    }

    @Override
    public Collection<MessageStat> getFromUser(Guild guild, User user)
    {
        return collection.find(and(
                eq("guildId", guild.getIdLong()),
                eq("userId", user.getIdLong())))
                .into(new THashSet<>());
    }

    @Override
    public Collection<MessageStat> getFromUserBetweenDates(Guild guild, User user, OffsetDateTime start, OffsetDateTime end)
    {
        return collection.find(and(
                eq("guildId", guild.getIdLong()),
                eq("userId", user.getIdLong()),
                lte("time", end),
                gte("time", start)))
                .into(new THashSet<>());
    }

    @Override
    public MessageStat getLatestMessage()
    {
        return collection.find().sort(Sorts.descending("time")).first();
    }
}
