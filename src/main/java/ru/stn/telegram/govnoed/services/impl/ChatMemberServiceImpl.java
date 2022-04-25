package ru.stn.telegram.govnoed.services.impl;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.services.ChatMemberService;
import ru.stn.telegram.govnoed.telegram.Bot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class ChatMemberServiceImpl implements ChatMemberService {
    private final Map<String, Function<ChatMember, User>> chatMemberExtractors = new HashMap<String, Function<ChatMember, User>>() {{
        put("creator", x -> ((ChatMemberOwner)x).getUser());
        put("administrator", x -> ((ChatMemberAdministrator)x).getUser());
        put("member", x -> ((ChatMemberMember)x).getUser());
        put("restricted", x -> ((ChatMemberRestricted)x).getUser());
        put("left", x -> ((ChatMemberLeft)x).getUser());
        put("kicked", x -> ((ChatMemberBanned)x).getUser());
    }};

    @Override
    public User getChatMemberUser(ChatMember chatMember) {
        return chatMemberExtractors.get(chatMember.getStatus()).apply(chatMember);
    }
    @Override
    public User getChatMemberUser(Bot bot, Chat chat, long userId) throws TelegramApiException {
        return getChatMemberUser(bot.execute(new GetChatMember(chat.getId().toString(), userId)));
    }
    @Override
    public User getChatMemberUserUnchecked(Bot bot, Chat chat, long userId) {
        try {
            return getChatMemberUser(bot, chat, userId);
        } catch (TelegramApiException e) {
            throw new UncheckedExecutionException(e);
        }
    }
}
