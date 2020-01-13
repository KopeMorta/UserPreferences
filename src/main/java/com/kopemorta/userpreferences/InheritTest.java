package com.kopemorta.userpreferences;

public class InheritTest extends Test {

    private int test4 = 4;
    private String test5 = "5";
    private long test6 = 6L;


    public static void main(String[] args) throws Throwable {
        final InheritTest firstInheritedObj = new InheritTest();
        final InheritTest newInheritedObj = new InheritTest();
        newInheritedObj.test1 = 11;
        newInheritedObj.test2 = "22";
        newInheritedObj.test3 = 33L;
        newInheritedObj.test4 = 5;
        newInheritedObj.test5 = "6";
        newInheritedObj.test6 = 7L;

        firstInheritedObj.updateFields(newInheritedObj);

        System.out.println(firstInheritedObj.test1);
        System.out.println(firstInheritedObj.test2);
        System.out.println(firstInheritedObj.test3);
        System.out.println(firstInheritedObj.test4);
        System.out.println(firstInheritedObj.test5);
        System.out.println(firstInheritedObj.test6);
    }
}
