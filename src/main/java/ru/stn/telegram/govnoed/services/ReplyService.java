package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.util.ResourceBundle;

public interface ReplyService {
    BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle);
}
