package ru.stn.telegram.govnoed.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.stn.telegram.govnoed.services.CommandService;
import ru.stn.telegram.govnoed.services.FormatService;
import ru.stn.telegram.govnoed.services.KeyboardService;
import ru.stn.telegram.govnoed.services.VoteService;
import ru.stn.telegram.govnoed.telegram.Bot;

@Configuration
@AllArgsConstructor
public class SpringConfig {
    private final TelegramConfig telegramConfig;
    private final VoteService statEntryService;
    private final CommandService botCommandService;
    private final KeyboardService keyboardService;
    private final FormatService formatService;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    public Bot springWebhookBot(SetWebhook setWebhook) {
        return new Bot(setWebhook, telegramConfig, statEntryService, botCommandService, keyboardService, formatService);
    }
}
