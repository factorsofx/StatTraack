package com.factorsofx.stattrack.persist;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface DataStore<T>
{
    Collection<T> getAll();

    void store(T obj);

    long size();

    default Collection<T> getMatching(Predicate<T> predicate)
    {
        return getAll().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
