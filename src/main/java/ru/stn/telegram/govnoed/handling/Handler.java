package ru.stn.telegram.govnoed.handling;

public interface Handler<TKey, TRes, TSel, TArg> {
    TRes handle(TSel selector, TArg argument);
}
