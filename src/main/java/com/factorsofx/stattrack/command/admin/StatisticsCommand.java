package com.factorsofx.stattrack.command.admin;

import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.Instant;

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
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Database Statistics");
        builder.setTimestamp(Instant.now());
        builder.setColor(new Color(36, 113, 163));

        builder.addField("Messages", Long.toString(persistenceService.messagesStored()), true);
        builder.addField("Opted-in Users", Integer.toString(persistenceService.getOptedInUsers().size()), true);

        channel.sendMessage(builder.build()).complete();
    }
}
