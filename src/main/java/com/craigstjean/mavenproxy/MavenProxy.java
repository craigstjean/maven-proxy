package com.craigstjean.mavenproxy;

public class MavenProxy {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("USAGE: <configuration file>");
            System.exit(1);
        }

    }

}