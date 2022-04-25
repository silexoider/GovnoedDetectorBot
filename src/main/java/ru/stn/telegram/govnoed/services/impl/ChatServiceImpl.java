package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stn.telegram.govnoed.entities.Chat;
import ru.stn.telegram.govnoed.repositories.ChatRepository;
import ru.stn.telegram.govnoed.services.ChatService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;

    @Override
    public Chat findById(long id) {
        Optional<Chat> chat = chatRepository.findById(id);
        return chat.orElse(null);
    }

    @Override
    public void setTimezone(Chat chat, ZoneId timezone) {
        chat.setTimezone(timezone);
        chatRepository.save(chat);
    }

    @Override
    public void setTimezone(long chatId, ZoneId timezone) {
        Chat chat = findById(chatId);
        if (chat == null) {
            chat = new Chat(chatId, timezone);
        } else {
            chat.setTimezone(timezone);
        }
        chatRepository.save(chat);
    }

    @Override
    public ZoneId getTimezoneById(long chatId) {
        Chat chat = findById(chatId);
        return
                chat == null ?
                        ZoneId.of("UTC")
                        :
                        chat.getTimezone();
    }

    @Override
    public LocalDate instantToDate(long chatId, Instant instant) {
        return instant.atZone(getTimezoneById(chatId)).toLocalDate();
    }
}
