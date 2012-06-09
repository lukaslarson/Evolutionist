package com.luklar9.assignment4;

/**
 * Created with IntelliJ IDEA.
 * User: lukas
 * Date: 6/7/12
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class HighScore {

    private int id;
    private String name;
    private int score;

    public HighScore(){

    }

    public HighScore(String n, int s) {
        name = n;
        score = s;
    }

    public HighScore(int i, String n, int s) {
        id = i;
        name = n;
        score = s;
    }

    public int getId(){
        return id;
    }

    public void setId(int i){
        id = i;
    }

    public String getName(){
        return name;
    }

    public void setName(String n){
        name = n;
    }
    public int getScore() {
        return score;
    }

    public void setScore(int s) {
        score = s;
    }
}
