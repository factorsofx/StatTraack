package com.factorsofx.stattrack.persist.mongo;

import com.factorsofx.stattrack.penalty.Penalty;
import com.factorsofx.stattrack.persist.PenaltyStore;
import com.mongodb.client.MongoDatabase;
import gnu.trove.set.hash.THashSet;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;

public class MongoPenaltyStore extends AbstractMongoDataStore<Penalty> implements PenaltyStore
{
    public MongoPenaltyStore(MongoDatabase db, String collectionName)
    {
        super(db, collectionName, Penalty.class);
    }

    @Override
    public Collection<Penalty> getPenaltiesForUser(long userId)
    {
        return collection.find(eq("userId", userId)).into(new THashSet<>());
    }

    @Override
    public int getAccumPenaltyForUser(long userId)
    {
        AtomicInteger accumPenalty = new AtomicInteger(0);
        OffsetDateTime now = OffsetDateTime.now();

        collection.find(and(eq("sufferer", userId), lte("issued", now), gte("expires", now)))
                .forEach((Consumer<? super Penalty>)(Penalty penalty) -> accumPenalty.addAndGet(penalty.getValue()));
        return accumPenalty.get();
    }
}
