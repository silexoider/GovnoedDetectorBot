package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ResourceBundle;

public interface KeyboardService {
    InlineKeyboardMarkup createInlineKeyboard(ResourceBundle resourceBundle);
}
