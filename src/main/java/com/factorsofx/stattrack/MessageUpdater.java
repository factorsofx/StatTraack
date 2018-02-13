package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.MessageStatStore;
import com.factorsofx.stattrack.stat.MessageStat;
import com.mongodb.MongoBulkWriteException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

class MessageUpdater
{
    private final OffsetDateTime startupTime;
    private MessageStatStore messageStatStore;
    private JDA jda;

    private static final Logger log = LoggerFactory.getLogger(MessageUpdater.class);

    MessageUpdater(MessageStatStore messageStatStore, JDA jda, OffsetDateTime startupTime)
    {
        this.messageStatStore = messageStatStore;
        this.jda = jda;
        this.startupTime = startupTime;
    }

    void update()
    {
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        // Either last message date or discord founding date.
        MessageStat lastStat = messageStatStore.getLatestMessage();
        OffsetDateTime last = lastStat != null ? lastStat.getTime() : OffsetDateTime.of(2015, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        int total = 0;

        for(TextChannel channel : jda.getTextChannels())
        {
            log.info("Starting readthrough of channel {}:{} to {}.", channel.getGuild().getName(), channel.getName(), last);
            try
            {
                MessageHistory traversal = channel.getHistory();
                List<Message> messages = traversal.retrievePast(100).complete();
                messages = messages.stream().filter((msg) -> msg.getCreationTime().isBefore(startupTime)).filter((msg) -> msg.getCreationTime().isAfter(last)).collect(Collectors.toList());
                while(!messages.isEmpty())
                {
                    messages.stream().map(MessageStat::fromMessage).forEach(messageStatStore::store);
                    total += messages.size();
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
                log.error("Bulk write error saving messages from readthrough", e);
            }
        }
        log.info("Completed channel readthrough, found " + total + " new messages.");
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
    }
}
