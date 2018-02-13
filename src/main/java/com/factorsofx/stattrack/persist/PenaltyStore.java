package com.factorsofx.stattrack.persist;

import com.factorsofx.stattrack.penalty.Penalty;

import java.util.Collection;

public interface PenaltyStore extends DataStore<Penalty>
{
    Collection<Penalty> getPenaltiesForUser(long userId);

    int getAccumPenaltyForUser(long userId);
}
