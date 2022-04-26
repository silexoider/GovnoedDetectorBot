package ru.stn.telegram.govnoed.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stn.telegram.govnoed.entities.Setting;

@Repository
public interface SettingRepository extends CrudRepository<Setting, String> {
}
