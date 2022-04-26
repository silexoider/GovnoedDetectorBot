package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import ru.stn.telegram.govnoed.entities.Vote;
import ru.stn.telegram.govnoed.services.*;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {
    public static final long MY_ID = 1234249224L;

    private final ChatService chatService;
    private final VoteService voteService;
    private final ChatMemberService chatMemberService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;
    private final LocalizationService localizationService;
    private final RiggingService riggingService;
    private final SendMessageService sendMessageService;
    private final SettingService settingService;

    @Override
    public BotApiMethod<?> showMenu(Chat chat, ResourceBundle resourceBundle) {
        SendMessage sendMessage = sendMessageService.createSendMessage(chat, localizationService.getMenuMessage(resourceBundle));
        sendMessage.setReplyMarkup(keyboardService.createInlineKeyboard());
        return sendMessage;
    }
    @Override
    public BotApiMethod<?> showTimezone(Chat chat, ResourceBundle resourceBundle) {
        ru.stn.telegram.govnoed.entities.Chat chatEntity = chatService.findById(chat.getId());
        String text =
                chatEntity == null ?
                        localizationService.getViewZoneAbsentMessage(resourceBundle)
                        :
                        String.format(localizationService.getViewZoneExistsMessage(resourceBundle), chatEntity.getTimezone());
        return sendMessageService.createSendMessage(chat, text);
    }
    @Override
    public BotApiMethod<?> setTimezone(Bot bot, Chat chat, User sender, String text, ResourceBundle resourceBundle) {
        String result;
        try {
            ChatMember chatMember = bot.execute(new GetChatMember(chat.getId().toString(), sender.getId()));
            if (!chat.getType().equals("private") && !Arrays.asList("creator", "administrator").contains(chatMember.getStatus())) {
                throw new RuntimeException(localizationService.getZoneActionFailureInsufficientPrivilegesMessage(resourceBundle));
            }
            ZoneId timezone;
            try {
                timezone = ZoneId.of(text);
            } catch (Exception e) {
                throw new RuntimeException(String.format(localizationService.getZoneActionFailureInvalidTimezoneMessage(resourceBundle), text), e);
            }
            chatService.setTimezone(chat.getId(), timezone);
            result = String.format(localizationService.getZoneActionSuccessMessage(resourceBundle), timezone);

        } catch (Exception e) {
            result = String.format(localizationService.getZoneActionFailureMessage(resourceBundle), e.getMessage());
        }
        return sendMessageService.createSendMessage(chat, result);
    }
    @Override
    public BotApiMethod<?> vote(LocalDate date, Chat chat, User sender, Message reply, ResourceBundle resourceBundle) {
        User nominee = reply.getFrom();
        LocalDate replyDate = Instant.ofEpochSecond(reply.getDate()).atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        if (!replyDate.equals(date)) {
            return sendMessageService.createSendMessage(
                    chat,
                    String.format(
                            localizationService.getVoteActionInvalidDateMessage(resourceBundle),
                            formatService.getDateEntry(replyDate),
                            formatService.getDateEntry(date)
                    )
            );
        }
        if (settingService.getRiggingValue()) {
            BotApiMethod<?> rigged = riggingService.rigVote(date, sender, nominee, chat, resourceBundle);
            if (rigged != null) {
                return rigged;
            }
        }
        boolean success = voteService.vote(date, sender.getId(), nominee.getId(), chat.getId());
        String text;
        if (success) {
            text = String.format(
                    localizationService.getVoteActionMessage(resourceBundle),
                    formatService.getUserString(sender, resourceBundle),
                    formatService.getUserString(nominee, resourceBundle)
            );
        } else {
            text = String.format(
                    localizationService.getVoteActionDeniedMessage(resourceBundle),
                    formatService.getUserString(sender, resourceBundle),
                    formatService.getDateEntry(date)
            );
        }
        return sendMessageService.createSendMessage(chat, text);
    }
    @Override
    public BotApiMethod<?> showVote(Bot bot, LocalDate date, Chat chat, User sender, ResourceBundle resourceBundle) {
        Vote vote = voteService.getVote(date, sender.getId(), chat.getId());
        String nomineeText;
        if (vote == null) {
            nomineeText = localizationService.getViewVoteMessageNotVoted(resourceBundle);
        } else {
            User nominee = chatMemberService.getChatMemberUser(bot, chat, vote.getNomineeId());
            nomineeText = formatService.getUserString(nominee, resourceBundle);
        }
        return sendMessageService.createSendMessage(
                chat,
                String.format(
                        localizationService.getViewVoteMessage(resourceBundle),
                        formatService.getUserString(sender, resourceBundle),
                        formatService.getDateEntry(date),
                        nomineeText
                )
        );
    }
    @Override
    public BotApiMethod<?> revoke(LocalDate date, Chat chat, User sender, ResourceBundle resourceBundle) {
        boolean success = voteService.revoke(date, sender.getId(), chat.getId());
        String textFormat = success ? localizationService.getRevokeMessage(resourceBundle) : localizationService.getUnableToRevokeMessage(resourceBundle);
        String text = String.format(textFormat, formatService.getUserString(sender, resourceBundle));
        return sendMessageService.createSendMessage(chat, text);
    }
    @Override
    public BotApiMethod<?> showWinners(Bot bot, LocalDate date, Chat chat, ResourceBundle resourceBundle) {
        VoteService.Winners winners = voteService.winners(date, chat.getId());
        List<User> winnerUsers = winners.getIds().stream().map(id -> chatMemberService.getChatMemberUserUnchecked(bot, chat, id)).collect(Collectors.toList());
        String text = null;
        String dateText = formatService.getDateEntry(date);
        if (winnerUsers.size() == 0) {
            text = String.format(localizationService.getNoWinnerMessage(resourceBundle), dateText);
        }
        if (winnerUsers.size() == 1) {
            User winner = winnerUsers.get(0);
            text = String.format(localizationService.getSingleWinnerMessage(resourceBundle), dateText, winners.getScore(), formatService.getUserString(winner, resourceBundle));
        }
        if (winnerUsers.size() > 1) {
            text = String.format(localizationService.getMultipleWinnersMessage(resourceBundle), dateText, winners.getScore(), formatService.usersToString(winnerUsers, resourceBundle));
        }
        return sendMessageService.createSendMessage(chat, text);
    }
    public BotApiMethod<?> showScores(Bot bot, LocalDate date, Chat chat, ResourceBundle resourceBundle) {
        List<VoteService.Score> scores = voteService.scores(date, chat.getId());
        StringBuilder text = new StringBuilder(
                String.format(
                        localizationService.getScoresMessage(resourceBundle),
                        formatService.getDateEntry(date)
                )
        );
        String entryFormat = localizationService.getScoresEntry(resourceBundle);
        for (VoteService.Score score : scores) {
            User user = chatMemberService.getChatMemberUser(bot, chat, score.getUserId());
            text.append(
                    String.format(
                        "%n%s",
                        String.format(
                                entryFormat,
                                formatService.getUserString(user, resourceBundle),
                                score.getValue()
                        )
                    )
            );
        }
        return sendMessageService.createSendMessage(chat, text.toString());
    }
    @Override
    public BotApiMethod<?> getRigging(Chat chat, User user, ResourceBundle resourceBundle) {
        if (user.getId() == MY_ID) {
            return
                    sendMessageService.createSendMessage(
                            chat,
                            String.format(
                                    localizationService.getRiggingMessage(resourceBundle),
                                    settingService.getRiggingValue()
                            )
                    );
        } else {
            return null;
        }
    }
    @Override
    public BotApiMethod<?> setRigging(Chat chat, User user, boolean rigging, ResourceBundle resourceBundle) {
        if (user.getId() == MY_ID) {
            settingService.setRiggingValue(rigging);
            return sendMessageService.createSendMessage(chat, String.format(localizationService.getRiggingMessage(resourceBundle), rigging));
        } else {
            return null;
        }
    }
}
