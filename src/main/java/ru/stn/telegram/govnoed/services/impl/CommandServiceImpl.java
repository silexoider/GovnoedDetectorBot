package ru.stn.telegram.govnoed.services.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.handling.MapHandler;
import ru.stn.telegram.govnoed.services.*;
import ru.stn.telegram.govnoed.telegram.Bot;
import ru.stn.telegram.govnoed.telegram.Config;

import java.time.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommandServiceImpl extends MapHandler<String, CommandServiceImpl.Args, BotApiMethod<?>> implements CommandService  {
    @FunctionalInterface
    protected interface EntryFunction {
        BotApiMethod<?> apply(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) throws TelegramApiException;
    }

    @Data
    @AllArgsConstructor
    public static class Args {
        private Bot bot;
        private Instant instant;
        private Chat chat;
        private User sender;
        private String text;
        private Message reply;
        private Command command;
        private ResourceBundle resourceBundle;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Command {
        private final String name;
        private final String bot;
        private final List<String> args;
    }

    private final Config config;
    private final ChatService chatService;
    private final ActionService actionService;
    private final FormatService formatService;
    private final SendMessageService sendMessageService;
    private final LocalizationService localizationService;

    private final Pattern mainPattern = Pattern.compile("^ */(?<name>[A-Za-z0-9_]+)(@(?<bot>[A-Za-z0-9_]+))?(?<args>( +([^ ]+))+)? *$");
    private final Pattern argsPattern = Pattern.compile(" +(?<arg>[^ ]+)");

    private final Map<String, EntryFunction> commandMap = new HashMap<>() {{
        put("start", CommandServiceImpl.this::menu);
        put("menu", CommandServiceImpl.this::menu);
        put("zone", CommandServiceImpl.this::zone);
        put("vote", CommandServiceImpl.this::vote);
        put("revoke", CommandServiceImpl.this::revoke);
        put("winner", CommandServiceImpl.this::winner);
        put("scores", CommandServiceImpl.this::scores);
        put("rigging", CommandServiceImpl.this::rigging);
    }};

    private static BiFunction<String, Args, BotApiMethod<?>> normalize(EntryFunction function) {
        return
                (k, a) -> {
                    try {
                        return function.apply(a.getBot(), a.getInstant(), a.getChat(), a.getSender(), a.getReply(), a.getCommand(), a.getResourceBundle());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                };
    }

    public CommandServiceImpl(
            Config config,
            ChatService chatService,
            ActionService actionService,
            FormatService formatService,
            SendMessageService sendMessageService,
            LocalizationService localizationService) {
        init(commandMap.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), normalize(e.getValue()))).collect(Collectors.toList()));
        this.config = config;
        this.chatService = chatService;
        this.actionService = actionService;
        this.formatService = formatService;
        this.sendMessageService = sendMessageService;
        this.localizationService = localizationService;
    }

    private LocalDate getDateWithArgs(LocalDate date, List<String> args) {
        if (args.size() > 0) {
            String text = args.get(0);
            date = formatService.parseDate(text);
        }
        return date;
    }
    private BotApiMethod<?> runFuncWithDateArg(LocalDate date, Chat chat, Command command, ResourceBundle resourceBundle, Function<LocalDate, BotApiMethod<?>> func) {
        date = getDateWithArgs(date, command.getArgs());
        if (date == null) {
            return sendMessageService.createSendMessage(chat, String.format(localizationService.getUnableToRecognizeDateMessage(resourceBundle), command.getArgs().get(0)));
        }
        return func.apply(date);
    }

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
        return actionService.revoke(chatService.instantToDate(chat.getId(), instant), chat ,sender, resourceBundle);
    }
    private BotApiMethod<?> winner(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return runFuncWithDateArg(
                chatService.instantToDate(chat.getId(), instant).minusDays(1),
                chat,
                command,
                resourceBundle,
                date -> {
                    LocalDate now = Instant.now().atZone(chatService.getTimezoneById(chat.getId())).toLocalDate();
                    if (!date.isBefore(now)) {
                        return sendMessageService.createSendMessage(chat, localizationService.getInvalidWinnerDateMessage(resourceBundle));
                    }
                    return actionService.showWinners(bot, date, chat, resourceBundle);
                }
        );
    }
    private BotApiMethod<?> scores(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        return runFuncWithDateArg(
                chatService.instantToDate(chat.getId(), instant),
                chat,
                command,
                resourceBundle,
                (date) -> actionService.showScores(bot, date, chat, resourceBundle)
        );
    }
    private BotApiMethod<?> rigging(Bot bot, Instant instant, Chat chat, User sender, Message reply, Command command, ResourceBundle resourceBundle) {
        if (command.getArgs().size() == 0) {
            return actionService.getRigging(chat, sender, resourceBundle);
        } else {
            boolean rigging = Boolean.parseBoolean(command.getArgs().get(0));
            return actionService.setRigging(chat, sender, rigging, resourceBundle);
        }
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

    @Override
    public BotApiMethod<?> process(Bot bot, Instant instant, Chat chat, User sender, String text, Message reply, ResourceBundle resourceBundle) {
        Command command = parse(text);
        if (command == null || command.getBot() != null && !config.getBotUsername().equals(command.getBot())) {
            return null;
        }
        return handle(command.getName(), new Args(bot, instant, chat, sender, text, reply, command, resourceBundle));
    }
}
