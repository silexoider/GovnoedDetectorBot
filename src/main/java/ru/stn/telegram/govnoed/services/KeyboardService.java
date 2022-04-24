package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface KeyboardService {
    ReplyKeyboardMarkup createReplyKeyboard();
    InlineKeyboardMarkup createInlineKeyboard();
}
