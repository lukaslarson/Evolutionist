package com.luklar9.assignment4;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {

    private float touched_x = 0;
    private float touched_y = 0;
    private float screen_x = 0;
    private float screen_y = 0;
    private int lives = 0;
    private int score = 0;
    private Random random = new Random();
    private static DisplayMetrics metrics = new DisplayMetrics();
    //private Bitmap brickBM = BitmapFactory.decodeResource(getResources(), R.drawable.brick);
    private CanvasThread canvasthread;
    private boolean isResumed = false;
    //private SoundManager soundManager = new SoundManager();
    private Path mPath = new Path();
    Canvas pCanvas;
    private ArrayList<Point> lines = new ArrayList<Point>();
    private boolean finish = false;
    private ArrayList<Dot> dots = new ArrayList<Dot>();
    android.graphics.Point dpsize = new android.graphics.Point();
    public WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    private long timeStart = 0;
    private long timeLast = 0;
    private long timeEqualizer = 0;
    private long lastUpdate = 0;
    private Paint paint;



    public Panel(Context context, AttributeSet attributeSet) {
        super(context);
        getHolder().addCallback(this);
        canvasthread = new CanvasThread(getHolder(), this);
        setFocusable(true);
        paint = new Paint();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // update screen size
        screen_x = display.getWidth();
        screen_y = display.getHeight();




        // initiate the sound manager
        /*soundManager.initSounds(getContext());

        soundManager.addSound(1, R.raw.brickbounce);
        soundManager.addSound(2, R.raw.paddlebounce);
        soundManager.addSound(3, R.raw.wallbounce);*/

        // set all values anew if the game is not resumed
        if (!isResumed) {
            startGame();
            isResumed = true;
        }

        // check if thread is terminated and start it
        if(canvasthread.getState()== Thread.State.TERMINATED){
            canvasthread = new CanvasThread(getHolder(), this);
            canvasthread.setRunning(true);
            canvasthread.start();
        }else {
            canvasthread.setRunning(true);
            canvasthread.start();
        }


        timeStart = System.currentTimeMillis();
        timeLast = System.currentTimeMillis();
        CanvasThread.justStart();

    }

    void startGame() {

        // generate some dots
        dots.clear();
        for (int i = 0; i < 5; i++) {
            for (int z = 0; z < 5; z++) {
                dots.add(new Dot(screen_x, screen_y, i+1, 5, true));
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        // screen rotation is locked in this version
        //StartMenu.updateDM(metrics);
        //screen_x = metrics.widthPixels;
        //screen_y = metrics.heightPixels;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        canvasthread.setRunning(false);
        while(retry) {
            try {
                // keep trying until the join works
                canvasthread.join();
                retry = false;
            }   catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    //@Override
    public void onDraw(Canvas canvas) {
        timeEqualizer = (timeLast - System.currentTimeMillis()) / 10;
        timeLast = System.currentTimeMillis();
        // draw everything black
        canvas.drawColor(Color.BLACK);

        drawPath(canvas);
        drawDots(canvas);

        if (((int) ((System.currentTimeMillis() - timeStart)) / 1000) > lastUpdate) {
            updateScore(false);
        }


    }

    public void drawDots(Canvas canvas) {
        for (int i = 0; i < dots.size(); i++) {
            paint.setColor(dots.get(i).getColor());
            canvas.drawCircle(dots.get(i).getX(), dots.get(i).getY(), dots.get(i).getSize(), paint);

            dots.get(i).maybeChangeDirection();

            if (dots.get(i).getX() + dots.get(i).getDx() * timeEqualizer + dots.get(i).getSize()*2  > screen_x ||
                    dots.get(i).getX() + dots.get(i).getDx() * timeEqualizer - dots.get(i).getSize() < 0) {
                dots.get(i).setDx(dots.get(i).getDx() * -1);
            }
            if (dots.get(i).getY() + dots.get(i).getDy() * timeEqualizer + dots.get(i).getSize()*2 > screen_y - 25 ||
                    dots.get(i).getY() + dots.get(i).getDy() * timeEqualizer - dots.get(i).getSize()*2 < 0) {
                dots.get(i).setDy(dots.get(i).getDy() * -1);
            }


            dots.get(i).setX(dots.get(i).getX() + dots.get(i).getDx() * timeEqualizer);
            dots.get(i).setY(dots.get(i).getY() + dots.get(i).getDy() * timeEqualizer);
        }
    }

    public void drawPath(Canvas canvas) {
        Path path = new Path();
        boolean first = true;
        for (int i = 0; i < lines.size(); i++) {
            if (first) {
                first = false;
                path.moveTo(lines.get(0).getX(), lines.get(0).getY());
            }
            path.lineTo(lines.get(i).getX(), lines.get(i).getY());
        }
        if (finish) {

            path.lineTo(lines.get(0).getX(), lines.get(0).getY());
            detectCollisions();
            finish = false;
            lines.clear();
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawPath(path, paint);
    }

    public void detectCollisions() {
        int[] xes = new int[lines.size()];
        int[] yes = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            xes[i] =(int) lines.get(i).getX();
        }
        for (int i = 0; i < lines.size(); i++) {
            yes[i] =(int) lines.get(i).getY();
        }

        int colorToMerge = 0;
        boolean colorMatch = true;
        Polygon polygon = new Polygon(xes, yes, lines.size());
        for (int i = 0; i < dots.size(); i++) {
            if (polygon.contains((int)dots.get(i).getX(),(int) dots.get(i).getY())) {
                if (colorToMerge == 0) {
                    colorToMerge = dots.get(i).getColor();
                }
                if (colorToMerge == dots.get(i).getColor()) {
                    dots.get(i).setToMerge(true);
                }   else { colorMatch = false; }
            }

        }
        int merges = 0;
        int mergedX = 0;
        int mergedY = 0;
        int mergedSize = 0;
        if (colorMatch) {
            for (int i = 0; i < dots.size(); i++) {

                if (dots.get(i).isToMerge()) {
                    merges += 1;
                    mergedX += dots.get(i).getX();
                    mergedY += dots.get(i).getY();
                    mergedSize += dots.get(i).getSize();

                }
            }
        }

        if (merges > 1) {
            Iterator<Dot> it = dots.iterator();
            while (it.hasNext()) {
                if (it.next().isToMerge()) {
                    it.remove();
                }
            }
            dots.add(new Dot(mergedX / merges, mergedY / merges, colorToMerge, mergedSize, false));
        }
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setToMerge(false);
        }

        // check win
        int color = 1;
        boolean winning = false;
        for (int i = 1; i<6; i++) {
            switch(i) {
                case 1: color = Color.BLUE;
                    break;
                case 2: color = Color.RED;
                    break;
                case 3: color = Color.GREEN;
                    break;
                case 4: color = Color.WHITE;
                    break;
                case 5: color = Color.CYAN;
                    break;
            }

            int counter = 0;
            for (int z = 0; z < dots.size(); z++) {
                if (dots.get(z).getColor() == color) {
                    counter++;
                }
            }
            if (counter==1) {
                winning = true;
            }
        }
        if (winning) {
            updateScore(true);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    private float mX, mY;

    private void touch_start(float x, float y) {
        Point point = new Point(x,y);
        lines.add(point);
    }
    private void touch_move(float x, float y) {
        Point point = new Point(x,y);
        lines.add(point);


    }
    private void touch_up() {
        finish = true;
    }


    // generate organisms upon starting
    public void createOrganisms() {

    }



    // updates the textviews above the surfaceview panel
    public void updateScore(boolean end) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("time", (int) ((System.currentTimeMillis() - timeStart)) / 1000);
        bundle.putBoolean("end", end);
        msg.setData(bundle);
        if (end) {
            isResumed = false;
            CanvasThread.pause();
        }
        Game.handler.sendMessage(msg);
    }

}
      /*
class SoundManager {
    private SoundPool soundPool;
    private HashMap soundPoolMap;
    private AudioManager audioManager;
    private  Context context;

    // initiate
    public void initSounds(Context c) {
        context = c;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    // add sound from .raw into the map
    public void addSound(int index, int SoundID)
    {
        soundPoolMap.put(index, soundPool.load(context, SoundID, 1));
    }

    // set the volume and play the sound
    public void play(int sound) {
        float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundPool.play((Integer) soundPoolMap.get(sound), streamVolume, streamVolume, 1, 0, 1f);
    }
}       */

class Point {
    private float x;
    private float y;

    Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

}

class Dot {
    private float x;
    private float y;
    private float dx;
    private float dy;
    private float size = 5;
    private int color;
    private boolean toMerge = false;
    Random random = new Random();
    private float speedEqualizer = 0;

    Dot(float x, float y, int c, float s, Boolean r) {

        if (r) {
            // make sure nothings spawns at the edges
            this.x = (int) (x* 0.1) + (float) (x * random.nextFloat() * 0.85);
            this.y = (int) (y * 0.1) + (float) (y * random.nextFloat() * 0.85);
        }
        else {
            this.x = x;
            this.y = y;
        }
        speedEqualizer = random.nextFloat();
        dx = 5 * speedEqualizer / s;
        dy = 5 * (1-speedEqualizer) / s;
        if (random.nextBoolean()) {
            dx = dx * -1;
        }
        if (random.nextBoolean()) {
            dy = dy * -1;
        }
        switch (c) {
            case 1: color = Color.BLUE;
                break;
            case 2: color = Color.RED;
                break;
            case 3: color = Color.GREEN;
                break;
            case 4: color = Color.WHITE;
                break;
            case 5: color = Color.CYAN;
                break;
            default: color = c;
                break;
        }
        size = s;

    }
    Dot(float x, float y, float size) {

        dx = random.nextFloat();
        dy = random.nextFloat();
        this.size = size;
        //this.color = color;
    }

    public void maybeChangeDirection() {
        if (random.nextFloat() > 0.99) {
            dx = dx * -1;
        }
        if (random.nextFloat() > 0.99) {
            dy = dy * -1;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public int getColor() {
        return color;
    }

    public float getSize() {
        return size;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public boolean isToMerge() {
        return toMerge;
    }

    public void setToMerge(boolean toMerge) {
        this.toMerge = toMerge;
    }
}

/**

 * Minimum Polygon class for Android.

 */

class Polygon {



    // Polygon coodinates.

    private int[] polyY, polyX;



    // Number of sides in the polygon.

    private int polySides;



    /**

     * Default constructor.

     * @param px Polygon y coods.

     * @param py Polygon x coods.

     * @param ps Polygon sides count.

     */

    public Polygon( int[] px, int[] py, int ps ) {



        polyX = px;

        polyY = py;

        polySides = ps;

    }



    /**

     * Checks if the Polygon contains a point.

     * @see "http://alienryderflex.com/polygon/"

     * @param x Point horizontal pos.

     * @param y Point vertical pos.

     * @return Point is in Poly flag.

     */

    public boolean contains( int x, int y ) {



        boolean oddTransitions = false;

        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {

            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {

                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {

                    oddTransitions = !oddTransitions;

                }

            }

        }

        return oddTransitions;

    }

}