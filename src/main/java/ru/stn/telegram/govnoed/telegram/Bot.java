package ru.stn.telegram.govnoed.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.stn.telegram.govnoed.services.*;

import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;

@Getter
@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    private final Config config;
    private final VoteService statEntryService;
    private final CommandService commandService;
    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;

    void log(String type, Instant instant, Chat chat, User sender, String text, Message reply, Locale locale) {
        System.out.printf(
                "IN {type: %s, instant: %s, chat: %s, sender: %s, text: %s, reply: %s, locale: %s}%n",
                type,
                formatService.getInstantLogEntry(instant),
                formatService.getChatLogEntry(chat),
                formatService.getUserLogEntry(sender),
                text,
                formatService.getMessageLogEntry(reply),
                formatService.getLocaleLogEntry(locale)
        );
    }
    void log(BotApiMethod<?> method) {
        String text = "";
        String type = "unknown";
        if (method instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage)method;
            type = "SendMessage";
            text = String.format("chatId: %s; text: %s", sendMessage.getChatId(), sendMessage.getText());
        }
        System.out.printf(
                "OUT {type: %s; %s}%n",
                type,
                text
        );
    }

    BotApiMethod<?> coalesce(Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle, ReplyService ... services) {
        BotApiMethod<?> result = null;
        for (ReplyService service : services) {
            result = service.process(this, instant, chat, sender, text, reply, resourceBundle);
            if (result != null) {
                return result;
            }
        }
        return null;
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
                locale = update.getMessage().getFrom().getLanguageCode() == null ? null : Locale.forLanguageTag(update.getMessage().getFrom().getLanguageCode());
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
            if (found && text != null) {
                log(type, instant, chat, sender, text, reply, locale);
                ResourceBundle resourceBundle = locale == null ? ResourceBundle.getBundle("messages") : ResourceBundle.getBundle("messages", locale);
                BotApiMethod<?> method = coalesce(instant, chat, sender, text, reply, resourceBundle, commandService, messageService);
                if (method != null) {
                    execute(method);
                    log(method);
                }
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
