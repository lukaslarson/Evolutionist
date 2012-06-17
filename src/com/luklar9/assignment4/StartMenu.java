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
    private EditText nameET;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        nameET = (EditText)findViewById(R.id.name);
        nameET.setOnKeyListener(onSoftKeyboardDonePress);
    }

    // fix DONE button on the text input
    private final View.OnKeyListener onSoftKeyboardDonePress=new View.OnKeyListener()
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

    @Override
    public boolean onTouchEvent( MotionEvent e )
    {
        // start game over activity on touch down
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
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
