package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ResourceBundle;

public interface ActionService {
    BotApiMethod<?> showMenu(Chat chat, ResourceBundle resourceBundle);
    BotApiMethod<?> showTimezone(Chat chat, ResourceBundle resourceBundle);
    BotApiMethod<?> setTimezone(Bot bot, Chat chat, User sender, String text, ResourceBundle resourceBundle);
    BotApiMethod<?> vote(LocalDate date, Chat chat, User sender, Message reply, ResourceBundle resourceBundle);
    BotApiMethod<?> showVote(Bot bot, LocalDate date, Chat chat, User sender, ResourceBundle resourceBundle) throws TelegramApiException;
    BotApiMethod<?> revoke(LocalDate date, Chat chat, User sender, ResourceBundle resourceBundle);
    BotApiMethod<?> showWinners(Bot bot, LocalDate date, Chat chat, ResourceBundle resourceBundle);
    BotApiMethod<?> showScores(Bot bot, LocalDate date, Chat chat, ResourceBundle resourceBundle);
}
