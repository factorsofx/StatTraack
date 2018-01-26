package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.stat.MessageStat;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.*;

@RegisterCommand(value = "channelHistory", permissions = Permission.VISUALS)
public class ChannelHistoryCommand implements BotCommand
{
    private PersistenceService persistenceService;
    private StandardChartTheme chartTheme;

    public ChannelHistoryCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;

        chartTheme = new StandardChartTheme("std", false);
        chartTheme.setBarPainter(new StandardBarPainter());
        chartTheme.setXYBarPainter(new StandardXYBarPainter());
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<TextChannel> targetChannels = new ArrayList<>();
        if(message.getMentionedChannels().isEmpty())
        {
            channel.sendMessage("Generating two-week histogram chart for current channel...").complete();
            targetChannels.add(channel);
        }
        else
        {
            targetChannels.addAll(message.getMentionedChannels());
            channel.sendMessage("Generating two-week histogram chart for given channels...").complete();
        }

        DefaultTableXYDataset dataset = new DefaultTableXYDataset();

        OffsetDateTime first = OffsetDateTime.now().minusDays(14);
        OffsetDateTime last = OffsetDateTime.now();

        for(TextChannel targetChannel : targetChannels)
        {
            XYSeries series = new XYSeries(targetChannel.getName(), true, false);
            List<MessageStat> stats = persistenceService.getChannelStats(Collections.singletonList(targetChannel));
            DatasetUtils.makeHistogramPlot(stats, series, first, last, 168);
            dataset.addSeries(series);
        }
        XYPlot plot = new XYPlot(dataset, new DateAxis("Message Time"), new NumberAxis("Messages"), new XYBarRenderer());
        plot.setOrientation(PlotOrientation.VERTICAL);

        plot.setRenderer(new StackedXYAreaRenderer2());
        // plot.setRenderer(new XYArea);

        JFreeChart chart = new JFreeChart("Channel Activity", plot);
        chartTheme.apply(chart);

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

    private static double getDoubleFromMessageStat(MessageStat stat)
    {
        return (double)(stat.getTime().toInstant().toEpochMilli());
    }
}
