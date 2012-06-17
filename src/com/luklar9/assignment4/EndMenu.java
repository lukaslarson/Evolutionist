package com.luklar9.assignment4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class EndMenu extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore);

        TextView yourScore = (TextView) findViewById(R.id.yourscore);
        TextView hs1 = (TextView) findViewById(R.id.hs1);
        TextView hs2 = (TextView) findViewById(R.id.hs2);
        TextView hs3 = (TextView) findViewById(R.id.hs3);
        TextView hs4 = (TextView) findViewById(R.id.hs4);
        TextView hs5 = (TextView) findViewById(R.id.hs5);

        // get the data from the intent
        Intent i = getIntent();

        // get the highscores
        DatabaseHandler db = new DatabaseHandler(this);
        db.addHighScore(new HighScore(i.getExtras().getString("name"), i.getExtras().getInt("time")));
        List<HighScore> highScoreList = db.getSortedHighScores();

        // populate the textviews
        switch (highScoreList.size()) {
            case 5: hs5.setText("5: " + highScoreList.get(4).getScore() + "s" + " - " + highScoreList.get(4).getName());
            case 4: hs4.setText("4: " + highScoreList.get(3).getScore() + "s" + " - " + highScoreList.get(3).getName());
            case 3: hs3.setText("3: " + highScoreList.get(2).getScore() + "s" + " - " + highScoreList.get(2).getName());
            case 2: hs2.setText("2: " + highScoreList.get(1).getScore() + "s" + " - " + highScoreList.get(1).getName());
            case 1: hs1.setText("1: " + highScoreList.get(0).getScore() + "s" + " - " + highScoreList.get(0).getName());
                break;
            default: break;
        }
        yourScore.setText("Your time: " + i.getExtras().getInt("time") + "secs");
    }
}
