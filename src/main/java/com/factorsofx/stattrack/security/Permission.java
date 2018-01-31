package com.factorsofx.stattrack.security;

public enum Permission
{
    VISUALS,
    ADMINISTRATE,
    BANNED; // Not really a permission but it works?

    public static final Permission[] DEFAULT_PERMS = new Permission[]{VISUALS};
}
