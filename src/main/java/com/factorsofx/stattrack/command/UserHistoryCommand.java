package com.factorsofx.stattrack.command;

import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.stat.DatasetUtils;
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
import java.util.ArrayList;
import java.util.List;

@RegisterCommand(value = "userhistory", permissions = Permission.VISUALS)
public class UserHistoryCommand implements BotCommand
{
    private PersistenceService persistenceService;
    private StandardChartTheme chartTheme;

    public UserHistoryCommand(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;

        chartTheme = new StandardChartTheme("std", false);
        //chartTheme.setBarPainter(new StandardBarPainter());
        //chartTheme.setXYBarPainter(new StandardXYBarPainter());
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        List<User> targetChannels = new ArrayList<>();
        if(message.getMentionedUsers().isEmpty())
        {
            if(args.length == 0)
            {
                channel.sendMessage("Generating one-week message history for you...").complete();
                targetChannels.add(user);
            }
            else
            {
                for(String uname : args)
                {
                    targetChannels.addAll(message.getJDA().getUsersByName(uname, true));
                }
                channel.sendMessage("Generating one-week message history for given users...").complete();
            }
        }
        else
        {
            targetChannels.addAll(message.getMentionedUsers());
            channel.sendMessage("Generating one-week message history for given users...").complete();
        }

        DefaultTableXYDataset dataset = new DefaultTableXYDataset();

        OffsetDateTime first = OffsetDateTime.now().minusDays(7);
        OffsetDateTime last = OffsetDateTime.now();

        for(User person : targetChannels)
        {
            XYSeries series = new XYSeries(person.getName(), true, false);
            List<MessageStat> stats = persistenceService.getTimeLimitedUserStats(person, message.getGuild(), first, last);
            //DatasetUtils.makeHistogramPlot(stats, series, first, last, 42);
            DatasetUtils.makeAccumPlot(stats, series, first, last, 42);
            dataset.addSeries(series);
        }
        XYPlot plot = new XYPlot(dataset, new DateAxis("Message Time"), new NumberAxis("Messages"), new StackedXYAreaRenderer2());
        plot.setOrientation(PlotOrientation.VERTICAL);

        plot.setRenderer(new StackedXYAreaRenderer2());
        // plot.setRenderer(new XYArea);

        JFreeChart chart = new JFreeChart("User Activity", plot);
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
}
