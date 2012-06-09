package com.luklar9.assignment4;

final class finals {
    private static String name;
    private static int score;

    public static String getName() {
        return finals.name;
    }

    public static void setName(String n) {
        if (!n.equals(""))
            finals.name = n;
        else {
            finals.name = "Player";
        }
    }

    public static int getScore() {
        return score;
    }

    public static void setScore(Integer s) {
        score = s;
    }
}
