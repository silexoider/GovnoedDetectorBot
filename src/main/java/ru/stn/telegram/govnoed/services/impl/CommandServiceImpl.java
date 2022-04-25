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
import ru.stn.telegram.govnoed.services.*;
import ru.stn.telegram.govnoed.telegram.Bot;
import ru.stn.telegram.govnoed.config.TelegramConfig;

import java.time.*;
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
    private final ChatService chatService;
    private final ActionService actionService;

    private final Pattern mainPattern = Pattern.compile("^ */(?<name>[A-Za-z0-9_]+)(@(?<bot>[A-Za-z0-9_]+))?(?<args>( +([^ ]+))+)? *$");
    private final Pattern argsPattern = Pattern.compile(" +(?<arg>[^ ]+)");

    private final Map<String, CommandFunction> commandHandlers = new HashMap<String, CommandFunction>() {{
        put("start", CommandServiceImpl.this::menu);
        put("menu", CommandServiceImpl.this::menu);
        put("zone", CommandServiceImpl.this::zone);
        put("vote", CommandServiceImpl.this::vote);
        put("revoke", CommandServiceImpl.this::revoke);
        put("winner", CommandServiceImpl.this::winner);
    }};

    private BotApiMethod<?> menu(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return actionService.showMenu(chat, resourceBundle);
    }
    private BotApiMethod<?> zone(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return
                command.getArgs().size() == 0 ?
                        actionService.showTimezone(chat, resourceBundle)
                        :
                        actionService.setTimezone(bot, chat, sender, command.getArgs().get(0), resourceBundle);
    }
    private BotApiMethod<?> vote(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) throws TelegramApiException {
        User nominee = reply == null ? null : reply.getFrom();
        LocalDate date = instant.atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
        return
                nominee == null ?
                        actionService.showVote(bot, date, chat, sender, resourceBundle)
                        :
                        actionService.vote(date, chat, sender, reply, resourceBundle);
    }
    private BotApiMethod<?> revoke(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return actionService.revoke(instant, chat ,sender, resourceBundle);
    }
    private BotApiMethod<?> winner(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return actionService.showWinners(bot, instant, chat, resourceBundle);
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
