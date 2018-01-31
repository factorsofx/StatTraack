package com.factorsofx.stattrack.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserProfile
{
    private long userId;
    private long guildId;
    private List<Permission> perms;

    public UserProfile() {} // God DAMN it BSON

    public UserProfile(long userId, long guildId, List<Permission> perms)
    {
        this.userId = userId;
        this.guildId = guildId;
        this.perms = perms;
    }

    public long getUserId()
    {
        return userId;
    }

    public long getGuildId()
    {
        return guildId;
    }

    public List<Permission> getPerms()
    {
        return Collections.unmodifiableList(perms);
    }
}
