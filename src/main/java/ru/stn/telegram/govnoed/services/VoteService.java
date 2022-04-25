package ru.stn.telegram.govnoed.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.stn.telegram.govnoed.entities.Vote;
import ru.stn.telegram.govnoed.services.impl.VoteServiceImpl;

import java.time.LocalDate;
import java.util.List;

public interface VoteService {
    @Getter
    @RequiredArgsConstructor
    class Winners {
        private final Integer score;
        private final List<Long> ids;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Score {
        private final long userId;
        private final int value;
    }


    boolean vote(LocalDate date, long senderId, long nomineeId, long chatId);
    Vote getVote(LocalDate date, long senderId, long chatId);
    boolean revoke(LocalDate date, long senderId, long chatId);
    Winners winners(LocalDate date, long chatId);
    List<Score> scores(LocalDate date, long chatId);
}
