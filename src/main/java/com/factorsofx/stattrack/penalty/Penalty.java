package com.factorsofx.stattrack.penalty;

import java.time.OffsetDateTime;

public class Penalty
{
    private long issuer;
    private long sufferer;

    private String reason = "none";
    private int value;

    private OffsetDateTime issued;
    private OffsetDateTime expires = OffsetDateTime.MAX;

    public long getIssuer()
    {
        return issuer;
    }

    public Penalty setIssuer(long issuer)
    {
        this.issuer = issuer;
        return this;
    }

    public long getSufferer()
    {
        return sufferer;
    }

    public Penalty setSufferer(long sufferer)
    {
        this.sufferer = sufferer;
        return this;
    }

    public String getReason()
    {
        return reason;
    }

    public Penalty setReason(String reason)
    {
        this.reason = reason;
        return this;
    }

    public int getValue()
    {
        return value;
    }

    public Penalty setValue(int value)
    {
        this.value = value;
        return this;
    }

    public OffsetDateTime getIssued()
    {
        return issued;
    }

    public Penalty setIssued(OffsetDateTime issued)
    {
        this.issued = issued;
        return this;
    }

    public OffsetDateTime getExpires()
    {
        return expires;
    }

    public Penalty setExpires(OffsetDateTime expires)
    {
        this.expires = expires;
        return this;
    }
}
