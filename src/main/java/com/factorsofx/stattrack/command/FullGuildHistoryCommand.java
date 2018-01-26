package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.stat.MessageStat;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.*;

public class FullGuildHistoryCommand implements BotCommand
{
    private PersistenceService persistenceService;

    public FullGuildHistoryCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        channel.sendMessage("Compiling full guild message history... (this may take a while)").complete();

        int bins = 128;

        List<MessageStat> guildMessages = persistenceService.getGuildStats(Collections.singletonList(message.getGuild()));

        TLongObjectMap<List<MessageStat>> channelMap = new TLongObjectHashMap<>();

        for(MessageStat stat : guildMessages)
        {
            if(!channelMap.containsKey(stat.getChannelId()))
            {
                channelMap.put(stat.getChannelId(), new ArrayList<>());
            }
            channelMap.get(stat.getChannelId()).add(stat);
        }

        DefaultTableXYDataset dataset = new DefaultTableXYDataset();

        OffsetDateTime first = guildMessages.get(0).getTime();
        OffsetDateTime last = guildMessages.get(guildMessages.size() - 1).getTime();

        List<XYSeries> seriesList = new ArrayList<>();

        for(long key : channelMap.keys())
        {
            XYSeries series = new XYSeries(message.getJDA().getTextChannelById(key).getName(), true, false);
            List<MessageStat> stats = channelMap.get(key);
            DatasetUtils.makeAccumPlot(stats, series, first, last, bins);

            seriesList.add(series);
        }

        seriesList.sort(Comparator.comparingDouble(XYSeries::getMaxY).reversed());
        seriesList.forEach(dataset::addSeries);

        XYPlot plot = new XYPlot(dataset, new DateAxis("Date"), new NumberAxis("Messages"), new StackedXYAreaRenderer2());
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        JFreeChart chart = new JFreeChart(message.getGuild().getName() + " message count", plot);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ChartUtils.writeChartAsPNG(baos, chart, 1200, 800);
            channel.sendFile(baos.toByteArray(), "histo.png").complete();
        }
        catch(IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
