package com.factorsofx.stattrack.stat;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.OffsetDateTime;

public class MessageStat
{
    @BsonId
    private long messageId;
    private String hashedSenderId;
    private long channelId;
    private OffsetDateTime time;

    public MessageStat() {} // FUCK YOU BSON

    private MessageStat(long messageId, String hashedSenderId, long channelId, OffsetDateTime time)
    {
        Preconditions.checkNotNull(hashedSenderId);
        Preconditions.checkNotNull(time);

        this.messageId = messageId;
        this.hashedSenderId = hashedSenderId;
        this.channelId = channelId;
        this.time = time;
    }

    public long getMessageId()
    {
        return messageId;
    }

    public String getHashedSenderId()
    {
        return hashedSenderId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public OffsetDateTime getTime()
    {
        return time;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        MessageStat that = (MessageStat) o;

        return channelId == that.channelId && hashedSenderId.equals(that.hashedSenderId) && time.equals(that.time);
    }

    @Override
    public int hashCode()
    {
        int result = hashedSenderId.hashCode();
        result = 31 * result + (int) (channelId ^ (channelId >>> 32));
        result = 31 * result + time.hashCode();
        return result;
    }

    public static MessageStat from(Message message)
    {
        return new MessageStat(
                message.getIdLong(),
                DigestUtils.sha256Hex(Longs.toByteArray(message.getAuthor().getIdLong())),
                message.getChannel().getIdLong(),
                message.getCreationTime());
    }
}
