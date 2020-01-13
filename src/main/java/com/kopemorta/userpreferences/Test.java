package com.kopemorta.userpreferences;

import java.io.File;

public class Test extends UserPreferences {
    private static final File prefFile = new File("test.prefs");



    protected File userPreferencesFile() {
        return prefFile;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals() {
        return false;
    }
}
