package com.kopemorta.userpreferences;

import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UserPreferences {

    private static final Logger LOGGER = Logger.getLogger(UserPreferences.class.getName());

    private static Config CONFIG;
    private static final List<UserPreferences> INSTANCES_REGISTRY = new CopyOnWriteArrayList<>();


    private transient ReentrantLock lock = new ReentrantLock();
    private transient int lashHashCode = -1;
    private transient long lastModified = -1L;


    public static void init(final Config config) {
        if (CONFIG != null) // Инициализировать можно только 1 раз :|
            return;

        CONFIG = config;
        CONFIG.getScheduledExecutor() // запуск демона
                .scheduleWithFixedDelay(UserPreferences::scheduledTask,
                        CONFIG.getInitialDelay(),
                        CONFIG.getDelay(),
                        TimeUnit.MILLISECONDS);
    }

    public static void registerInstance(final UserPreferences instance) {
        if (CONFIG == null) // Если не был задан кастомный конфиг - создаём дефолтный
            init(Config.builder().build());

        INSTANCES_REGISTRY.add(instance);
    }


    /*
     * Основная логика.
     * Проходим по списку с экземплярами, смотрим обновились они или их файл на диске.
     */
    private static void scheduledTask() {
        try {
            for (UserPreferences instance : INSTANCES_REGISTRY) {
                // если файл на диске новей чем в памяти и он существует - загружаем его с диска
                if (instance.lastModified < instance.userPreferencesFile().lastModified())
                    if (instance.userPreferencesFile().exists())
                        instance.updateFromFile();


                // если экземпляр обновился - сохраняем новую версию в файле
                if (instance.lashHashCode != instance.hashCode())
                    instance.updateToFile();
            }
        } catch (Exception e) { // Если не поймать ошибку о ней никогда и не узнать
            LOGGER.log(Level.SEVERE, "Unknown exception", e);
        }
    }

    private void updateToFile() {
        lock.lock(); // что бы не загружать файл с диска и сразу же его обновлять
        try {
            { // Объект в строку и её на диск
                final String objInStr = CONFIG.getGson().toJson(this, this.getClass());
                try (FileWriter fw = new FileWriter(userPreferencesFile())) {
                    fw.write(objInStr);
                }
            }

            { // Обновляем метки
                this.lashHashCode = this.hashCode();
                this.lastModified = this.userPreferencesFile().lastModified();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Saving object exception", e);
        } finally {
            lock.unlock();
        }
    }

    private void updateFromFile() {
        lock.lock(); // что бы не загружать файл с диска и сразу же его обновлять
        try {
            UserPreferences userPreferencesObj;
            // Читаем файл и преобразуем в объект
            try (JsonReader jsonReader = new JsonReader(new FileReader(userPreferencesFile()))) {
                userPreferencesObj = CONFIG.getGson().fromJson(jsonReader, this.getClass());
            }


            // Если что-то пошло не так (иногда бывает)
            if (userPreferencesObj == null) {
                LOGGER.log(Level.INFO, "On updating object from file received empty data");
                return;
            }

            this.updateFields(userPreferencesObj);

            { // Обновляем метки
                this.lashHashCode = this.hashCode();
                this.lastModified = this.userPreferencesFile().lastModified();
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
    private void updateFields(final UserPreferences newUserPreferences) throws IllegalAccessException {
        Class<?> thisCurrentClass = this.getClass();
        Class<?> newCurrentClass = newUserPreferences.getClass();
        for (; thisCurrentClass != null && newCurrentClass != null;
             thisCurrentClass = thisCurrentClass.getSuperclass(), newCurrentClass = newCurrentClass.getSuperclass()) {

            final Field[] fields = thisCurrentClass.getDeclaredFields();
            for (final Field field : fields) {
                if (Modifier.isTransient(field.getModifiers()) ||
                        Modifier.isFinal(field.getModifiers()) ||
                        Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);


                final Object newValue = field.get(newUserPreferences);
                field.set(this, newValue);
            }
        }
    }


    // Файл с настройками, с ним и происходит вся работа
    protected abstract File userPreferencesFile();

    // Принудительное наследование, т.к. в этом класе этот метод активно используется
    public abstract int hashCode();

    public abstract boolean equals(Object obj);
}
