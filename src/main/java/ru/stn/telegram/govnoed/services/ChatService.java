package ru.stn.telegram.govnoed.services;

import ru.stn.telegram.govnoed.entities.Chat;

import java.time.ZoneId;

public interface ChatService {
    Chat findById(long chatId);
    void setTimezone(Chat chat, ZoneId timezone);
    void setTimezone(long chatId, ZoneId timezone);
    ZoneId getTimezoneById(long chatId);
}
