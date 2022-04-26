package ru.stn.telegram.govnoed.telegram;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Config {
    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
}
