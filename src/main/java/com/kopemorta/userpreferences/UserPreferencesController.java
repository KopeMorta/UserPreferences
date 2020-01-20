package com.kopemorta.userpreferences;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class UserPreferencesController {

    private static final Logger LOGGER = Logger.getLogger(UserPreferencesController.class.getName());


    private static final List<UserPreferencesObj> USER_PREFERENCES_OBJS = new CopyOnWriteArrayList<>();
    static Config CONFIG;


    static void init(final Config config) {
        if (CONFIG != null) // Инициализировать можно только 1 раз :|
            return;

        CONFIG = config;
        CONFIG.getScheduledExecutor() // запуск демона
                .scheduleWithFixedDelay(UserPreferencesController::scheduledTask,
                        CONFIG.getInitialDelay(),
                        CONFIG.getDelay(),
                        TimeUnit.MILLISECONDS);
    }


    static void registerInstance(final UserPreferencesObj userPreferencesObj) {
        if (CONFIG == null) // Если не был задан кастомный конфиг - создаём дефолтный
            init(Config.builder().build());


        /*
         * Первоначальная инициализация объекта.
         * Загрузка его с диска или наоборот, сохранение на диск.
         */
        {
            update(userPreferencesObj);
        }


        USER_PREFERENCES_OBJS.add(userPreferencesObj); // добавление объекта в список обновляемых
    }


    /*
     * Основная логика.
     * Проходим по списку с экземплярами, смотрим обновились они или их файл на диске.
     */
    private static void scheduledTask() {
        for (UserPreferencesObj userPreferencesObj : USER_PREFERENCES_OBJS) {
            update(userPreferencesObj);
        }
    }

    private static void update(final UserPreferencesObj userPreferencesObj) {
        try {
            // если файл на диске новей чем в памяти и он существует - загружаем его с диска
            if (userPreferencesObj.getFileLastModified() < userPreferencesObj.getCurrentFileLastModified())
                if (userPreferencesObj.fileExists())
                    userPreferencesObj.updateFromFile();


            // если экземпляр обновился - сохраняем новую версию в файле
            if (userPreferencesObj.getLashHashCode() != userPreferencesObj.getCurrentHashCode())
                userPreferencesObj.updateToFile();
        } catch (Exception e) { // Если не поймать ошибку о ней никогда и не узнать
            LOGGER.log(Level.SEVERE, "Unknown exception", e);
        }
    }
}
