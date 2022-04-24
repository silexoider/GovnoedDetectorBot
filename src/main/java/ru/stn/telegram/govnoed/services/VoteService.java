package ru.stn.telegram.govnoed.services;

import ru.stn.telegram.govnoed.entities.Vote;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface VoteService {
    boolean vote(LocalDate date, long senderId, long nomineeId, long chatId);
    Vote getVote(LocalDate date, long senderId, long chatId);
    boolean revoke(LocalDate date, long senderId, long chatId);
    List<Long> winner(LocalDate date, long chatId);
}
