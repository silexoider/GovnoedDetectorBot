package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.stn.telegram.govnoed.services.KeyboardService;
import ru.stn.telegram.govnoed.services.LocalizationService;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class KeyboardServiceImpl implements KeyboardService {
    private final LocalizationService localizationService;

    @Override
    public InlineKeyboardMarkup createInlineKeyboard(ResourceBundle resourceBundle) {
        List<List<InlineKeyboardButton>> rows = Arrays.asList(
                Arrays.asList(
                        createInlineKeyboardButton(localizationService.getMenuButtonMessage(resourceBundle), "/menu"),
                        createInlineKeyboardButton(localizationService.getWinnerButtonMessage(resourceBundle), "/winner"),
                        createInlineKeyboardButton(localizationService.getScoresButtonMessage(resourceBundle), "/scores")
                ),
                Arrays.asList(
                        createInlineKeyboardButton(localizationService.getVoteButtonMessage(resourceBundle), "/vote"),
                        createInlineKeyboardButton(localizationService.getRevokeButtonMessage(resourceBundle), "/revoke")
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
