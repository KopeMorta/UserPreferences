package com.kopemorta.userpreferences;

import java.io.File;

public class Test extends UserPreferences {
    private static final File prefFile = new File("test.prefs");


    public int test1 = 1;
    public String test2 = "2";
    public long test3 = 3L;


    public static void main(String[] args) throws Throwable {
        final Test firstObj = new Test();
        final Test newObj = new Test();
        newObj.test1 = 2;
        newObj.test2 = "3";
        newObj.test3 = 4L;

//        firstObj.updateFields(newObj);

        System.out.println(newObj.test1);
        System.out.println(newObj.test2);
        System.out.println(newObj.test3);
    }


    protected File userPreferencesFile() {
        return prefFile;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
