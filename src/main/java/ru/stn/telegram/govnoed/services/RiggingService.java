package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;
import java.util.ResourceBundle;

public interface RiggingService {
    BotApiMethod<?> rigVote(LocalDate date, User sender, User nominee, Chat chat, ResourceBundle resourceBundle);
}
