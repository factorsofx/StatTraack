package com.factorsofx.stattrack.persist;

import com.factorsofx.stattrack.stat.MessageStat;
import com.factorsofx.stattrack.stat.OptedInUser;
import net.dv8tion.jda.core.entities.*;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PersistenceService
{
    List<MessageStat> getGuildStats(List<Guild> guilds);

    List<MessageStat> getChannelStats(List<TextChannel> channels);

    List<MessageStat> getTimeLimitedChannelStats(List<TextChannel> channels, OffsetDateTime beginning, OffsetDateTime end);

    List<MessageStat> getTimeLimitedUserStats(User user, Guild guild, OffsetDateTime beginning, OffsetDateTime end);

    List<MessageStat> getTimeLimitedGuildStats(List<Guild> guilds, OffsetDateTime beginning, OffsetDateTime end);

    List<MessageStat> getUserStats(User user, Guild guild);

    Set<OptedInUser> getOptedInUsers();

    MessageStat getLatestMessage();

    void persistMessage(Message message);

    void persistMessages(Collection<Message> messages);

    long messagesStored();

    long usersStored();

    void optInUser(User user);

    void optOutUser(User user);
}
