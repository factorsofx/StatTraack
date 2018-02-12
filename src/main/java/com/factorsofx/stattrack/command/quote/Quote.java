package com.factorsofx.stattrack.command.quote;

import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.List;

@RegisterCommand(value = "quote", optExclusive = false)
public class Quote implements BotCommand
{
    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<Message> msgs = channel.getHistory().retrievePast(2).complete();

        Message toQuote = msgs.get(1);

        channel.sendMessage(new EmbedBuilder()
                .setDescription(toQuote.getContentRaw())
                .setTimestamp(Instant.now())
                .setColor(Color.decode("0x114499"))
                .setAuthor(toQuote.getAuthor().getName())
                .setThumbnail(toQuote.getAuthor().getEffectiveAvatarUrl())
                .build()).complete();
    }
}
