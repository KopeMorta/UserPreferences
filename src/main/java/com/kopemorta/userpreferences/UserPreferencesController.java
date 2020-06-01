package com.kopemorta.userpreferences;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.kopemorta.userpreferences.DefaultRegisterOptions.LOAD_FROM_DISK;
import static com.kopemorta.userpreferences.DefaultRegisterOptions.SAVE_ON_DISK;

class UserPreferencesController {

    private static final Logger LOGGER = Logger.getLogger(UserPreferencesController.class.getName());


    private static final List<UserPreferencesObjHolder> USER_PREFERENCES_OBJS = new CopyOnWriteArrayList<>();
    static Config CONFIG;


    static void init(final Config config) {
        if (CONFIG != null) // Инициализировать можно только 1 раз
            return;

        CONFIG = config;
        CONFIG.getScheduledExecutor() // запуск демона
                .scheduleWithFixedDelay(UserPreferencesController::scheduledTask,
                        CONFIG.getInitialDelay(),
                        CONFIG.getDelay(),
                        TimeUnit.MILLISECONDS);
    }


    static synchronized void registerInstance(final UserPreferencesObj userPreferencesObj) {
        if (CONFIG == null) // Если не был задан кастомный конфиг - создаём дефолтный
            init(Config.builder().build());

        final UserPreferencesObjHolder holder = new UserPreferencesObjHolder(userPreferencesObj);

        final Optional<RegisterOption> loadFromDiskOpt = userPreferencesObj.haveRegisterOption(LOAD_FROM_DISK);
        if (loadFromDiskOpt.isPresent()) {
            final Runnable loadFromDiskRunnable = () -> updateFromFile(holder.getUserPreferencesObj());
            if (loadFromDiskOpt.get().repeatable())
                holder.addRepeatableTask(loadFromDiskRunnable);
            else
                holder.addNotRepeatableTask(loadFromDiskRunnable);
        }

        final Optional<RegisterOption> saveOnDiskOpt = userPreferencesObj.haveRegisterOption(SAVE_ON_DISK);
        if (saveOnDiskOpt.isPresent()) {
            final Runnable saveOnDiskRunnable = () -> updateToFile(holder.getUserPreferencesObj());
            if (saveOnDiskOpt.get().repeatable())
                holder.addRepeatableTask(saveOnDiskRunnable);
            else
                holder.addNotRepeatableTask(saveOnDiskRunnable);
        }


        update(holder);


        if (holder.haveRepeatableTasks())
            USER_PREFERENCES_OBJS.add(holder);
    }


    /*
     * Основная логика.
     * Проходим по списку с экземплярами, смотрим обновились они или их файл на диске.
     */
    private static void scheduledTask() {
        for (UserPreferencesObjHolder holder : USER_PREFERENCES_OBJS) {
            try {
                update(holder);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unknown exception", e);
            }
        }
    }


    private static void update(final UserPreferencesObjHolder holder) {
        for (Runnable runnable : holder.getNotRepeatableTasks()) {
            runnable.run();
        }

        for (Runnable runnable : holder.getRepeatableTasks()) {
            runnable.run();
        }
    }

    private static void updateFromFile(final UserPreferencesObj userPreferencesObj) {
        if (userPreferencesObj.getLastModified() < userPreferencesObj.getCurrentFileLastModified())
            if (userPreferencesObj.fileExists())
                userPreferencesObj.updateFromFile();
    }

    private static void updateToFile(final UserPreferencesObj userPreferencesObj) {
        if (userPreferencesObj.getLashHashCode() != userPreferencesObj.getCurrentHashCode())
            userPreferencesObj.updateToFile();
    }


    @Value
    private static class UserPreferencesObjHolder {
        UserPreferencesObj userPreferencesObj;
        List<Runnable> repeatableTasks = new ArrayList<>();
        List<Runnable> notRepeatableTasks = new ArrayList<>();

        private void addRepeatableTask(final Runnable runnable) {
            repeatableTasks.add(runnable);
        }

        private void addNotRepeatableTask(final Runnable runnable) {
            notRepeatableTasks.add(runnable);
        }

        private boolean haveRepeatableTasks() {
            return repeatableTasks.size() > 0;
        }
    }
}
