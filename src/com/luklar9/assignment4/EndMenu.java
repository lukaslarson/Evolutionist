package com.luklar9.assignment4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lukas
 * Date: 6/7/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class EndMenu extends Activity{

    private TextView hs1;
    private TextView hs2;
    private TextView hs3;
    private TextView hs4;
    private TextView hs5;
    private DatabaseHandler db;
    private int highScoreCount;
    private List<HighScore> highScoreList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore);

        hs1 = (TextView) findViewById(R.id.hs1);
        hs2 = (TextView) findViewById(R.id.hs2);
        hs3 = (TextView) findViewById(R.id.hs3);
        hs4 = (TextView) findViewById(R.id.hs4);
        hs5 = (TextView) findViewById(R.id.hs5);

        Intent i = getIntent();
        db = new DatabaseHandler(this);
        db.addHighScore(new HighScore(i.getExtras().getString("name"), i.getExtras().getInt("time")));
        highScoreList = db.getSortedHighScores();
        Log.w("Test", "3:" + highScoreList.size());

        switch (highScoreList.size()) {
            case 5: hs5.setText("5: " + highScoreList.get(4).getScore() + "s" + " - " + highScoreList.get(4).getName());
            case 4: hs4.setText("4: " + highScoreList.get(3).getScore() + "s" + " - " + highScoreList.get(3).getName());
            case 3: hs3.setText("3: " + highScoreList.get(2).getScore() + "s" + " - " + highScoreList.get(2).getName());
            case 2: hs2.setText("2: " + highScoreList.get(1).getScore() + "s" + " - " + highScoreList.get(1).getName());
            case 1: hs1.setText("1: " + highScoreList.get(0).getScore() + "s" + " - " + highScoreList.get(0).getName());
                break;
            default: break;
        }

        Log.w("TEST", "4:"+highScoreList.get(0).getScore());


    }
}
