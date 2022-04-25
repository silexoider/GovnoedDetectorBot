package ru.stn.telegram.govnoed.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.stn.telegram.govnoed.entities.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {
}
