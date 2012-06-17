package com.luklar9.assignment4;

import android.content.Context;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.*;

import java.util.*;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {

    // initiate things
    private float screen_x = 0;
    private float screen_y = 0;
    private CanvasThread canvasthread;
    private boolean isResumed = false;
    private final ArrayList<Point> lines = new ArrayList<Point>();
    private boolean finish = false;
    private final ArrayList<Dot> dots = new ArrayList<Dot>();
    private final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    private final Display display = wm.getDefaultDisplay();
    private long timeStart = 0;
    private long timeLast = 0;
    private long timeEqualizer = 0;
    private long freeTimer = 0;
    private final Paint paint;
    private final SoundManager soundManager = new SoundManager();
    private int colorToMerge = 0;

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
        soundManager.initSounds(getContext());
        soundManager.addSound(1, R.raw.draw);
        soundManager.addSound(2, R.raw.merge);
        soundManager.addSound(3, R.raw.pow);
        soundManager.addSound(4, R.raw.winning);

        // set all values anew if the game is not resumed
        if (!isResumed) {
            startGame();
            isResumed = true;
            timeStart = System.currentTimeMillis();
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


        timeLast = System.currentTimeMillis();
        freeTimer = System.currentTimeMillis();
        CanvasThread.justStart();

    }

    void startGame() {

        // generate some dots
        dots.clear();
        for (int i = 0; i < 5; i++) {
            for (int z = 0; z < 5; z++) {
                // add them to the arraylist so they can be drawn
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

        // set the timeequalizer to normalize dot movements disjointed from fps
        // and update timeLast to prepare for creating the next timeEqualizer
        timeEqualizer = (timeLast - System.currentTimeMillis()) / 10;
        timeLast = System.currentTimeMillis();

        // draw everything black
        canvas.drawColor(Color.BLACK);

        // draw things
        drawPath(canvas);
        drawDots(canvas);

        // update the score if more than 1s has passed
        if (((int) ((System.currentTimeMillis() - timeStart)) / 1000) > 0) {
            updateScore(false);
        }
    }

    void drawDots(Canvas canvas) {
        for (Dot dot : dots) {

            // draw the dot
            paint.setColor(dot.getColor());
            canvas.drawCircle(dot.getX(), dot.getY(), dot.getSize(), paint);

            // randomly alter dx / dy
            dot.maybeChangeDirection();

            // check for border collisions before updating the balls position
            if (dot.getX() + dot.getDx() * timeEqualizer + dot.getSize() * 2 > screen_x ||
                    dot.getX() + dot.getDx() * timeEqualizer - dot.getSize() < 0) {
                dot.setDx(dot.getDx() * -1);
            }
            if (dot.getY() + dot.getDy() * timeEqualizer + dot.getSize() * 2 > screen_y - 25 ||
                    dot.getY() + dot.getDy() * timeEqualizer - dot.getSize() * 2 < 0) {
                dot.setDy(dot.getDy() * -1);
            }

            // update the position
            dot.setX(dot.getX() + dot.getDx() * timeEqualizer);
            dot.setY(dot.getY() + dot.getDy() * timeEqualizer);
        }
    }

    void drawPath(Canvas canvas) {

        // recreate the path to draw
        Path path = new Path();
        boolean first = true;

        // put all the detected lines in it
        for (Point line : lines) {
            if (first) {
                first = false;
                path.moveTo(lines.get(0).getX(), lines.get(0).getY());
            }
            path.lineTo(line.getX(), line.getY());
        }

        // check for collisions on touch up
        if (finish) {
            detectCollisions();
            finish = false;
            lines.clear();
        }

        // draw it
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawPath(path, paint);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        // get the position
        float x = event.getX();
        float y = event.getY();
        
        // differentiate between actions
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

    // start a new line if the freetimer has been reached after a faulty draw
    private void touch_start(float x, float y) {
        if (System.currentTimeMillis() > freeTimer) {
            lines.add(new Point(x,y));
        }
    }
    
    // add points to the path
    private void touch_move(float x, float y) {
        if (System.currentTimeMillis() > freeTimer) {
            lines.add(new Point(x,y));
            if (detectIntersects()) {
                lines.clear();
                freeTimer = System.currentTimeMillis() + 1000;
                soundManager.play(3);
            }
        }
    }
    
    // end of path, detect collisions on the way back, add the origin to the lines array
    // also play an annoying sound if the user fails
    private void touch_up() {
        if (System.currentTimeMillis() > freeTimer) {
            if (lines.size() > 0) {
                lines.add(new Point(lines.get(0).getX(), lines.get(0).getY()));
                if (detectIntersects()) {
                    lines.clear();
                    soundManager.play(3);
                } else {
                    finish = true;
                }
            }
        }
    }

    // detect line intersecting itself
    boolean detectIntersects() {
        if (lines.size()>2) {
            for (int i = 0; i < lines.size() - 1; i++) {
                if (detectLineIntersection(lines.get(i), lines.get(i + 1), lines.get(lines.size() - 2), lines.get(lines.size() - 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    // updates the textviews above the surfaceview panel
    void updateScore(boolean end) {
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

    private boolean detectLineIntersection(Point start1,
                                           Point end1, Point start2, Point end2) {

        //init
        double k1, k2, m1, m2, iY, iX;

        // y = kx + m

        // find k
        k1 = (start1.y - end1.y) / (start1.x - end1.x);
        k2 = (start2.y - end2.y) / (start2.x - end2.x);

        // find m
        // m = y - kx
        m1 = start1.y - (k1 * start1.x);
        m2 = start2.y - (k2 * start2.x);

        // slopes are equal
        if (k1 == k2) {

            // they lines are on the same line
            if (m1 == m2) {

                // check if one end of the second line is overlapped by the first line
                if (Math.min(start1.x, end1.x) < start2.x &&
                        Math.max(start1.x, end1.x) > start2.x) {
                    // first point overlapped
                    return true;
                }

                // check the other line
                if (Math.min(start1.x, end1.x) < end2.x &&
                        Math.max(start1.x, end1.x) > end2.x) {
                    // second point overlapped
                    return true;
                }
            }

            // the lines are simply parallel
            return false;
        }

        // slopes are not equal, meaning they intersect somewhere
        // find if they intersect withing the segment
        else {
            // find the intersecting point
            iX = (m1 - m2) / (k2 - k1);
            // intersection in iX gives iY
            iY = k1*iX + m1;

            // check if the first line segment overlaps the intersection point
            if ((iX > Math.min(start1.x, end1.x) && iX < Math.max(start1.x,
                    end1.x))
                    && (iY > Math.min(start1.y, end1.y) && iY < Math.max(
                    start1.y, end1.y))) {

                // first segment overlaps the intersection point
                // check the second
                if ((iX > Math.min(start2.x, end2.x) &&
                        iX < Math.max(start2.x, end2.x)) && (
                        iY > Math.min(start2.y, end2.y) &&
                        iY < Math.max(start2.y, end2.y))) {

                    // both segments overlap the intersection point
                    return true;
                }
            }

            // both lines do not overlap the intersection point
            return false;
        }
    }

    void detectCollisions() {

        // put all the x:s and y:s into an array
        int[] xes = new int[lines.size()];
        int[] yes = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            xes[i] =(int) lines.get(i).getX();
        }
        for (int i = 0; i < lines.size(); i++) {
            yes[i] =(int) lines.get(i).getY();
        }

        boolean colorMatch = true;

        // check if the polygon contains more than 1 of each color but no other color
        colorToMerge = 0;
        for (Dot dot2 : dots) {

            // check if the dots are within the polygon
            if (detectContained((int) dot2.getX(), (int) dot2.getY(), xes, yes, lines.size())) {

                //  set the color to the first color found
                if (colorToMerge == 0) {
                    colorToMerge = dot2.getColor();
                }

                // if any of the next colors are not the same, set colormatch to false and break
                if (colorToMerge == dot2.getColor()) {
                    dot2.setToMerge(true);
                } else {
                    colorMatch = false;
                    break;
                }
            }
        }

        // set merging variables
        int merges = 0;
        int mergedX = 0;
        int mergedY = 0;
        int mergedSize = 0;

        // calculate position of the new and count merges, must be >1
        if (colorMatch) {
            for (Dot dot : dots) {
                if (dot.isToMerge()) {
                    merges += 1;
                    mergedX += dot.getX();
                    mergedY += dot.getY();
                    mergedSize += dot.getSize();
                }
            }
        }

        // more than one to merge, play a sound, remove the old dot, add a new
        if (merges > 1) {
            soundManager.play(2);
            Iterator<Dot> it = dots.iterator();
            while (it.hasNext()) {
                if (it.next().isToMerge()) {
                    it.remove();
                }
            }
            dots.add(new Dot(mergedX / merges, mergedY / merges, colorToMerge, mergedSize, false));
        }

        // set everything back to not being merged
        for (Dot dot1 : dots) {
            dot1.setToMerge(false);
        }

        // check win, only 1 dot of any color
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

            // count every color and play a sound if winning
            int counter = 0;
            for (Dot dot : dots) {
                if (dot.getColor() == color) {
                    counter++;
                }
            }
            if (counter==1) {
                soundManager.play(4);
                winning = true;
            }
        }

        // update the score and end the game via the handler
        if (winning) {
            updateScore(true);
        }
    }

    private boolean detectContained( int pointX, int pointY, int[] xes, int[] yes, int sides ) {

        boolean side = false;

        // run once for every edge of the polygon, no precheck with a larger bounding box,
        // should be implemented if using more dots
        for( int i = 0, j = sides -1; i < sides; j = i++ ) {

            // check if point lies between two polygon edges on the y-axis
            if ( (yes[i] < pointY && yes[j] >= pointY ) || (yes[j] < pointY && yes[i] >= pointY)) {

                // check if the point horizontally intersects with polygon line on the right side
                // points within the polygon have an odd number of point-extended line-intersecting polygon lines
                if (xes[i] + (pointY - yes[i]) / (yes[j] - yes[i]) * (xes[j] - xes[i]) < pointX) {
                    side = !side;
                }
            }
        }
        return side;
    }
}

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
}

class Point {
    public final float x;
    public final float y;

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
    private final Random random = new Random();

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

        // make sure the dots move the same speed, size taken into account
        float speedEqualizer = random.nextFloat();
        dx = 5 * speedEqualizer / s;
        dy = 5 * (1- speedEqualizer) / s;
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

    // 1% chance to change direction every draw
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





