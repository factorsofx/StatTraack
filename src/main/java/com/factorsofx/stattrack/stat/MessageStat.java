package com.factorsofx.stattrack.stat;

import net.dv8tion.jda.core.entities.Message;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.OffsetDateTime;

public class MessageStat
{
    @BsonId
    private long msgId;
    private long senderId;
    private long channelId;
    private long guildId;
    private OffsetDateTime time;

    public MessageStat() {}

    public MessageStat(long msgId, long senderId, long channelId, long guildId, OffsetDateTime time)
    {
        this.msgId = msgId;
        this.senderId = senderId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.time = time;
    }

    public static MessageStat fromMessage(Message msg)
    {
        return new MessageStat(
                msg.getIdLong(),
                msg.getAuthor().getIdLong(),
                msg.getChannel().getIdLong(),
                msg.getGuild().getIdLong(),
                msg.getCreationTime());
    }

    public long getMsgId()
    {
        return msgId;
    }

    public long getSenderId()
    {
        return senderId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public long getGuildId()
    {
        return guildId;
    }

    public OffsetDateTime getTime()
    {
        return time;
    }
}
