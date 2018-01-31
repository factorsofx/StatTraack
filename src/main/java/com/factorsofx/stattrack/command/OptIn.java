package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

@RegisterCommand(value = "optin", optExclusive = false)
public class OptIn implements BotCommand
{
    private PersistenceService persistenceService;

    public OptIn(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        if(persistenceService.getOptedInUsers().stream().noneMatch((optedInUser) -> optedInUser.getId() == user.getIdLong()))
        {
            persistenceService.optInUser(user);
            channel.sendMessage(user.getAsMention() + ", you are now opted in!").complete();
        }
        else
        {
            channel.sendMessage(user.getAsMention() + ", you were already opted in!").complete();
        }
    }
}
