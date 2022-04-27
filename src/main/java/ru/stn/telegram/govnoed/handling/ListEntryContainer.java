package ru.stn.telegram.govnoed.handling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ListEntryContainer<TKey, TVal> implements EntryContainer<Function<TKey, Boolean>, TVal, TKey> {
    @Getter
    @RequiredArgsConstructor
    public class Entry {
        private final Function<TKey, Boolean> filter;
        private final TVal value;
    }

    private final List<Entry> entries = new LinkedList<>();

    @Override
    public void insert(Function<TKey, Boolean> filter, TVal value) {
        entries.add(new Entry(filter, value));
    }

    @Override
    public TVal select(TKey selector) {
        Entry entry = entries.stream().filter(e -> e.filter.apply(selector)).findFirst().orElse(null);
        if (entry == null) {
            return null;
        } else {
            return entry.getValue();
        }
    }
}
