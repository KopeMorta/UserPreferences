package com.kopemorta.userpreferences;

import java.io.File;

import static com.kopemorta.userpreferences.DefaultRegisterOptions.*;

public interface UserPreferences {

    static void init(final Config config) {
        UserPreferencesController.init(config);
    }


    /**
     * This method just one time (on call) load this preferences from disk.
     * If file not exists - do nothing.
     * @param instance UserPreferences instance
     * @param <T> object implements UserPreferences
     */
    static <T extends UserPreferences> void singleLoadFromDisk(final T instance) {
        final RegisterOption[] defaultOptions = new RegisterOption[]{SINGLE_LOAD_FROM_DISK};

        registerInstance(instance, defaultOptions);
    }

    /**
     * This method just one time (on call) save this preferences to disk.
     * @param instance UserPreferences instance
     * @param <T> object implements UserPreferences
     */
    static <T extends UserPreferences> void singleSaveOnDisk(final T instance) {
        final RegisterOption[] defaultOptions = new RegisterOption[]{SINGLE_SAVE_ON_DISK};

        registerInstance(instance, defaultOptions);
    }

    /**
     * This method one time (on call) load this preferences from disk and lifetime updating to disk.
     * @param instance UserPreferences instance
     * @param <T> object implements UserPreferences
     */
    static <T extends UserPreferences> void singleLoadFromDiskRepeatSaveOnDisk(final T instance) {
        final RegisterOption[] defaultOptions = new RegisterOption[]{SINGLE_LOAD_FROM_DISK, SAVE_ON_DISK};

        registerInstance(instance, defaultOptions);
    }

    /**
     * This method one time (on call) save this preferences to disk and lifetime updating from disk.
     * @param instance UserPreferences instance
     * @param <T> object implements UserPreferences
     */
    static <T extends UserPreferences> void singleSaveOnDiskRepeatLoadFromDisk(final T instance) {
        final RegisterOption[] defaultOptions = new RegisterOption[]{SINGLE_SAVE_ON_DISK, LOAD_FROM_DISK};

        registerInstance(instance, defaultOptions);
    }

    /**
     * Default register method with default register options.
     * This method register instance with lifetime updating to disk and from disk.
     * @param instance UserPreferences instance
     * @param <T> object implements UserPreferences
     */
    static <T extends UserPreferences> void registerInstance(final T instance) {
        final RegisterOption[] defaultOptions = new RegisterOption[]{LOAD_FROM_DISK, SAVE_ON_DISK};

        registerInstance(instance, defaultOptions);
    }


    /**
     * Register UserPreferences instance in controller with custom register options.
     * Inherited options must contains only one time. If have multiple inherited register options - only the first is saved.
     * @param instance UserPreferences instance
     * @param registerOptions register options
     * @param <T> object implements UserPreferences
     * @throws IllegalArgumentException if register options is null or length zero
     */
    static <T extends UserPreferences> void registerInstance(final T instance, final RegisterOption[] registerOptions) {
        final UserPreferencesObj userPreferencesObj = new UserPreferencesObj(instance, registerOptions);

        UserPreferencesController.registerInstance(userPreferencesObj);
    }


    // Файл с настройками, с ним и происходит вся работа
    File userPreferencesFile();

    int hashCode();

    boolean equals(Object obj);
}
