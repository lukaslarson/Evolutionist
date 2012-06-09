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

/**
 * Created with IntelliJ IDEA.
 * User: lukas
 * Date: 4/22/12
 * Time: 9:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game extends Activity{

    private static TextView timeTV;
    private static String name = "Player";
    public static Context c;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        CanvasThread.justStart();
        timeTV = (TextView) findViewById(R.id.time);
        c = getApplicationContext();
        Intent i = getIntent();
        if (!i.getExtras().getString("name").equals("")) {
            name = i.getExtras().getString("name");
        }
    }

    // handler to update textviews from other thread
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int time = msg.getData().getInt("time");
            timeTV.setText("Time: " + time + " secs");

            if (msg.getData().getBoolean("end")) {
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
