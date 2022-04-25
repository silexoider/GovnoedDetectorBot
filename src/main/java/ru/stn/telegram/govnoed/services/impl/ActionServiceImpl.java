package ru.stn.telegram.govnoed.services.impl;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
    private final ChatService chatService;
    private final VoteService voteService;
    private final ChatMemberService chatMemberService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;

    private SendMessage createSendMessage(Chat chat, String text) {
        SendMessage sendMessage = new SendMessage(chat.getId().toString(), text);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    @Override
    public BotApiMethod<?> showMenu(Chat chat, ResourceBundle resourceBundle) {
        SendMessage sendMessage = createSendMessage(chat, resourceBundle.getString("menu_message"));
        sendMessage.setReplyMarkup(keyboardService.createInlineKeyboard());
        return sendMessage;
    }
    @Override
    public BotApiMethod<?> showTimezone(Chat chat, ResourceBundle resourceBundle) {
        ru.stn.telegram.govnoed.entities.Chat chatEntity = chatService.findById(chat.getId());
        String text =
                chatEntity == null ?
                        resourceBundle.getString("view_zone_absent_message")
                        :
                        String.format(resourceBundle.getString("view_zone_exists_message"), chatEntity.getTimezone());
        return createSendMessage(chat, text);
    }
    @Override
    public BotApiMethod<?> setTimezone(Bot bot, Chat chat, User sender, String text, ResourceBundle resourceBundle) {
        String result;
        try {
            ChatMember chatMember = bot.execute(new GetChatMember(chat.getId().toString(), sender.getId()));
            if (!chat.getType().equals("private") && !Arrays.asList("creator", "administrator").contains(chatMember.getStatus())) {
                throw new RuntimeException(resourceBundle.getString("zone_action_failure_insufficient_privileges_message"));
            }
            ZoneId timezone;
            try {
                timezone = ZoneId.of(text);
            } catch (Exception e) {
                throw new RuntimeException(String.format(resourceBundle.getString("zone_action_failure_invalid_timezone_message"), text), e);
            }
            chatService.setTimezone(chat.getId(), timezone);
            result = String.format(resourceBundle.getString("zone_action_success_message"), timezone);

        } catch (Exception e) {
            result = String.format(resourceBundle.getString("zone_action_failure_message"), e.getMessage());
        }
        return createSendMessage(chat, result);
    }
    @Override
    public BotApiMethod<?> vote(LocalDate date, Chat chat, User sender, Message reply, ResourceBundle resourceBundle) {
        User nominee = reply.getFrom();
        LocalDate replyDate = Instant.ofEpochSecond(reply.getDate()).atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        if (!replyDate.equals(date)) {
            return createSendMessage(
                    chat,
                    String.format(
                        resourceBundle.getString("vote_action_invalid_date_message"),
                        formatService.getDateEntry(replyDate),
                        formatService.getDateEntry(date)
                    )
            );
        }
        boolean success = voteService.vote(date, sender.getId(), nominee.getId(), chat.getId());
        String text;
        if (success) {
            text = String.format(
                    resourceBundle.getString("vote_action_message"),
                    formatService.getUserString(sender, resourceBundle),
                    formatService.getUserString(nominee, resourceBundle)
            );
        } else {
            text = String.format(
                    resourceBundle.getString("vote_action_denied_message"),
                    formatService.getUserString(sender, resourceBundle),
                    formatService.getDateEntry(date)
            );
        }
        return createSendMessage(chat, text);
    }
    @Override
    public BotApiMethod<?> showVote(Bot bot, LocalDate date, Chat chat, User sender, ResourceBundle resourceBundle) throws TelegramApiException {
        Vote vote = voteService.getVote(date, sender.getId(), chat.getId());
        String nomineeText;
        if (vote == null) {
            nomineeText = resourceBundle.getString("view_vote_message_not_voted");
        } else {
            User nominee = chatMemberService.getChatMemberUser(bot, chat, vote.getNomineeId());
            nomineeText = formatService.getUserString(nominee, resourceBundle);
        }
        return createSendMessage(
                chat,
                String.format(
                    resourceBundle.getString("view_vote_message"),
                    formatService.getUserString(sender, resourceBundle),
                    formatService.getDateEntry(date),
                    nomineeText
                )
        );
    }
    @Override
    public BotApiMethod<?> revoke(Instant instant, Chat chat, User sender, ResourceBundle resourceBundle) {
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        boolean success = voteService.revoke(date, sender.getId(), chat.getId());
        String textFormat = success ? resourceBundle.getString("revoke_message") : resourceBundle.getString("unable_to_revoke_message");
        String text = String.format(textFormat, formatService.getUserString(sender, resourceBundle));
        return createSendMessage(
                chat,
                text
        );
    }
    @Override
    public BotApiMethod<?> showWinners(Bot bot, Instant instant, Chat chat, ResourceBundle resourceBundle) {
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        VoteService.Winners winners = voteService.winners(date, chat.getId());
        List<User> winnerUsers = winners.getIds().stream().map(id -> chatMemberService.getChatMemberUserUnchecked(bot, chat, id)).collect(Collectors.toList());
        String text = null;
        String dateText = formatService.getDateEntry(date);
        if (winnerUsers.size() == 0) {
            text = String.format(resourceBundle.getString("no_winner_message"), dateText);
        }
        if (winnerUsers.size() == 1) {
            User winner = winnerUsers.get(0);
            text = String.format(resourceBundle.getString("single_winner_message"), dateText, winners.getScore(), formatService.getUserString(winner, resourceBundle));
        }
        if (winnerUsers.size() > 1) {
            text = String.format(resourceBundle.getString("multiple_winners_message"), dateText, winners.getScore(), formatService.usersToString(winnerUsers, resourceBundle));
        }
        return createSendMessage(
                chat,
                text
        );
    }
    public BotApiMethod<?> showScores(Bot bot, Instant instant, Chat chat, ResourceBundle resourceBundle) {
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        List<VoteService.Score> scores = voteService.scores(date, chat.getId());
        StringBuilder text = new StringBuilder(resourceBundle.getString("scores_message"));
        String entryFormat = resourceBundle.getString("scores_entry");
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
        return createSendMessage(chat, text.toString());
    }
}
