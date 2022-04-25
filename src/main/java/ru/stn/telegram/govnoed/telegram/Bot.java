package ru.stn.telegram.govnoed.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.stn.telegram.govnoed.config.TelegramConfig;
import ru.stn.telegram.govnoed.services.CommandService;
import ru.stn.telegram.govnoed.services.FormatService;
import ru.stn.telegram.govnoed.services.KeyboardService;
import ru.stn.telegram.govnoed.services.VoteService;

import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;

@Getter
@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    private final TelegramConfig config;
    private final VoteService statEntryService;
    private final CommandService botCommandService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;

    void log(String type, Instant instant, Chat chat, User sender, String text, Message reply, Locale locale) {
        System.out.printf(
                "{type: %s, instant: %s, chat: %s, sender: %s, text: %s, reply: %s, locale: %s}%n",
                type,
                formatService.getInstantLogEntry(instant),
                formatService.getChatLogEntry(chat),
                formatService.getUserLogEntry(sender),
                text,
                formatService.getMessageLogEntry(reply),
                formatService.getLocaleLogEntry(locale)
        );
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            boolean found = false;
            String type = null;
            Chat chat = null;
            User sender = null;
            String text = null;
            Message reply = null;
            Locale locale = null;
            Instant instant = null;
            if (update.hasMessage()) {
                found = true;
                type = "message";
                chat = update.getMessage().getChat();
                sender = update.getMessage().getFrom();
                text = update.getMessage().getText();
                reply = update.getMessage().getReplyToMessage();
                locale = Locale.forLanguageTag(update.getMessage().getFrom().getLanguageCode());
                instant = Instant.ofEpochSecond(update.getMessage().getDate());
            }
            if (update.hasCallbackQuery() && !found) {
                found = true;
                type = "callback";
                chat = update.getCallbackQuery().getMessage().getChat();
                sender = update.getCallbackQuery().getFrom();
                text = update.getCallbackQuery().getData();
                instant = Instant.now();
            }
            if (found) {
                log(type, instant, chat, sender, text, reply, locale);
                ResourceBundle resourceBundle = locale == null ? ResourceBundle.getBundle("messages") : ResourceBundle.getBundle("messages", locale);
                BotApiMethod<?> method = botCommandService.process(this, instant, chat, sender, text, reply, resourceBundle);
                execute(method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
