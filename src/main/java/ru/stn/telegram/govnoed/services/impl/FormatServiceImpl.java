package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.stn.telegram.govnoed.services.FormatService;
import ru.stn.telegram.govnoed.services.LocalizationService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class FormatServiceImpl implements FormatService {
    private final LocalizationService localizationService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter instantFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public String getUserName(User user) {
        if (user.getUserName() != null) {
            return user.getUserName();
        }
        String text = null;
        if (user.getFirstName() != null) {
            text = user.getFirstName();
        }
        if (user.getLastName() != null) {
            if (text == null) {
                text = user.getLastName();
            }
            else {
                text = String.format("%s %s", text, user.getLastName());
            }
        }
        if (text != null) {
            return text;
        }
        return user.getId().toString();
    }

    @Override
    public String getInstantLogEntry(Instant instant) {
        return String.format("%s UTC", instantFormatter.format(instant.atOffset(ZoneOffset.UTC)));
    }
    @Override
    public String getDateEntry(Temporal temporal) {
        return dateFormatter.format(temporal);
    }
    @Override
    public String getChatLogEntry(Chat chat) {
        return chat == null ? "{null}" : String.format("{id: %d, type: %s, title: %s, un: %s, fn: %s, ln: %s}", chat.getId(), chat.getType(), chat.getTitle(), chat.getUserName(), chat.getFirstName(), chat.getLastName());
    }
    @Override
    public String getUserLogEntry(User user) {
        return user == null ? "{null}" : String.format("{id: %d, un: %s, fn: %s, ln: %s}", user.getId(), user.getUserName(), user.getFirstName(), user.getLastName());
    }
    @Override
    public String getMessageLogEntry(Message message) {
        return message == null ? "{null}" : String.format("{id: %d, chat: %s, sender: %s, text: %s, reply: %s", message.getMessageId(), getChatLogEntry(message.getChat()), getUserLogEntry(message.getFrom()), message.getText(), getMessageLogEntry(message.getReplyToMessage()));
    }
    @Override
    public String getLocaleLogEntry(Locale locale) {
        return locale == null ? "{null}" : locale.toLanguageTag();
    }
    @Override
    public String usersToString(List<User> users, ResourceBundle resourceBundle) {
        StringBuilder result = new StringBuilder();
        for (User user : users) {
            String item = getUserString(user, resourceBundle);
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(item);
        }
        return result.toString();
    }
    @Override
    public String getUserString(User user, ResourceBundle resourceBundle) {
        return
                user == null ?
                        localizationService.getShowAbsentUserMessage(resourceBundle)
                        :
                        String.format(localizationService.getShowPresentUserMessage(resourceBundle), user.getId(), getUserName(user));
    }

    @Override
    public LocalDate parseDate(String text) {
        LocalDate date;
        try {
            date = LocalDate.parse(text, dateFormatter);
        } catch (DateTimeParseException e) {
            date = null;
        }
        return date;
    }
}
