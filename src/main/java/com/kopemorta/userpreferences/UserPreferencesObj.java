package com.kopemorta.userpreferences;

import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class UserPreferencesObj {

    private static final Logger LOGGER = Logger.getLogger(UserPreferencesObj.class.getName());


    private final UserPreferences userPreferences;
    private final List<RegisterOption> registerOptions;

    private final ReentrantLock lock;

    @Getter(AccessLevel.PACKAGE)
    private int lashHashCode;
    @Getter(AccessLevel.PACKAGE)
    private long lastModified;


    UserPreferencesObj(final UserPreferences userPreferences,
                       final RegisterOption[] registerOptions) {

        if (registerOptions == null || registerOptions.length <= 0)
            throw new IllegalArgumentException("Bad RegisterOptions.");

        this.userPreferences = userPreferences;
        this.registerOptions = new ArrayList<>();
        for(RegisterOption registerOption : registerOptions) {
            final Optional<RegisterOption> alreadyPresentOpt = haveRegisterOption(registerOption);
            //noinspection StatementWithEmptyBody
            if(alreadyPresentOpt.isPresent()) {
                // just ignore duplicate option
            } else {
                this.registerOptions.add(registerOption);
            }
        }

        this.lock = new ReentrantLock();
        this.lashHashCode = -1;
        this.lastModified = -1L;
    }


    int getCurrentHashCode() {
        return userPreferences.hashCode();
    }

    long getCurrentFileLastModified() {
        return userPreferences.userPreferencesFile().lastModified();
    }

    boolean fileExists() {
        return userPreferences.userPreferencesFile().exists();
    }

    Optional<RegisterOption> haveRegisterOption(final RegisterOption registerOption) {
        for (RegisterOption option : registerOptions) {
            if(option.getClass().isInstance(registerOption))
                return Optional.of(option);
        }

        return Optional.empty();
    }


    void updateToFile() {
        lock.lock(); // что бы не загружать файл с диска и сразу же его обновлять
        try {
            { // Объект в строку и её на диск
                final String objInStr = UserPreferencesController.CONFIG.getGson().toJson(userPreferences, userPreferences.getClass());
                try (FileWriter fw = new FileWriter(userPreferences.userPreferencesFile())) {
                    fw.write(objInStr);
                }
            }

            { // Обновляем метки
                this.lashHashCode = this.userPreferences.hashCode();
                this.lastModified = this.userPreferences.userPreferencesFile().lastModified();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Saving object exception", e);
        } finally {
            lock.unlock();
        }
    }

    void updateFromFile() {
        lock.lock(); // что бы не загружать файл с диска и сразу же его обновлять
        try {
            UserPreferences userPreferences;
            // Читаем файл и преобразуем в объект
            try (JsonReader jsonReader = new JsonReader(new FileReader(this.userPreferences.userPreferencesFile()))) {
                userPreferences = UserPreferencesController.CONFIG.getGson().fromJson(jsonReader, this.userPreferences.getClass());
            }


            // Если что-то пошло не так (иногда бывает)
            if (userPreferences == null) {
                LOGGER.log(Level.INFO, "On updating object from file received empty data");
                return;
            }

            this.updateFields(userPreferences);

            { // Обновляем метки
                this.lashHashCode = this.userPreferences.hashCode();
                this.lastModified = this.userPreferences.userPreferencesFile().lastModified();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Updating object exception", e);
        } finally {
            lock.unlock();
        }
    }


    /*
     * Тут происходит обновление полей текущего объекта и всех его родителей.
     * Поле не будет обновленно если оно содержит один из следующих модификаторов:
     * transient, final, static
     */
    private void updateFields(final UserPreferences userPreferences) throws IllegalAccessException {
        for (Class<?> thisCurrentClass = this.userPreferences.getClass();
             thisCurrentClass != null; thisCurrentClass = thisCurrentClass.getSuperclass()) {
            final Field[] fields = thisCurrentClass.getDeclaredFields();
            for (final Field field : fields) {
                if (Modifier.isTransient(field.getModifiers()) ||
                        Modifier.isFinal(field.getModifiers()) ||
                        Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);


                final Object newValue = field.get(userPreferences);
                field.set(this.userPreferences, newValue);
            }
        }
    }

}
