package ru.stn.telegram.govnoed.handling;

import java.util.function.Function;

public interface EntryContainer<TKey, TVal, TSel> {
    void insert(TKey key, TVal value);
    TVal select(TSel selector);
}
