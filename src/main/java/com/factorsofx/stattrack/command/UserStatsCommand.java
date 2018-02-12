package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.stat.MessageStat;
import com.factorsofx.stattrack.stat.OptedInUser;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RegisterCommand(value = "userstats", permissions = Permission.VISUALS)
public class UserStatsCommand implements BotCommand
{
    private PersistenceService persistenceService;

    public UserStatsCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        channel.sendMessage("Compiling frequency chart for all users in guild...").complete();

        List<MessageStat> messages = persistenceService.getGuildStats(Collections.singletonList(message.getGuild()));
        Set<OptedInUser> users = persistenceService.getOptedInUsers();

        THashMap<String, OptedInUser> idLookup = new THashMap<>();
        users.forEach((oiu) -> idLookup.put(oiu.getHashedId(), oiu));

        TLongIntMap countMap = new TLongIntHashMap();
        for(MessageStat stat : messages)
        {
            if(idLookup.containsKey(stat.getHashedSenderId()))
            {
                countMap.adjustOrPutValue(idLookup.get(stat.getHashedSenderId()).getId(), 1, 1);
            }
            else
            {
                countMap.adjustOrPutValue(0L, 1, 1);
            }
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        for(long userId : countMap.keys())
        {
            if(userId != 0)
            {
                dataset.setValue(message.getJDA().getUserById(userId).getName(), countMap.get(userId));
            }
        }
        dataset.sortByValues(SortOrder.DESCENDING);

        dataset.insertValue(0, "Unknown", countMap.get(0));

        JFreeChart chart = ChartFactory.createRingChart("User activity", dataset, true, false, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ChartUtils.writeChartAsPNG(baos, chart, 600, 400);
            channel.sendFile(baos.toByteArray(), "histo.png").complete();
        }
        catch(IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
