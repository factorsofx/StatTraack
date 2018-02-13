package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.MessageStatStore;
import com.factorsofx.stattrack.persist.PenaltyStore;
import com.factorsofx.stattrack.persist.UserProfileStore;
import com.factorsofx.stattrack.persist.mongo.MongoMessageStatStore;
import com.factorsofx.stattrack.persist.mongo.MongoPenaltyStore;
import com.factorsofx.stattrack.persist.mongo.MongoUserProfileStore;
import com.factorsofx.stattrack.persist.mongo.provider.EnumCodecProvider;
import com.factorsofx.stattrack.persist.mongo.provider.OffsetDateTimeCodecProvider;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.security.auth.login.LoginException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatTrack
{
    public static String CMD_PREFIX;

    public static void main(String... args) throws LoginException, InterruptedException
    {
        final String BOT_TOKEN  = Optional.ofNullable(System.getenv("BOT_TOKEN")).orElseThrow(() -> new IllegalArgumentException("Must have bot token"));
        final String CMD_PREFIX = Optional.ofNullable(System.getenv("CMD_PREFIX")).orElse("::");
        StatTrack.CMD_PREFIX = CMD_PREFIX;

        final String MONGO_HOST = Optional.ofNullable(System.getenv("MONGO_HOST")).orElse("localhost");
        final String MONGO_PORT = Optional.ofNullable(System.getenv("MONGO_PORT")).orElse("27017");
        final String DB_NAME    = Optional.ofNullable(System.getenv("DB_NAME")).orElse("StatTrack");

        List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
        conventions.add(0, Conventions.SET_PRIVATE_FIELDS_CONVENTION);

        CodecRegistry registry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new OffsetDateTimeCodecProvider.OffsetDateTimeCodec<>()),
                CodecRegistries.fromProviders(new EnumCodecProvider(), PojoCodecProvider.builder().conventions(conventions).automatic(true).build()));
        MongoClient client = new MongoClient(new ServerAddress(MONGO_HOST, Integer.parseInt(MONGO_PORT)), MongoClientOptions.builder().codecRegistry(registry).build());
        MongoDatabase db = client.getDatabase(DB_NAME);

        ClassToInstanceMap<Object> dependencies = ImmutableClassToInstanceMap.builder()
                .put(MessageStatStore.class, new MongoMessageStatStore(db, "msgStats"))
                .put(UserProfileStore.class, new MongoUserProfileStore(db, "profiles"))
                .put(PenaltyStore.class, new MongoPenaltyStore(db, "penalties"))
                .build();

        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(BOT_TOKEN)
                .setEventManager(new AnnotatedEventManager())
                .buildBlocking();
        OffsetDateTime startTime = OffsetDateTime.now();

        jda.addEventListener(new StatTrackListener(dependencies, startTime));
        jda.addEventListener(new CommandListener(dependencies, CMD_PREFIX));
        jda.addEventListener(new ConnectionStatusListener(dependencies));

        new MessageUpdater(dependencies.getInstance(MessageStatStore.class), jda, startTime).update();
    }
}
