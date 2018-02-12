package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.MessageUtils;
import com.factorsofx.stattrack.StatTrack;
import com.factorsofx.stattrack.stat.AdvStatReq;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.cli.*;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterCommand(value = {"astats", "advancedstats"})
public class AdvancedStatsCommand implements BotCommand
{
    private static Options options = new Options();
    {
        options.addOption("?", "Shows this help message");

        options.addOption("S", "save-as", true, "Saves the parameters as a name. Does not generate a chart.");
        options.addOption("R", "recall-from", true, "Loads parameters from the given save. Ignores all other options.");
        options.addOption("L", "list-saved", false, "Lists saved searches");

        options.addOption("c", "channel", true, "Channel(s) to query");
        options.addOption("u", "users", true, "User(s) to query");

        OptionGroup modeGroup = new OptionGroup();
        modeGroup.addOption(Option.builder("l").longOpt("line-chart").hasArg(false).desc("Generates a stacked line chart of message counts").build());
        modeGroup.addOption(Option.builder("p").longOpt("pie-chart").hasArg(false).desc("Generates a pie chart of message counts").build());
        modeGroup.addOption(Option.builder("h").longOpt("histogram").hasArg(false).desc("Generates a histogram of message counts").build());

        options.addOptionGroup(modeGroup);

        options.addOption("d", "display", true, "What to display the stats of. Valid options: USERS, CHANNELS");

        options.addOption(Option.builder().longOpt("include-penalties").desc("Includes leaderboard penalties in stat tracking.").build());

        options.addOption("b", "before", true, "Gets only messages before the given date");
        options.addOption("a", "after", true, "Gets only messages after the given date");
        options.addOption("Z", "zero", false, "Starts every message count at 0 at the beginning of the date range");
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<String> argsList = MessageUtils.getStringParsedArgs(args);

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine line = parser.parse(options, argsList.toArray(new String[]{}));

            if(line.hasOption('?'))
            {
                HelpFormatter formatter = new HelpFormatter();
                StringBuilder msgBuilder = new StringBuilder();
                formatter.printHelp(new PrintWriter(new StringBuilderWriter(msgBuilder)), 60, StatTrack.CMD_PREFIX + "advancedstats [options]", "Used to generate charts not possible with other stat commands", options, 0, 4, "");
                channel.sendMessage("```" + msgBuilder.toString() + "```").complete();
                return;
            }

            AdvStatReq req = new AdvStatReq();

            req.setGuild(channel.getGuild().getIdLong());

            if(line.hasOption('S') || line.hasOption('R') || line.hasOption('L'))
            {
                channel.sendMessage(MessageUtils.getErrorEmbed("Query saving not yet implemented")).complete();
                return;
            }

            if(line.hasOption('l'))
            {
                req.setMode(AdvStatReq.Mode.STACKED_AREA);
            }
            else if(line.hasOption('p'))
            {
                req.setMode(AdvStatReq.Mode.PIE_CHART);
            }
            else if(line.hasOption('h'))
            {
                req.setMode(AdvStatReq.Mode.HISTOGRAM);
            }

            if(line.hasOption('d'))
            {
                req.setTarget(AdvStatReq.StatTarget.valueOf(line.getOptionValue('d')));
            }

            if(line.hasOption('b'))
            {
                req.setBefore(MessageUtils.getDateFromTime(line.getOptionValue('b')));
            }

            if(line.hasOption('a'))
            {
                req.setAfter(MessageUtils.getDateFromTime(line.getOptionValue('a')));
            }

            if(line.hasOption('c'))
            {
                TLongSet channelSet = new TLongHashSet();

            }

            channel.sendMessage(MessageUtils.getObjectEmbed(req, Color.YELLOW)).complete();
        }
        catch(ParseException e)
        {
            channel.sendMessage(MessageUtils.getExceptionEmbed(e)).complete();
        }
    }
}
