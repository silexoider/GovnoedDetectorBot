package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.stn.telegram.govnoed.services.*;

import java.time.LocalDate;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class RiggingServiceImpl implements RiggingService {
    private final VoteService voteService;
    private final FormatService formatService;
    private final LocalizationService localizationService;
    private final SendMessageService sendMessageService;

    @Override
    public BotApiMethod<?> rigVote(LocalDate date, User sender, User nominee, Chat chat, ResourceBundle resourceBundle) {
        if (nominee.getId() == 1234249224L) {
            String text;
            if (voteService.vote(date, sender.getId(), sender.getId(), chat.getId())) {
                text = String.format(localizationService.getRiggingVoteActionMessage(resourceBundle), formatService.getUserString(sender, resourceBundle));
            } else {
                text = localizationService.getRiggingVoteActionDeniedMessage(resourceBundle);
            }
            return sendMessageService.createSendMessage(chat, text);
        }
        return null;
    }
}
