package com.factorsofx.stattrack.persist.mongo;

import com.factorsofx.stattrack.persist.DataStore;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractMongoDataStore<T> implements DataStore<T>
{
    MongoCollection<T> collection;

    AbstractMongoDataStore(MongoDatabase db, String collectionName, Class<T> clazz)
    {
        collection = db.getCollection(collectionName, clazz);
    }

    @Override
    public Collection<T> getAll()
    {
        return Stream.generate(collection.find().iterator()::next).collect(Collectors.toList());
    }

    @Override
    public long size()
    {
        return collection.count();
    }

    @Override
    public void store(T obj)
    {
        collection.insertOne(obj);
    }
}
