package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

@RegisterCommand(value = "optout", optExclusive = false)
public class OptOut implements BotCommand
{
    private PersistenceService persistenceService;

    public OptOut(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        if(persistenceService.getOptedInUsers().stream().anyMatch((optedInUser) -> optedInUser.getId() == user.getIdLong()))
        {
            persistenceService.optOutUser(user);
            channel.sendMessage(user.getAsMention() + ", you are now opted out.").complete();
        }
        else
        {
            channel.sendMessage(user.getAsMention() + ", you were already opted out.").complete();
        }
    }
}
