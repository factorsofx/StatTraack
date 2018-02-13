package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.MessageUtils;
import com.factorsofx.stattrack.persist.MessageStatStore;
import com.factorsofx.stattrack.persist.PenaltyStore;
import com.factorsofx.stattrack.stat.MessageStat;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

@RegisterCommand(value = "leaderboard")
public class LeaderboardCommand implements BotCommand
{
    private MessageStatStore messageStatStore;
    private PenaltyStore penaltyStore;

    private static Options options = new Options();
    static
    {
        options.addOption("l", "limit", true, "Limits the amount of results.");
        options.addOption(Option.builder().longOpt("ignore-penalties").desc("Ignores user penalties").hasArg(false).build());
    }

    public LeaderboardCommand(MessageStatStore messageStatStore, PenaltyStore penaltyStore)
    {
        this.messageStatStore = messageStatStore;
        this.penaltyStore = penaltyStore;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<String> argsList = MessageUtils.getStringParsedArgs(args);
        CommandLineParser parser = new DefaultParser();

        CommandLine line;

        try
        {
            line = parser.parse(options, argsList.toArray(new String[]{}));
        }
        catch(ParseException e)
        {
            throw new RuntimeException(e);
        }

        Collection<MessageStat> guildMsgs = messageStatStore.getAllInGuild(channel.getGuild());

        TLongIntMap scoreMap = new TLongIntHashMap();

        for(MessageStat stat : guildMsgs)
        {
            scoreMap.adjustOrPutValue(stat.getSenderId(), 1, 1);
        }

        if(!line.hasOption("ignore-penalties"))
        {
            for(long id : scoreMap.keys())
            {
                scoreMap.adjustValue(id, -penaltyStore.getAccumPenaltyForUser(id));
            }
        }

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("```");

        List<Tuple2<Long, Integer>> pairs = new ArrayList<>();
        Arrays.stream(scoreMap.keys()).mapToObj((k) -> Tuple.tuple(k, scoreMap.get(k))).forEach(pairs::add);

        pairs.sort(Collections.reverseOrder(Comparator.comparing(Tuple2::v2)));

        int limit = Integer.MAX_VALUE;
        if(line.hasOption('l'))
        {
            limit = Integer.parseInt(line.getOptionValue('l'));
        }
        if(limit > pairs.size())
        {
            limit = pairs.size();
        }

        for(int i = 0; i < limit; i++)
        {
            msgBuilder
                    .append(StringUtils.rightPad(Integer.toString(i + 1) + ": ", (int)Math.log10(limit) + 3))
                    .append(Optional.ofNullable(channel.getJDA().getUserById(pairs.get(i).v1)).map(User::getName).orElse("Unknown"))
                    .append(" - ")
                    .append(pairs.get(i).v2)
                    .append('\n');
        }

        msgBuilder.append("```");

        channel.sendMessage(new EmbedBuilder()
                .setTitle("Leaderboard")
                .setDescription(msgBuilder.toString())
                .setTimestamp(Instant.now())
                .setColor(new Color(41, 128, 185))
                .build()).complete();
    }
}
