package ru.stn.telegram.govnoed.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TelegramConfig {
    @Value("${bot.webhook-path}")
    private String webhookPath;
    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
}
