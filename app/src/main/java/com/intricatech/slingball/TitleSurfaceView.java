package com.intricatech.slingball;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Bolgbolg on 16/08/2016.
 */
public class TitleSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback,
        Choreographer.FrameCallback,
        Runnable {

    static final String TAG = "TitleSurfaceView";
    int viewWidth, viewHeight;
    int xCenter, yCenter;
    int lavaWidth, lavaHeight;
    SurfaceHolder holder;
    Choreographer choreographer;
    Resources resources;
    Thread titleSurfaceViewThread;

    volatile boolean continueRenderingTitle;
    private volatile boolean startNextDrawCycle;

    float backgroundOffset;
    float radarXPos;
    float radarXPosIncrement;
    static final float RADAR_XPOS_INCREMENT_RATIO = 0.012f;
    static final float BACKGROUND_OFFSET_INCREMENT = 3.0f;
    Rect backgroundSource;
    RectF backgroundDest;

    static final String TITLE_TEXT = "SLINGBALL";
    static final float TEXT_SIZE = 80.0f;
    static final float TITLE_XCEN = 0.5f;
    static final float TITLE_YCEN = 0.5f;
    static final float TITLE_XSIZE = 0.8f;
    static final float TITLE_Y_TO_X_RATIO = 0.75f;
    Paint textPaint;
    Paint transparencyPaint;
    Bitmap titleBitmap;
    Bitmap textBitmap;
    Bitmap radarBitmap;
    Canvas canvas;


    public TitleSurfaceView (Context context) {
        super(context);
        resources = context.getResources();
        holder = getHolder();
        holder.addCallback(this);
        initialize();
    }

    public TitleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        initialize();
    }

    public TitleSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        holder.addCallback(this);
        initialize();
    }

    private void initialize() {
        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(this);
        startNextDrawCycle = false;
        resources = getResources();
        textBitmap = BitmapFactory.decodeResource(resources, R.drawable.titletext2);
        radarBitmap = BitmapFactory.decodeResource(resources, R.drawable.radar_sweep);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);

        backgroundSource = new Rect();
        backgroundDest = new RectF();
    }

    public void run() {
        outerloop:
        while (continueRenderingTitle) {
            if (!holder.getSurface().isValid()) {
                continue;
            }
            innerloop:
            while (!startNextDrawCycle) {
                try {
                    Thread.sleep(0, 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            startNextDrawCycle = false;
            canvas = holder.lockCanvas();

            updateBackgroundOffset();

            updateRadarXPos();
            drawRadarBackground(canvas);

            canvas.drawBitmap(titleBitmap, 0, 0, null);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void createTitleScreen(int width, int height) {
        titleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(titleBitmap);
        canvas.drawARGB(255, 0, 0, 0);


        float bitmapRatio = (float)textBitmap.getHeight() / textBitmap.getWidth();
        float surfaceRatio = (float)height / width;

        RectF dest;
        if (surfaceRatio >= bitmapRatio) {
            dest = new RectF(
                    0,
                    (height / 2) - (width * bitmapRatio / 2),
                    width,
                    (height / 2) + (width * bitmapRatio / 2)
            );
        } else {
            dest = new RectF(
                    (width / 2) - (height / bitmapRatio / 2),
                    0,
                    (width / 2) + (height / bitmapRatio / 2),
                    height
            );
        }


        // Draw the text Bitmap onto the blank background.
        Rect source = new Rect(
                0,
                0,
                textBitmap.getWidth(),
                textBitmap.getHeight()
        );
        Paint porterPaint = new Paint();
        porterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        porterPaint.setAntiAlias(true);
        canvas.drawBitmap(
                textBitmap,
                source,
                dest,
                porterPaint);
    }

    private void drawRadarBackground(Canvas canvas) {
        canvas.drawARGB(255, 165, 170, 170);
        canvas.drawBitmap(
                radarBitmap,
                radarXPos,
                0,
                null
        );
    }

    private void updateBackgroundOffset() {
        backgroundOffset += BACKGROUND_OFFSET_INCREMENT;
        if (backgroundOffset >= 100) {
            backgroundOffset = 0;
        }
    }

    private void updateRadarXPos () {

        if ((radarXPos -= radarXPosIncrement) < 0 ) {
            radarXPos = viewWidth;
        }
    }
    public void onPause() {
        continueRenderingTitle = false;
        startNextDrawCycle = true;
        try {
            titleSurfaceViewThread.join();
        } catch (InterruptedException e) {
            Log.d(TAG, "Attempt to join titleSurfaceViewThread failed");
            e.printStackTrace();
        }
        choreographer.removeFrameCallback(this);
    }

    public void onResume() {
        continueRenderingTitle = true;
        choreographer.postFrameCallback(this);


    }
    public void onStop() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        viewWidth = width;
        viewHeight = height;
        xCenter = width / 2;
        yCenter = height / 2;

        createTitleScreen(viewWidth, viewHeight);

        lavaWidth = (int) (width * TITLE_XSIZE);
        lavaHeight = (int) (width * TITLE_XSIZE * TITLE_Y_TO_X_RATIO);
        float radarHeight, radarWidth, ratio;
        radarXPosIncrement = RADAR_XPOS_INCREMENT_RATIO * viewWidth;
        radarHeight = viewHeight;
        ratio = radarBitmap.getHeight() / radarBitmap.getWidth();
        radarWidth = radarHeight / ratio;
        radarBitmap = Bitmap.createScaledBitmap(
                radarBitmap,
                (int) radarWidth,
                (int) radarHeight,
                false
        );
        titleSurfaceViewThread = new Thread(this);
        titleSurfaceViewThread.start();

        startNextDrawCycle = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        continueRenderingTitle = false;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        startNextDrawCycle = true;
        choreographer.postFrameCallback(this);
    }
}
