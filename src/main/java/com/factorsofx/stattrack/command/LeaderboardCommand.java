package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.stat.MessageStat;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.List;

public class LeaderboardCommand implements BotCommand
{
    private PersistenceService persistenceService;

    public LeaderboardCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<MessageStat> guildMsgs = persistenceService.getGuildStats(Collections.singletonList(message.getGuild()));
    }
}
