package ru.stn.telegram.govnoed.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stn.telegram.govnoed.entities.Setting;
import ru.stn.telegram.govnoed.repositories.SettingRepository;
import ru.stn.telegram.govnoed.services.SettingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final SettingRepository settingRepository;

    private static <T, R> R callSafe(T object, Function<T, R> func) {
        if (object == null) {
            return null;
        } else {
            return func.apply(object);
        }
    }
    private <T> T getSettingValue(String name, Function<Setting, T> getter) {
        return callSafe(getSettingByName(name), getter);
    }
    private <T> void setSettingValue(String name, BiConsumer<Setting, T> setter, T value) {
        Setting setting = getSettingByName(name);
        if (setting == null) {
            setting = new Setting(name);
        }
        setter.accept(setting, value);
        settingRepository.save(setting);
    }

    @Override
    public Setting getSettingByName(String name) {
        Optional<Setting> setting = settingRepository.findById(name);
        return setting.orElse(null);
    }

    @Override
    public Integer getIntValue(String name) {
        return getSettingValue(name, Setting::getIntValue);
    }
    @Override
    public String getStringValue(String name) {
        return getSettingValue(name, Setting::getStringValue);
    }
    @Override
    public Double getDoubleValue(String name) {
        return getSettingValue(name, Setting::getDoubleValue);
    }
    @Override
    public Boolean getBooleanValue(String name) {
        return getSettingValue(name, Setting::isBooleanValue);
    }
    @Override
    public LocalDateTime getDateTimeValue(String name) {
        return getSettingValue(name, Setting::getDateTimeValue);
    }

    @Override
    public void setIntValue(String name, int value) {
        setSettingValue(name, Setting::setIntValue, value);
    }
    @Override
    public void setStringValue(String name, String value) {
        setSettingValue(name, Setting::setStringValue, value);
    }
    @Override
    public void setDoubleValue(String name, double value) {
        setSettingValue(name, Setting::setDoubleValue, value);
    }
    @Override
    public void setBooleanValue(String name, boolean value) {
        setSettingValue(name, Setting::setBooleanValue, value);
    }
    @Override
    public void setDateTimeValue(String name, LocalDateTime value) {
        setSettingValue(name, Setting::setDateTimeValue, value);
    }

    @Override
    public Setting getRigging() {
        return getSettingByName("rigging");
    }
    @Override
    public Boolean getRiggingValue() {
        return Objects.requireNonNullElse(getBooleanValue("rigging"), false);
    }
    @Override
    public void setRiggingValue(boolean value) {
        setBooleanValue("rigging", value);
    }

}
