package ru.stn.telegram.govnoed.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stn.telegram.govnoed.entities.Vote;
import ru.stn.telegram.govnoed.repositories.VoteRepository;
import ru.stn.telegram.govnoed.services.VoteService;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;

    @Override
    public boolean vote(LocalDate date, long senderId, long nomineeId, long chatId) {
        Vote vote = getVote(date, senderId, chatId);
        if (vote != null) {
            return false;
        }
        vote = new Vote(date, chatId, senderId, nomineeId);
        voteRepository.save(vote);
        return true;
    }
    @Override
    public Vote getVote(LocalDate date, long senderId, long chatId) {
        Optional<Vote> vote = voteRepository.findFirstByDateAndSenderIdAndChatId(date, senderId, chatId);
        return vote.orElse(null);
    }
    @Override
    public boolean revoke(LocalDate date, long senderId, long chatId) {
        Vote vote = getVote(date, senderId, chatId);
        if (vote == null) {
            return false;
        }
        voteRepository.delete(vote);
        return true;
    }
    @Override
    public Winners winners(LocalDate date, long chatId) {
        List<Score> scores = scores(date, chatId);
        if (scores.size() == 0) {
            return new Winners(null, new LinkedList<>());
        } else {
            int maxScore = scores.get(0).getValue();
            return
                    new Winners(
                            maxScore,
                            scores.stream().filter(x -> x.getValue() == maxScore).map(Score::getUserId).collect(Collectors.toList())
                    );
        }
    }
    @Override
    public List<Score> scores(LocalDate date, long chatId) {
        return voteRepository.findScores(date, chatId).stream().map(x -> new Score((long)x[0], (int)(long)x[1])).collect(Collectors.toList());
    }
}
