package com.kopemorta.userpreferences.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Util {
    public static ScheduledExecutorService createScheduledService(String name) {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName(name);
            return t;
        });
    }
}
