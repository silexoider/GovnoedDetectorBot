package ru.stn.telegram.govnoed.handling;

import java.util.HashMap;
import java.util.Map;

public class MapEntryContainer<TKey, TVal> implements EntryContainer<TKey, TVal, TKey> {
    private final Map<TKey, TVal> entries = new HashMap<>();

    @Override
    public void insert(TKey key, TVal value) {
        entries.put(key, value);
    }

    @Override
    public TVal select(TKey selector) {
        return entries.get(selector);
    }
}
