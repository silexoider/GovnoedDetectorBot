package ru.stn.telegram.govnoed.services;

import ru.stn.telegram.govnoed.entities.Setting;

import java.time.LocalDateTime;

public interface SettingService {
    Setting getSettingByName(String name);

    Integer getIntValue(String name);
    String getStringValue(String name);
    Double getDoubleValue(String name);
    Boolean getBooleanValue(String name);
    LocalDateTime getDateTimeValue(String name);

    void setIntValue(String name, int value);
    void setStringValue(String name, String value);
    void setDoubleValue(String name, double value);
    void setBooleanValue(String name, boolean value);
    void setDateTimeValue(String name, LocalDateTime value);

    Setting getRigging();
    Boolean getRiggingValue();
    void setRiggingValue(boolean value);
}
