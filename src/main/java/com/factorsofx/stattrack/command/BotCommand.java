package com.factorsofx.stattrack.command;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public interface BotCommand
{
    void execute(User user, TextChannel channel, Message message, String[] args);
}
