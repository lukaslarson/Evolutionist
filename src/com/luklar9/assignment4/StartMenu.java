package com.luklar9.assignment4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class StartMenu extends Activity
{
    /** Called when the activity is first created. */
    private static WindowManager wm;
    private static TextView timeTV;
    private EditText nameET;
    private boolean enabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        wm = getWindowManager();

        nameET = (EditText)findViewById(R.id.name);
        timeTV = (TextView) findViewById(R.id.time);
        nameET.setOnKeyListener(onSoftKeyboardDonePress);
    }

    // fix DONE button on the text input
    private View.OnKeyListener onSoftKeyboardDonePress=new View.OnKeyListener()
    {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);
            }
            return false;
        }
    };

    // get screen size
    public static void updateDM (DisplayMetrics dm){
        wm.getDefaultDisplay().getMetrics(dm);
    }

    @Override
    public boolean onTouchEvent( MotionEvent e )
    {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            // start game over activity
            Intent i = new Intent();
            i.setClassName("com.luklar9.assignment4", "com.luklar9.assignment4.Game");
            Bundle b = new Bundle();
            b.putString("name", "" + nameET.getText());
            i.putExtras(b);
            this.startActivity(i);
        }

        return true;
    }
}
