package ru.stn.telegram.govnoed.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long voteId;
    private LocalDate date;
    private long chatId;
    private long senderId;
    private long nomineeId;

    public Vote(LocalDate date, long chatId, long senderId, long nomineeId) {
        this.date = date;
        this.chatId = chatId;
        this.senderId = senderId;
        this.nomineeId = nomineeId;
    }
}
