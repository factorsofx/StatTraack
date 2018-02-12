package com.factorsofx.stattrack.stat;

import gnu.trove.set.TLongSet;
import org.jfree.chart.JFreeChart;

import java.time.OffsetDateTime;

public class AdvStatReq
{
    public enum Mode
    {
        PIE_CHART,
        HISTOGRAM,
        STACKED_AREA
    }

    public enum StatTarget
    {
        USERS,
        CHANNELS
    }

    private Mode mode = Mode.STACKED_AREA;
    private StatTarget target = StatTarget.USERS;
    private TLongSet users = null;
    private TLongSet channels = null;
    private OffsetDateTime before = OffsetDateTime.MAX;
    private OffsetDateTime after = OffsetDateTime.MIN;
    private boolean includePenalties = false;
    private long guild = 0L;

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    public StatTarget getTarget()
    {
        return target;
    }

    public void setTarget(StatTarget target)
    {
        this.target = target;
    }

    public TLongSet getUsers()
    {
        return users;
    }

    public void setUsers(TLongSet users)
    {
        this.users = users;
    }

    public TLongSet getChannels()
    {
        return channels;
    }

    public void setChannels(TLongSet channels)
    {
        this.channels = channels;
    }

    public OffsetDateTime getBefore()
    {
        return before;
    }

    public void setBefore(OffsetDateTime before)
    {
        this.before = before;
    }

    public OffsetDateTime getAfter()
    {
        return after;
    }

    public void setAfter(OffsetDateTime after)
    {
        this.after = after;
    }

    public boolean includePenalties()
    {
        return includePenalties;
    }

    public void setIncludePenalties(boolean includePenalties)
    {
        this.includePenalties = includePenalties;
    }

    public long getGuild()
    {
        return guild;
    }

    public void setGuild(long guild)
    {
        this.guild = guild;
    }
}
