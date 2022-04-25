package ru.stn.telegram.govnoed.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.services.ReplyService;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class BaseReplyService<T extends BaseReplyService.BaseEntry> implements ReplyService<T> {
    @FunctionalInterface
    protected interface EntryFunction<T extends BaseEntry> {
        BotApiMethod<?> apply(Bot bot, Instant instant, Chat chat, User sender, Message reply, T entry, ResourceBundle resourceBundle) throws TelegramApiException;
    }

    @Getter
    @RequiredArgsConstructor
    public static class BaseEntry {
        private final String name;
    }

    private final Map<String, EntryFunction<T>> entryHandlers = createEntryHandlers();

    @Override
    public BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle) {
        try {
            T entry = convertTextToEntry(text);
            if (!check(entry)) {
                return null;
            }
            EntryFunction<T> func = entryHandlers.get(entry.getName());
            if (func != null) {
                return func.apply(bot, instant, chat, sender, reply, entry, resourceBundle);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected boolean check(T entry) {
        return true;
    }

    protected abstract T convertTextToEntry(String text);
    protected abstract Map<String, EntryFunction<T>> createEntryHandlers();
}
