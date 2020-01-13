package com.kopemorta.userpreferences;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.File;

public class SimpleTest {

    public static void main(String[] args) throws InterruptedException {
        final TestPreferences testPreferences = new TestPreferences();
        UserPreferences.register(testPreferences);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(testPreferences.toString());
            }
        }).start();

        Thread.sleep(5000);

        testPreferences.timeout = 1_1324;
        testPreferences.firstName = "2_firsname";
        testPreferences.lastName = "3_lastname";
        testPreferences.id = 4_44;

        Thread.sleep(222222222);
    }


    @EqualsAndHashCode(callSuper = false)
    @ToString
    private static class TestPreferences extends UserPreferences {

        private long timeout;
        private String firstName;
        private String lastName;
        private int id;


        private TestPreferences() {
            this.timeout = 1000L;
            this.firstName = "MY_firstName";
            this.lastName = "MY_lastName";
            this.id = Integer.MAX_VALUE - 32134;
        }


        @Override
        protected File userPreferencesFile() {
            return new File("simpleTest.txt");
        }
    }
}
