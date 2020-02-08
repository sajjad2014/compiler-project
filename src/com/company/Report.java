package com.company;

public class Report {
    public static void error(String massage, int exitCode){
        System.err.println(massage);
        System.exit(exitCode);
    }
}
