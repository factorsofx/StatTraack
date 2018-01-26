package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.stat.MessageStat;
import com.mongodb.MongoBulkWriteException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

class MessageUpdater
{
    private final OffsetDateTime startupTime;
    private PersistenceService persistenceService;
    private JDA jda;

    private static final Logger log = LoggerFactory.getLogger(MessageUpdater.class);

    MessageUpdater(PersistenceService persistenceService, JDA jda, OffsetDateTime startupTime)
    {
        this.persistenceService = persistenceService;
        this.jda = jda;
        this.startupTime = startupTime;
    }

    void update()
    {
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        // Either last message date or discord founding date.
        MessageStat lastStat = persistenceService.getLatestMessage();
        OffsetDateTime last = lastStat != null ? lastStat.getTime() : OffsetDateTime.of(2015, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        for(TextChannel channel : jda.getTextChannels())
        {
            log.info("Starting back-traversal of channel {}:{} to {}.", channel.getGuild().getName(), channel.getName(), last);
            try
            {
                MessageHistory traversal = channel.getHistory();
                List<Message> messages = traversal.retrievePast(100).complete();
                messages = messages.stream().filter((msg) -> msg.getCreationTime().isBefore(startupTime)).filter((msg) -> msg.getCreationTime().isAfter(last)).collect(Collectors.toList());
                while(!messages.isEmpty())
                {
                    persistenceService.persistMessages(messages);
                    messages = traversal.retrievePast(100).complete();
                    messages = messages.stream().filter((msg) -> msg.getCreationTime().isBefore(startupTime)).filter((msg) -> msg.getCreationTime().isAfter(last)).collect(Collectors.toList());
                }
            }
            catch(InsufficientPermissionException e)
            {
                log.warn("No history permissions in {}:{}, skipping.", channel.getGuild().getName(), channel.getName());
            }
            catch(MongoBulkWriteException e)
            {
                log.error("Bulk write error saving messages from back-traversal", e);
            }
        }
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
    }
}
