package com.luklar9.assignment4;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class CanvasThread extends Thread {
    private static SurfaceHolder surfaceholder;
    private Panel panel;
    private boolean _run = false;
    private static boolean running = false;

    public CanvasThread(SurfaceHolder surfaceHolder, Panel panel) {
        this.surfaceholder = surfaceHolder;
        this.panel = panel;
    }

    public void setRunning(boolean run) {
        _run = run;
    }

    // starts/stops drawing
    public static void pause() {
        synchronized (surfaceholder) {
            if (running)  {
                running = false;
            }
            else {
                running = true;
            }
        }}

    // only starts drawing
    public static void justStart() {
        synchronized (surfaceholder) {
            running = true;
        }}

    @Override
    public void run() {
        Canvas c;
        while (_run) {
            c = null;
            try {
                // lock the canvas for drawing
                c = surfaceholder.lockCanvas();
                synchronized (surfaceholder) {
                    if (running) {
                        panel.onDraw(c);
                    }

                }
            }   finally {
                if (c != null) {
                    // unlock it and post to screen
                    surfaceholder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
