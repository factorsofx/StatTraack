package com.factorsofx.stattrack.command.admin;

import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import com.factorsofx.stattrack.security.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

@RegisterCommand(value = "query", permissions = Permission.ADMINISTRATE, optExclusive = false)
public class QueryCommand implements BotCommand
{

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {

    }
}
