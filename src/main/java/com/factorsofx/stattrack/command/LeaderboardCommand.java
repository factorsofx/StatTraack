package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.stat.MessageStat;
import com.factorsofx.stattrack.stat.OptedInUser;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.jooq.lambda.tuple.Tuple;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RegisterCommand(value = "leaderboard")
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

        TObjectIntMap<String> hashedIdCounts = new TObjectIntHashMap<>();

        for(MessageStat stat : guildMsgs)
        {
            hashedIdCounts.adjustOrPutValue(stat.getHashedSenderId(), 1, 1);
        }

        String leaderboardMessage = "```\nLeaderboard\n" +
                hashedIdCounts.keySet().stream()
                        .sorted(Comparator.comparingInt(hashedIdCounts::get).reversed())
                        .map(str -> Tuple.tuple(str, hashedIdCounts.get(str)))
                        .limit(20)
                        .map((tuple) -> tuple.v2 + " - " + Optional.ofNullable(persistenceService.findOptedInUserWithHashedId(tuple.v1))
                                .map(OptedInUser::getId)
                                .map(message.getJDA()::getUserById)
                                .map(User::getName).orElse("Unknown"))
                        .collect(Collectors.joining("\n")) +
                "```";

        channel.sendMessage(leaderboardMessage).complete();
    }
}
