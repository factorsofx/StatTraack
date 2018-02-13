package com.factorsofx.stattrack.persist;

import com.factorsofx.stattrack.stat.MessageStat;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;
import java.util.Collection;

public interface MessageStatStore extends DataStore<MessageStat>
{
    Collection<MessageStat> getAllInGuild(Guild guild);

    Collection<MessageStat> getAllBetweenDates(Guild guild, OffsetDateTime start, OffsetDateTime end);

    Collection<MessageStat> getInChannel(Guild guild, Channel channel);

    Collection<MessageStat> getInChannelBetweenDates(Guild guild, Channel channel, OffsetDateTime start, OffsetDateTime end);

    Collection<MessageStat> getFromUser(Guild guild, User user);

    Collection<MessageStat> getFromUserBetweenDates(Guild guild, User user, OffsetDateTime start, OffsetDateTime end);

    MessageStat getLatestMessage();
}
