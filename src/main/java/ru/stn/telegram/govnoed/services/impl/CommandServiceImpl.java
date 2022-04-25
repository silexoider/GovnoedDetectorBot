package ru.stn.telegram.govnoed.services.impl;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.entities.Vote;
import ru.stn.telegram.govnoed.services.CommandService;
import ru.stn.telegram.govnoed.services.FormatService;
import ru.stn.telegram.govnoed.services.KeyboardService;
import ru.stn.telegram.govnoed.services.VoteService;
import ru.stn.telegram.govnoed.telegram.Bot;
import ru.stn.telegram.govnoed.config.TelegramConfig;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {
    @FunctionalInterface
    private interface CommandFunction {
        BotApiMethod<?> apply(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) throws TelegramApiException;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Command {
        private final String name;
        private final String bot;
        private final List<String> args;
    }

    private final TelegramConfig config;
    private final VoteService voteService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;

    private final Pattern mainPattern = Pattern.compile("^ */(?<name>[A-Za-z0-9_]+)(@(?<bot>[A-Za-z0-9_]+))?(?<args>( +([^ ]+))+)? *$");
    private final Pattern argsPattern = Pattern.compile(" +(?<arg>[^ ]+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Map<String, CommandFunction> commandHandlers = new HashMap<String, CommandFunction>() {{
        put("menu", CommandServiceImpl.this::menu);
        put("vote", CommandServiceImpl.this::vote);
        put("revoke", CommandServiceImpl.this::revoke);
        put("winner", CommandServiceImpl.this::winner);
    }};
    private final Map<String, Function<ChatMember, User>> chatMemberExtractors = new HashMap<String, Function<ChatMember, User>>() {{
        put("creator", x -> ((ChatMemberOwner)x).getUser());
        put("administrator", x -> ((ChatMemberAdministrator)x).getUser());
        put("member", x -> ((ChatMemberMember)x).getUser());
        put("restricted", x -> ((ChatMemberRestricted)x).getUser());
        put("left", x -> ((ChatMemberLeft)x).getUser());
        put("kicked", x -> ((ChatMemberBanned)x).getUser());
    }};

    private User getChatMemberUser(ChatMember chatMember) {
        return chatMemberExtractors.get(chatMember.getStatus()).apply(chatMember);
    }
    private User getChatMemberUser(Bot bot, Chat chat, long userId) throws TelegramApiException {
        return getChatMemberUser(bot.execute(new GetChatMember(chat.getId().toString(), userId)));
    }
    private User getChatMemberUserUnchecked(Bot bot, Chat chat, long userId) {
        try {
            return getChatMemberUser(bot, chat, userId);
        } catch (TelegramApiException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    private SendMessage createSendMessage(Chat chat, String text) {
        SendMessage sendMessage = new SendMessage(chat.getId().toString(), text);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    private BotApiMethod<?> menu(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        SendMessage sendMessage = createSendMessage(chat, resourceBundle.getString("menu_message"));
        sendMessage.setReplyMarkup(keyboardService.createInlineKeyboard());
        return sendMessage;
    }
    private BotApiMethod<?> vote(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) throws TelegramApiException {
        User nominee = reply == null ? null : reply.getFrom();
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        String dateText = dateTimeFormatter.format(date);
        String text;
        if (nominee != null) {
            boolean success = voteService.vote(date, sender.getId(), nominee.getId(), chat.getId());
            if (success) {
                text = String.format(
                        resourceBundle.getString("vote_action_message"),
                        sender.getId(),
                        formatService.getUserName(sender),
                        nominee.getId(),
                        formatService.getUserName(nominee)
                );
            } else {
                text = String.format(
                        resourceBundle.getString("vote_action_denied_message"),
                        sender.getId(),
                        formatService.getUserName(sender),
                        dateText
                );
            }
        } else {
            Vote vote = voteService.getVote(date, sender.getId(), chat.getId());
            String nomineeText;
            if (vote == null) {
                nomineeText = resourceBundle.getString("view_vote_message_not_voted");
            } else {
                nominee = getChatMemberUser(bot, chat, vote.getNomineeId());
                nomineeText = String.format(resourceBundle.getString("view_vote_message_voted"), nominee.getId(), formatService.getUserName(nominee));
            }
            text = String.format(
                    resourceBundle.getString("view_vote_message"),
                    sender.getId(),
                    formatService.getUserName(sender),
                    dateText,
                    nomineeText
            );
        }
        return createSendMessage(
                chat,
                text
        );
    }
    private BotApiMethod<?> revoke(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        boolean success = voteService.revoke(date, sender.getId(), chat.getId());
        String textFormat = success ? resourceBundle.getString("revoke_message") : resourceBundle.getString("unable_to_revoke_message");
        String text = String.format(textFormat, sender.getId(), formatService.getUserName(sender));
        return createSendMessage(
                chat,
                text
        );
    }
    private BotApiMethod<?> winner(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        List<Long> winnerIds = voteService.winner(date, chat.getId());
        List<User> winners = winnerIds.stream().map(id -> getChatMemberUserUnchecked(bot, chat, id)).collect(Collectors.toList());
        String text = null;
        String dateText = dateTimeFormatter.format(date);
        if (winners.size() == 0) {
            text = String.format(resourceBundle.getString("no_winner_message"), dateText);
        }
        if (winners.size() == 1) {
            User winner = winners.get(0);
            text = String.format(resourceBundle.getString("single_winner_message"), dateText, winner.getId(), formatService.getUserName(winner));
        }
        if (winners.size() > 1) {
            text = String.format(resourceBundle.getString("multiple_winners_message"), dateText, usersToString(winners, resourceBundle.getString("multiple_winners_item")));
        }
        return createSendMessage(
                chat,
                text
        );
    }

    private String usersToString(List<User> users, String pattern) {
        StringBuilder result = new StringBuilder();
        for (User user : users) {
            String item = String.format(pattern, user.getId(), formatService.getUserName(user));
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(item);
        }
        return result.toString();
    }

    @Override
    public BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle) {
        try {
            Command command = parse(text);
            if (command == null || command.getBot() != null && !config.getBotUsername().equals(command.getBot())) {
                return null;
            }
            CommandFunction func = commandHandlers.get(command.getName());
            if (func != null) {
                return func.apply(bot, instant, chat, sender, reply, command, resourceBundle);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Command parse(String text) {
        Matcher mainMatcher = mainPattern.matcher(text);
        boolean found = mainMatcher.find();
        if (!found) {
            return null;
        }
        String name = mainMatcher.group("name");
        String bot = mainMatcher.group("bot");
        List<String> args = new ArrayList<>();
        String argsText = mainMatcher.group("args");
        if (argsText != null) {
            Matcher argsMatcher = argsPattern.matcher(argsText);
            while (argsMatcher.find()) {
                args.add(argsMatcher.group("arg"));
            }
        }
        return new Command(name, bot, args);
    }
}
