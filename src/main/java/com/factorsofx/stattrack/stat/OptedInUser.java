package com.factorsofx.stattrack.stat;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class OptedInUser
{
    @BsonId
    private long id;
    private String hashedId;

    public OptedInUser() {} // FUCK YOU BSON

    private OptedInUser(long id, String hashedId)
    {
        Preconditions.checkNotNull(hashedId);

        this.id = id;
        this.hashedId = hashedId;
    }

    public long getId()
    {
        return id;
    }

    public String getHashedId()
    {
        return hashedId;
    }

    public static OptedInUser fromUser(User user)
    {
        return new OptedInUser(user.getIdLong(), DigestUtils.sha256Hex(Longs.toByteArray(user.getIdLong())));
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        OptedInUser that = (OptedInUser) o;

        if(id != that.id) return false;
        return hashedId.equals(that.hashedId);
    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + hashedId.hashCode();
        return result;
    }
}
