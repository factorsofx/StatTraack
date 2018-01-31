package com.factorsofx.stattrack.command.admin;

import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

@RegisterCommand(value = "stats", optExclusive = false)
public class StatisticsCommand implements BotCommand
{
    private PersistenceService persistenceService;

    public StatisticsCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        String msgBuilder = "Database statistics:```\n" +
                " - Messages persisted: " + persistenceService.messagesStored() + '\n' +
                " - Users opted in: " + persistenceService.usersStored() +
                "```";

        channel.sendMessage(msgBuilder).complete();
    }
}
