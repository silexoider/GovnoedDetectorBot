package ru.stn.telegram.govnoed.handling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListHandler<TKey, TArg, TRes> extends BaseHandler<Function<TKey, Boolean>, TRes, TKey, TArg> {
    public ListHandler(Map.Entry<Function<TKey, Boolean>, BiFunction<TKey, TArg, TRes>> ... entries) {
        super(Arrays.stream(entries).collect(Collectors.toList()));
    }

    @Override
    protected EntryContainer<Function<TKey, Boolean>, BiFunction<TKey, TArg, TRes>, TKey> createContainer() {
        return new ListEntryContainer<>();
    }
}
