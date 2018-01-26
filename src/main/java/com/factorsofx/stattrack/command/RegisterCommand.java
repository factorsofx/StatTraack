package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.security.Permission;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterCommand
{
    String[] value();

    Permission[] permissions() default {};
}
