package com.factorsofx.stattrack.command.admin;

import com.factorsofx.stattrack.MessageUtils;
import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.awt.*;

@RegisterCommand(value = "forceopt", permissions = Permission.ADMINISTRATE, optExclusive = false)
public class ForceOpt implements BotCommand
{
    private PersistenceService persistenceService;

    public ForceOpt(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        if(args.length != 1)
        {
            channel.sendMessage("Bad args").complete();
            return;
        }
        try
        {
            long id = Long.parseLong(args[0]);

            User target = message.getJDA().getUserById(id);
            if(target == null)
            {
                channel.sendMessage("Could not find target").complete();
                return;
            }

            MessageUtils.doIfConfirmed("Are you sure?", channel, user, () ->
            {
                persistenceService.optInUser(target);
                channel.sendMessage("User " + user.getName() + " force-opted in.").complete();
            });
        }
        catch(NumberFormatException e)
        {
            channel.sendMessage("Bad long format").complete();
        }
    }
}
