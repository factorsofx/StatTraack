package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.stat.MessageStat;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RegisterCommand(value = "weekdayStats", permissions = Permission.VISUALS)
public class DaysOfWeekStats implements BotCommand
{
    private PersistenceService persistenceService;

    public DaysOfWeekStats(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        if(message.getMentionedChannels().size() <= 0)
        {
            channel.sendMessage("You have to mention at least one channels!").complete();
            return;
        }

        List<MessageStat> stats = persistenceService.getChannelStats(message.getMentionedChannels());

        TLongObjectMap<TObjectIntMap<DayOfWeek>> channelDayCountMap = new TLongObjectHashMap<>();

        for(MessageStat stat : stats)
        {
            if(!channelDayCountMap.containsKey(stat.getChannelId()))
            {
                channelDayCountMap.put(stat.getChannelId(), new TObjectIntHashMap<>());
            }
            channelDayCountMap.get(stat.getChannelId()).adjustOrPutValue(stat.getTime().getDayOfWeek(), 1, 1);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Arrays.stream(channelDayCountMap.keys()).mapToObj(message.getJDA()::getTextChannelById)
        .forEach((textChannel) ->
        {
            TObjectIntMap<DayOfWeek> dowMap = channelDayCountMap.get(textChannel.getIdLong());
            for(DayOfWeek day : DayOfWeek.values())
            {
                dataset.addValue(dowMap.get(day), textChannel.getName(), day.name());
            }
        });

        JFreeChart chart = ChartFactory.createBarChart("Messages in channel(s) on each day", "Weekday", "Messages", dataset);

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
