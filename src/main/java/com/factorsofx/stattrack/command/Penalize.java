package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.MessageUtils;
import com.factorsofx.stattrack.penalty.Penalty;
import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.joestelmach.natty.Parser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RegisterCommand(value = "penalize", permissions = Permission.PENALIZE, optExclusive = false)
public class Penalize implements BotCommand
{
    private static Options options = new Options();
    static
    {
        options.addOption("e", "expiration-date", true, "When the penalty expires");
        options.addOption("r", "reason", true, "Reason for penalty");
    }

    private PersistenceService persistenceService;

    public Penalize(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<String> argsList = MessageUtils.getStringParsedArgs(args);

        DefaultParser parser =  new DefaultParser();

        try
        {
            CommandLine line = parser.parse(options, argsList.toArray(new String[]{}), true);

            if(line.getArgList().size() != 2)
            {
                channel.sendMessage(MessageUtils.getErrorEmbed("Invalid args.")).complete();
                return;
            }

            Penalty penalty = new Penalty();

            penalty.setIssuer(user.getIdLong());
            penalty.setIssued(OffsetDateTime.now());
            if(line.hasOption('e'))
            {
                penalty.setExpires(MessageUtils.getDateFromTime(line.getOptionValue('e')));
            }
            if(line.hasOption('r'))
            {
                penalty.setReason(line.getOptionValue('r'));
            }

            try
            {
                penalty.setSufferer(MessageUtils.getIdFromUserMention(line.getArgList().get(0)));
            }
            catch(IllegalArgumentException e)
            {
                List<User> possibleUsers = channel.getJDA().getUsersByName(line.getArgList().get(0), true);
                if(possibleUsers.isEmpty())
                {
                    channel.sendMessage(MessageUtils.getErrorEmbed("User not found")).complete();
                    return;
                }
                if(possibleUsers.size() > 1)
                {
                    channel.sendMessage(MessageUtils.getErrorEmbed("Ambiguous username")).complete();
                    return;
                }
                penalty.setSufferer(possibleUsers.get(0).getIdLong());
            }

            penalty.setValue(Integer.parseInt(line.getArgList().get(1)));

            MessageUtils.doIfConfirmed("Are you sure you wish to issue a penalty?", channel, user, () ->
            {
                persistenceService.persistPenalty(penalty);
                channel.sendMessage(new EmbedBuilder()
                        .setTitle("Penalty Issued")
                        .addField("Reason", penalty.getReason(), false)
                        .addField("Issued By", user.getName(), true)
                        .addField("Issued To", channel.getJDA().getUserById(penalty.getSufferer()).getName(), true)
                        .setColor(new Color(220, 118, 51))
                        .setTimestamp(Instant.now())
                        .build())
                        .complete();
            });
        }
        catch(ParseException e)
        {
            throw new RuntimeException(e);
        }
    }
}
