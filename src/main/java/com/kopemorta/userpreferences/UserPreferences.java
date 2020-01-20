package com.kopemorta.userpreferences;

import java.io.File;

public interface UserPreferences {

    static void init(final Config config) {
        UserPreferencesController.init(config);
    }


    /*
     * Получаем на вход инстанс для наблюдения, затем трансформируем его в вспомогательный объект и регистрируем его.
     */
    static <T extends UserPreferences> void registerInstance(final T instance) {
        final UserPreferencesObj userPreferencesObj = new UserPreferencesObj(instance);

        UserPreferencesController.registerInstance(userPreferencesObj);
    }


    // Файл с настройками, с ним и происходит вся работа
    File userPreferencesFile();

    int hashCode();

    boolean equals(Object obj);
}
