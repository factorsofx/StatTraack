package com.factorsofx.stattrack;

import com.factorsofx.stattrack.security.Permission;

public @interface RegisterCommand
{
    String[] name();

    Permission[] required() default {};
}
