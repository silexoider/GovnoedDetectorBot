package ru.stn.telegram.govnoed.handling;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class MapHandler<TKey, TArg, TRes> extends BaseHandler<TKey, TRes, TKey, TArg> {
    public MapHandler(Map.Entry<TKey, BiFunction<TKey, TArg, TRes>> ... entries) {
        super(Arrays.stream(entries).collect(Collectors.toList()));
    }

    @Override
    protected EntryContainer<TKey, BiFunction<TKey, TArg, TRes>, TKey> createContainer() {
        return new MapEntryContainer<>();
    }
}
