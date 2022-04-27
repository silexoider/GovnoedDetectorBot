package ru.stn.telegram.govnoed.handling;

import java.util.Map;
import java.util.function.BiFunction;

public abstract class BaseHandler<TKey, TRes, TSel, TArg> implements Handler<TKey, TRes, TSel, TArg> {
    private final EntryContainer<TKey, BiFunction<TSel, TArg, TRes>, TSel> container = createContainer();

    protected BaseHandler(Iterable<Map.Entry<TKey, BiFunction<TSel, TArg, TRes>>> entries) {
        for (Map.Entry<TKey, BiFunction<TSel, TArg, TRes>> entry : entries) {
            container.insert(entry.getKey(), entry.getValue());
        }
    }

    public TRes handle(TSel selector, TArg argument) {
        BiFunction<TSel, TArg, TRes> action = container.select(selector);
        if (action != null) {
            return action.apply(selector, argument);
        } else {
            return null;
        }
    }

    protected abstract EntryContainer<TKey, BiFunction<TSel, TArg, TRes>, TSel> createContainer();
}
