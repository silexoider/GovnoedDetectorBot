package ru.stn.telegram.govnoed.services.impl;

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

public abstract class BaseReplyServiceImpl<T extends BaseReplyServiceImpl.Entry> implements ReplyService<T> {
    @FunctionalInterface
    protected interface EntryFunction<T extends Entry> {
        BotApiMethod<?> apply(Bot bot, Instant instant, Chat chat, User sender, Message reply, T entry, ResourceBundle resourceBundle) throws TelegramApiException;
    }

    public interface Entry {
        String getKey();
    }

    private final Map<String, EntryFunction<T>> entryHandlers = createEntryHandlers();

    @Override
    public BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle) {
        try {
            T entry = convertTextToEntry(text);
            if (!check(entry)) {
                return null;
            }
            EntryFunction<T> func = entryHandlers.get(entry.getKey());
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
