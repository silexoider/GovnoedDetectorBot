package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.stn.telegram.govnoed.services.ActionService;
import ru.stn.telegram.govnoed.services.ChatService;
import ru.stn.telegram.govnoed.services.MessageService;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends BaseReplyService<MessageServiceImpl.Entry> implements MessageService {
    @RequiredArgsConstructor
    public static class Entry implements BaseReplyService.Entry {
        private final String value;

        @Override
        public String getKey() {
            return value;
        }
    }

    private final ChatService chatService;
    private final ActionService actionService;

    private final Pattern pattern = Pattern.compile("[A-Za-zА-ЯЁа-яё]+");

    private BotApiMethod<?> common(Bot bot, Instant instant, Chat chat, User sender, Message reply, MessageServiceImpl.Entry entry, ResourceBundle resourceBundle) {
        if (reply == null) {
            return null;
        }
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        return actionService.vote(date, chat, sender, reply, resourceBundle);
    }

    @Override
    protected Entry convertTextToEntry(String text) {
        List<String> words = new LinkedList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group().toUpperCase());
        }
        return new Entry(words.stream().reduce("", (subtotal, element) -> subtotal == "" ? element : String.format("%s %s", subtotal, element)));
    }

    @Override
    protected Map<String, EntryFunction<Entry>> createEntryHandlers() {
        return new HashMap<String, EntryFunction<MessageServiceImpl.Entry>>() {{
            put("ГОВНОЕД", MessageServiceImpl.this::common);
            put("ТЫ ГОВНОЕД", MessageServiceImpl.this::common);
            put("ГОВНОЕД ТЫ", MessageServiceImpl.this::common);
        }};
    }
}
