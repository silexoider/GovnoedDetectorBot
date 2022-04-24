package ru.stn.telegram.govnoed.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stn.telegram.govnoed.entities.Vote;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends CrudRepository<Vote, Long> {
    Optional<Vote> findFirstByDateAndSenderIdAndChatId(LocalDate date, long senderId, long chatId);
    @Query("select nomineeId, Count(1) from Vote where date = :date and chatId = :chatId group by nomineeId order by Count(1) desc")
    List<Object[]> findScores(LocalDate date, long chatId);
}
