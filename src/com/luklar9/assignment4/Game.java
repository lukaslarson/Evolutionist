package com.luklar9.assignment4;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.TextView;

public class Game extends Activity{

    private static TextView timeTV;
    private static String name = "Player";
    private static Context c;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        // start the thread
        CanvasThread.justStart();

        // initiate and get name from first screen
        timeTV = (TextView) findViewById(R.id.time);
        c = getApplicationContext();
        Intent i = getIntent();
        if (!i.getExtras().getString("name").equals("")) {
            name = i.getExtras().getString("name");
        }
    }

    // handler to update textviews from other thread, also ends game
    public static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // update the textview
            int time = msg.getData().getInt("time");
            timeTV.setText("Time: " + time + " secs");

            // if end
            if (msg.getData().getBoolean("end")) {

                // create an intent and set the name and score in a bundle
                Intent i = new Intent();
                i.setClassName("com.luklar9.assignment4", "com.luklar9.assignment4.EndMenu");
                Bundle b = new Bundle();
                b.putString("name", name);
                b.putInt("time", time);
                i.putExtras(b);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
            }

        }
    };

}
