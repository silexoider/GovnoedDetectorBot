package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Locale;

public interface FormatService {
    String getUserName(User user);

    String getInstantLogEntry(Instant instant);
    String getDateEntry(Temporal temporal);
    String getChatLogEntry(Chat chat);
    String getUserLogEntry(User user);
    String getMessageLogEntry(Message message);
    String getLocaleLogEntry(Locale locale);
}
