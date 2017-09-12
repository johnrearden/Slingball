package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Bolgbolg on 23/01/2016.
 */
public class CountdownTimer {
    private static String TAG = "CountdownTimer";
    long clockTime, initialTime, timeElapsed;
    long countdownAmount;
    float displayedTime;
    boolean running;

    Canvas mainCanvas;
    PlayAreaInfo playAreaInfo;

    Paint backgroundPaint;
    int outerCircleColor;
    Paint alphaPaint;
    int alphaPaintValue;
    BlurMaskFilter blur;

    static final float RED_THRESHOLD = 10.0f;
    static final int COLUMNS_OF_GLYPHS_IN_SOURCE = 11;
    static final int ROWS_OF_GLYPHS_IN_SOURCE = 4;
    static final float NUMBER_OF_GLYPHS_IN_DISPLAY = 4.75f;
    static final float POSITION_OF_POINT = 10.0f;
    static final float POSITION_OF_COLON = 10.5f;
    static final long SECONDS_TO_NANOS = 1000000000;
    static final float INTERNAL_BORDER = IntRepConsts.COUNTDOWN_INTERNAL_BORDER;
    static final float FADING_OUT_INITIAL_VALUE = IntRepConsts.COUNTDOWN_FADING_OUT_INITIAL_VALUE;

    Rect colonSource;
    RectF colonTarget;

    float xPos, yPos;
    float xSize, ySize;
    float radius;
    float glyphWidth, glyphHeight;
    float borderThickness, dividerThickness, internalBorderThickness;
    float haloBlurThickness;
    RectF opaqueMaskRect;
    float screenWidth, screenHeight;

    enum RunningToZero {
        OFF,
        RUNNING_TO_ZERO,
        COMPLETE
    }

    enum FadingOut {
        OFF,
        FADING_OUT,
        COMPLETE;

        float counter = 0;
        float decrement = IntRepConsts.COUNTDOWN_FADING_OUT_DECREMENT;
    }

    RunningToZero runningToZero;
    FadingOut fadingOut;
    float runningToZeroIncrement;

    Rect glyphSourceRect;
    RectF glyphTargetRect;
    Rect pointSource;
    RectF pointTarget;

    Bitmap currentGraphic;
    Bitmap source;
    Bitmap scaledDigits;
    Canvas canvas;

    enum DisplayType {
        NORMAL_OFF(0),
        NORMAL_ON(1),
        URGENT_OFF(2),
        URGENT_ON(3);
        int index;
        DisplayType(int index) {
            this.index = index;
        }
    }
    DisplayType displayType;

    public CountdownTimer(Resources resources) {

        source = BitmapFactory.decodeResource(resources, R.drawable.alldigits2);

        displayType = DisplayType.NORMAL_ON;
        glyphSourceRect = new Rect();
        glyphTargetRect = new RectF();
        runningToZero = RunningToZero.COMPLETE;
        fadingOut = FadingOut.COMPLETE;

        colonSource = new Rect();
        colonTarget = new RectF();
        pointSource = new Rect();
        pointTarget = new RectF();

        backgroundPaint = new Paint();
        outerCircleColor = resources.getColor(R.color.outer_circle);
        backgroundPaint.setColor(outerCircleColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        alphaPaint = new Paint();
        alphaPaintValue = 0;
        alphaPaint.setARGB(alphaPaintValue, 0, 0, 0);
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void update() {
        if (runningToZero == RunningToZero.RUNNING_TO_ZERO) {
            displayedTime = (float) (clockTime) / SECONDS_TO_NANOS;
            displayedTime -= runningToZeroIncrement;
            clockTime = (long)(displayedTime * SECONDS_TO_NANOS);
            if (displayedTime <= 0) {
                runningToZero = RunningToZero.COMPLETE;
                clockTime = 0;
                displayedTime = 0;
            }
        }
        if (fadingOut == FadingOut.FADING_OUT) {
            fadingOut.counter -= fadingOut.decrement;
            alphaPaintValue = (int) fadingOut.counter;
            if (alphaPaintValue < 0) {
                alphaPaintValue = 0;
            }
            if (fadingOut.counter <= 0) {
                fadingOut = FadingOut.COMPLETE;
                fadingOut.counter = 0;
            }
        }
        if (running) {
            // Calculate the time to display in nanoseconds.
            timeElapsed = System.nanoTime() - initialTime;
            if (timeElapsed > countdownAmount) {
                clockTime = 0;
            } else {
                clockTime = countdownAmount - timeElapsed;
            }

            // Convert the time to display to seconds.
            displayedTime = (float) (clockTime) / SECONDS_TO_NANOS;
        }
    }

    void startFadingOut() {
        fadingOut = FadingOut.FADING_OUT;
        fadingOut.counter = FADING_OUT_INITIAL_VALUE;
        running = false;
    }

    public void draw(Canvas canvas) {
        this.mainCanvas = canvas;
        createCountdownGraphic(displayedTime);
    }

    private void createCountdownGraphic(float displayedTime) {

        if(displayedTime < RED_THRESHOLD) {
            displayType = DisplayType.URGENT_ON;
        } else {
            displayType = DisplayType.NORMAL_ON;
        }
        int minutes = (int)(displayedTime / 60);
        int minutesOnes = minutes % 10;
        int seconds = (int)(displayedTime % 60);
        int secondsTens = (int)(seconds / 10);
        int secondsOnes = seconds % 10;
        int tenths = (int)(displayedTime * 10) % 10;

        // Draw the minutes.
        drawGlyph(minutesOnes, 0);

        // Draw the colon.
        float colonXPos = scaledDigits.getWidth() * (POSITION_OF_COLON / 11.0f);
        float colonYPos = glyphHeight * displayType.index;
        colonSource.set(
                (int) colonXPos,
                (int) colonYPos,
                (int) (colonXPos + (glyphWidth / 2)),
                (int) (colonYPos + glyphHeight));
        colonTarget.set(
                xPos + glyphWidth,
                yPos,
                xPos + glyphWidth * 1.5f,
                yPos + glyphHeight);
        mainCanvas.drawBitmap(scaledDigits, colonSource, colonTarget, null);

        // Draw the seconds.
        drawGlyph(secondsTens, 1.5f);
        drawGlyph(secondsOnes, 2.5f);

        // Draw the point.
        float pointXPos = scaledDigits.getWidth() * (POSITION_OF_POINT / 11.0f);
        float pointYPos = glyphHeight * displayType.index;
        pointSource.set((int)pointXPos,
                (int)pointYPos,
                (int)(pointXPos + (glyphWidth / 2)),
                (int)(pointYPos + glyphHeight));
        pointTarget.set(
                xPos + glyphWidth * 3.5f,
                yPos,
                xPos + glyphWidth * 4.0f,
                yPos + glyphHeight);
        mainCanvas.drawBitmap(scaledDigits, pointSource, pointTarget, null);

        // Draw the tenths of seconds.
        glyphSourceRect.set((int)(tenths * glyphWidth),
                (int)(displayType.index * glyphHeight),
                (int)((tenths + 1) * glyphWidth),
                (int)((displayType.index + 1) * glyphHeight) - 1);
        glyphTargetRect.set(
                xPos + 4.0f * glyphWidth,
                yPos + glyphHeight * 0.25f,
                xPos + 4.75f * glyphWidth - internalBorderThickness,
                yPos + glyphHeight - internalBorderThickness);

        mainCanvas.drawBitmap(scaledDigits, glyphSourceRect, glyphTargetRect, null);

        // If appropriate, draw the opaque mask over the countdown display.
        alphaPaint.setAlpha(255 - alphaPaintValue);
        mainCanvas.drawRect(opaqueMaskRect, alphaPaint);
    }

    private void drawGlyph(int value, float position) {
        glyphSourceRect.set((int)(value * glyphWidth),
                (int)(displayType.index * glyphHeight),
                (int)((value + 1) * glyphWidth),
                (int)((displayType.index + 1) * glyphHeight) - 1);
        glyphTargetRect.set(
                xPos + (position * glyphWidth) + internalBorderThickness,
                yPos + internalBorderThickness,
                xPos + ((position + 1) * glyphWidth) - internalBorderThickness,
                yPos + glyphHeight - internalBorderThickness);
        mainCanvas.drawBitmap(scaledDigits, glyphSourceRect, glyphTargetRect, null);
    }

    public void setTime(float amount) {
        this.countdownAmount = (long)amount * SECONDS_TO_NANOS;
    }

    public float getTime() {
        return clockTime / SECONDS_TO_NANOS;
    }

    public void start() {
        running = true;
        initialTime = System.nanoTime();
    }

    public void stop() {
        running = false;
    }

    public void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        this.playAreaInfo = playAreaInfo;

        screenWidth = playAreaInfo.screenWidth;
        screenHeight = playAreaInfo.screenHeight;
        xPos = IntRepConsts.COUNTDOWN_XPOS * screenWidth;
        yPos = IntRepConsts.COUNTDOWN_YPOS * playAreaInfo.topPanelHeight;
        xSize = IntRepConsts.COUNTDOWN_XSIZE * screenWidth;
        ySize = IntRepConsts.COUNTDOWN_YSIZE * playAreaInfo.topPanelHeight;
        opaqueMaskRect = new RectF(xPos, yPos, xPos + xSize, yPos + ySize);
        borderThickness = IntRepConsts.COUNTDOWN_BORDER_THICKNESS * screenWidth;
        haloBlurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;
        dividerThickness = IntRepConsts.COUNTDOWN_DIVIDER_THICKNESS * screenWidth;
        glyphWidth = xSize / NUMBER_OF_GLYPHS_IN_DISPLAY;
        Log.d(TAG, "countdownTimer.glyphWidth == " + glyphWidth);
        internalBorderThickness = glyphWidth * INTERNAL_BORDER;
        glyphHeight = ySize;

        backgroundPaint.setMaskFilter(new BlurMaskFilter(haloBlurThickness, BlurMaskFilter.Blur.NORMAL));

        // Create a scaled version of the source Bitmap.
        int newWidth = (int)(glyphWidth * COLUMNS_OF_GLYPHS_IN_SOURCE);
        int newHeight = (int)(glyphHeight * ROWS_OF_GLYPHS_IN_SOURCE);
        scaledDigits = Bitmap.createScaledBitmap(source, newWidth, newHeight, false);

        // Create the currentGraphic Bitmap and associated canvas.
        int gWidth = (int)xSize;
        int gHeight = (int)ySize;
        currentGraphic = Bitmap.createBitmap(gWidth, gHeight, Bitmap.Config.ARGB_8888);
        mainCanvas = new Canvas(currentGraphic);
        mainCanvas.drawARGB(255, 0, 0, 0);

        // Create the halo blurMaskFilter.
        blur = new BlurMaskFilter(haloBlurThickness, BlurMaskFilter.Blur.INNER);

    }

    void drawInitialBackground (Canvas canvas) {
        // Draw the background box.
        int w = playAreaInfo.screenWidth;
        int h = playAreaInfo.topPanelHeight;
        float border;
        float left, right, top, bottom;
        border = IntRepConsts.COUNTDOWN_BORDER_THICKNESS * w;
        radius = IntRepConsts.ROUNDRECT_RADIUS * w;
        left = IntRepConsts.COUNTDOWN_XPOS * w - border;
        top = IntRepConsts.COUNTDOWN_YPOS * h - border;
        right = IntRepConsts.COUNTDOWN_XPOS * w +
                IntRepConsts.COUNTDOWN_XSIZE * w + border;
        bottom = IntRepConsts.COUNTDOWN_YPOS * h +
                IntRepConsts.COUNTDOWN_YSIZE * h + border;
        /*bumpTest.setBounds((int) left, (int)top, (int) right, (int) bottom);
        bumpTest.draw(canvas);*/
        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, backgroundPaint);
        left += border;
        right -= border;
        top += border;
        bottom -= border;
        canvas.drawRect(new RectF(left, top, right, bottom), backgroundPaint);

        // Draw the black rectangle in the box.
        canvas.drawBitmap(
                currentGraphic,
                xPos,
                yPos,
                null
        );
    }

    public float getDisplayedTime() {
        return displayedTime;
    }

    public void increaseTimeRemaining(int seconds) {
        countdownAmount += seconds * SECONDS_TO_NANOS;
        update();
    }

}
