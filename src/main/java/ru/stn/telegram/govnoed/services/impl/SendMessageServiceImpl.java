package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import ru.stn.telegram.govnoed.services.SendMessageService;

@Service
public class SendMessageServiceImpl implements SendMessageService {
    @Override
    public SendMessage createSendMessage(Chat chat, String text) {
        SendMessage sendMessage = new SendMessage(chat.getId().toString(), text);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }
}
