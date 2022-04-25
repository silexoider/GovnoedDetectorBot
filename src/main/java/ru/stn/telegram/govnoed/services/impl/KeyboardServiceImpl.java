package ru.stn.telegram.govnoed.services.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.stn.telegram.govnoed.services.KeyboardService;

import java.util.Arrays;
import java.util.List;

@Service
public class KeyboardServiceImpl implements KeyboardService {
    @Override
    public ReplyKeyboardMarkup createReplyKeyboard() {
        List<KeyboardRow> rows = Arrays.asList(
                new KeyboardRow(
                        Arrays.asList(
                                new KeyboardButton("Button2")
                        )
                )
        );
        return new ReplyKeyboardMarkup(rows);
    }

    @Override
    public InlineKeyboardMarkup createInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = Arrays.asList(
                Arrays.asList(
                        createInlineKeyboardButton("Меню", "/menu"),
                        createInlineKeyboardButton("Победитель", "/winner"),
                        createInlineKeyboardButton("Таблица", "/scores")
                ),
                Arrays.asList(
                        createInlineKeyboardButton("Мой голос", "/vote"),
                        createInlineKeyboardButton("Отозвать голос", "/revoke")
                )
        );
        return new InlineKeyboardMarkup(rows);
    }
    public InlineKeyboardButton createInlineKeyboardButton(String name, String data) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(name);
        button.setCallbackData(data);
        return button;
    }
}
