package com.kopemorta.userpreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Builder
public class Config {
    @Builder.Default
    private ScheduledExecutorService scheduledExecutor = createScheduledService("UserPreferences Service");

    @Builder.Default
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .setLenient()
            .create();

    @Builder.Default
    private int initialDelay = 20;

    @Builder.Default
    private int delay = 100;



    private static ScheduledExecutorService createScheduledService(final String name) {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName(name);
            return t;
        });
    }
}
