package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.MongoPersistenceService;
import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;

import javax.security.auth.login.LoginException;
import java.time.OffsetDateTime;
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

        PersistenceService persistenceService = new MongoPersistenceService(MONGO_HOST, Integer.parseInt(MONGO_PORT), DB_NAME);

        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(BOT_TOKEN)
                .setEventManager(new AnnotatedEventManager())
                .buildBlocking();
        OffsetDateTime startTime = OffsetDateTime.now();

        jda.addEventListener(new StatTrackListener(persistenceService, startTime));
        jda.addEventListener(new CommandListener(persistenceService, CMD_PREFIX));
        jda.addEventListener(new ConnectionStatusListener(persistenceService));

        new MessageUpdater(persistenceService, jda, startTime).update();
    }
}
