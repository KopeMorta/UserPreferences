package com.kopemorta.userpreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.kopemorta.userpreferences.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UserPreferences {

    private static final Logger LOGGER = Logger.getLogger(UserPreferences.class.getName());

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ScheduledExecutorService executor = Util.createScheduledService("User Preferences Updater");
    private static final List<Runnable> tasks = new ArrayList<>();


    private transient ReentrantLock lock = new ReentrantLock();
    private transient Condition condition = lock.newCondition();

    private transient int lashHashCode = -1;
    private transient long lastModified = -1L;


    private Runnable updateToFileTask() {
        return () -> {
            lock.lock();
            try {
                final String objInStr = GSON.toJson(this, this.getClass());
                try (FileWriter fw = new FileWriter(userPreferencesFile())) {
                    fw.write(objInStr);
                }

                this.lashHashCode = this.hashCode();
                this.lastModified = this.userPreferencesFile().lastModified();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Saving object exception", e);
            } finally {
                lock.unlock();
            }
        };
    }

    private Runnable updateFromFileTask() {
        return () -> {
            lock.lock();
            try {
                UserPreferences userPreferencesObj;
                try (JsonReader jsonReader = new JsonReader(new FileReader(userPreferencesFile()))) {
                    userPreferencesObj = GSON.fromJson(jsonReader, this.getClass());
                }

                if (userPreferencesObj == null) {
                    LOGGER.log(Level.WARNING, "On updating object from file received empty data");
                    return;
                }

                this.updateFields(userPreferencesObj);

                this.lashHashCode = this.hashCode();
                this.lastModified = this.userPreferencesFile().lastModified();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Updating object exception", e);
            } finally {
                lock.unlock();
            }
        };
    }

    public void updateFields(final UserPreferences newUserPreferences) throws IllegalAccessException {
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

    protected abstract File userPreferencesFile();

    public abstract int hashCode();

    public abstract boolean equals();
}
