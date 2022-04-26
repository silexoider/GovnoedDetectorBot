package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;

public interface SendMessageService {
    SendMessage createSendMessage(Chat chat, String text);
}
