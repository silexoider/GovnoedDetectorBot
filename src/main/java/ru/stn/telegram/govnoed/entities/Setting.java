package ru.stn.telegram.govnoed.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Setting {
    @Id
    private String name;
    private int intValue;
    private String stringValue;
    private double doubleValue;
    private boolean booleanValue;
    private LocalDateTime dateTimeValue;

    public Setting(String name) {
        this.name = name;
    }
}
