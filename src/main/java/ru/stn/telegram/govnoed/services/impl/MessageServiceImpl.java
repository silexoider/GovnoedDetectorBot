package ru.stn.telegram.govnoed.services.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.handling.ListHandler;
import ru.stn.telegram.govnoed.services.ActionService;
import ru.stn.telegram.govnoed.services.ChatService;
import ru.stn.telegram.govnoed.services.MessageService;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ListHandler<String, MessageServiceImpl.Args, BotApiMethod<?>> implements MessageService {
    @FunctionalInterface
    protected interface EntryFunction {
        BotApiMethod<?> apply(Bot bot, Instant instant, Chat chat, User sender, Message reply, ResourceBundle resourceBundle) throws TelegramApiException;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Entry {
        private final Function<String, Boolean> filter;
        private final BiFunction<String, Args, BotApiMethod<?>> action;
    }

    @Getter
    @AllArgsConstructor
    public static class Args {
        private Bot bot;
        private Instant instant;
        private Chat chat;
        private User sender;
        private Message reply;
        private ResourceBundle resourceBundle;
    }

    private final ChatService chatService;
    private final ActionService actionService;

    private final Pattern pattern = Pattern.compile("\\bговноед(|[аеуы]|ов|ам|ом|ами|ах)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final List<Entry> entries = Arrays.asList(
            new Entry(this::voteFilter, normalize(this::voteAction))
    );

    private static BiFunction<String, Args, BotApiMethod<?>> normalize(EntryFunction function) {
        return
                (k, a) -> {
                    try {
                        return function.apply(a.getBot(), a.getInstant(), a.getChat(), a.getSender(), a.getReply(), a.getResourceBundle());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                };
    }

    public MessageServiceImpl(
            ChatService chatService,
            ActionService actionService
    ) {
        init(entries.stream().map(e -> new AbstractMap.SimpleEntry<>(e.getFilter(), e.getAction())).collect(Collectors.toList()));
        this.chatService = chatService;
        this.actionService = actionService;
    }

    private boolean voteFilter(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
    private BotApiMethod<?> voteAction(Bot bot, Instant instant, Chat chat, User sender, Message reply, ResourceBundle resourceBundle) {
        if (reply == null) {
            return null;
        }
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        return actionService.vote(date, chat, sender, reply, resourceBundle);
    }

    @Override
    public BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle) {
        return handle(text, new Args(bot, instant, chat, sender, reply, resourceBundle));
    }
}
