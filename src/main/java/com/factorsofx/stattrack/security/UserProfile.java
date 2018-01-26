package com.factorsofx.stattrack.security;

import java.util.Arrays;

public class UserProfile
{
    private long userId;
    private Permission[] perms;

    public UserProfile(long userId, Permission[] perms)
    {
        this.userId = userId;
        this.perms = perms;
    }

    public long getUserId()
    {
        return userId;
    }

    public Permission[] getPerms()
    {
        return Arrays.copyOf(perms, perms.length);
    }
}
