package com.factorsofx.stattrack.stat;

import com.factorsofx.stattrack.stat.MessageStat;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;

public class DatasetUtils
{
    public static void makeAccumPlot(List<MessageStat> stats, XYSeries series, OffsetDateTime first, OffsetDateTime last, int bins)
    {
        Iterator<MessageStat> iter = stats.iterator();
        MessageStat currentRecord = iter.next();
        OffsetDateTime current = first;
        Duration interval = Duration.between(first, last).abs().dividedBy(bins);
        int accum = 0;
        do
        {
            series.add(current.toInstant().toEpochMilli(), accum);
            current = current.plus(interval);
            while(currentRecord.getTime().isBefore(current))
            {
                if(iter.hasNext())
                {
                    currentRecord = iter.next();
                    accum++;
                }
                else
                {
                    break;
                }
            }
        }
        while(current.isBefore(last));
    }

    public static void makeHistogramPlot(List<MessageStat> stats, XYSeries series, OffsetDateTime first, OffsetDateTime last, int bins)
    {
        Iterator<MessageStat> iter = stats.iterator();
        MessageStat currentRecord;// = iter.next();
        OffsetDateTime current = first;
        Duration interval = Duration.between(first, last).abs().dividedBy(bins);
        int accum = 0;
        do
        {
            series.add(current.toInstant().toEpochMilli(), accum);
            accum = 0;
            current = current.plus(interval);
            //while(currentRecord.getTime().isBefore(current))
            while(iter.hasNext())
            {
                currentRecord = iter.next();
                if(currentRecord.getTime().isBefore(current) && currentRecord.getTime().isAfter(first))
                {
                    accum++;
                }
                else
                {
                    break;
                }
            }
        }
        while(current.isBefore(last));
    }

    public static XYDataset makeEqualAreaHistogramPlot(List<MessageStat> stats, List<TextChannel> channels, OffsetDateTime first, OffsetDateTime last)
    {
        XYDataset dataset = new DefaultXYDataset();

        return null;
    }
}
