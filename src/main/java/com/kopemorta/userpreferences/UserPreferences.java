package com.kopemorta.userpreferences;

import com.kopemorta.userpreferences.util.Util;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

public abstract class UserPreferences {

    private static final ScheduledExecutorService executor = Util.createScheduledService("User Preferences Updater");

    protected abstract File userPreferencesFile();

    public abstract int hashCode();

    public abstract boolean equals();
}
