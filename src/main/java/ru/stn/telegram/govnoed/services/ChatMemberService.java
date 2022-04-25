package ru.stn.telegram.govnoed.services;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.stn.telegram.govnoed.telegram.Bot;

public interface ChatMemberService {
    User getChatMemberUser(ChatMember chatMember);
    User getChatMemberUser(Bot bot, Chat chat, long userId);
    User getChatMemberUserUnchecked(Bot bot, Chat chat, long userId);
}
