package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Bolgbolg on 01/03/2016.
 */
public class LevelAnnouncer {
    Resources resources;                // A reference to this Activities Resources object, to allow decoding of graphics.
    Bitmap digitImageSource;
    Bitmap digitImage;
    static final float RATIO_OF_DIGIT_TO_CIRCLE_DIAMETER = 0.18f;
    Bitmap displayedBitmap;             // The Bitmap returned to the PhysicsAndGBRenderer.
    Canvas canvas;
    float xPos, yPos;        // corner of the circle.
    int levelNumberToDisplay;
    int startCycles, fadeInCycles, showCycles, fadeOutCycles;
    boolean animating;
    int currentCycle, totalCycles;

    Rect source;
    RectF dest;
    float xCircleCentre, yCircleCentre;
    float imageWidth, imageHeight, displayedBitmapHeight;

    Paint bitmapPaint;
    int alphaComponent;

    static final float PAUSE_BEFORE_START = 0.2f;
    static final float FADE_IN = 0.3f;
    static final float FADE_OUT = 0.3f;

    enum FadePhase {PAUSE_BEFORE_START, FADING_IN, CONSTANT, FADING_OUT}
    FadePhase fadePhase;

    public LevelAnnouncer(Resources resources, Bitmap digitSource) {
        this.resources = resources;
        this.digitImageSource = digitSource;
        animating = false;
        bitmapPaint = new Paint();

        source = new Rect();
        dest = new RectF();
    }

    void update(int levelNumber) {
        switch (fadePhase) {
            case PAUSE_BEFORE_START: {
                alphaComponent = 0;
                if (currentCycle++ >= startCycles) {
                    fadePhase = FadePhase.FADING_IN;
                }
                break;
            }
            case FADING_IN: {
                int a = currentCycle - startCycles;
                float denom = (float)a / fadeInCycles;
                alphaComponent = (int)(255 * denom);
                if (currentCycle++ >= fadeInCycles + startCycles) {
                    fadePhase = FadePhase.CONSTANT;
                }
                break;
            }
            case CONSTANT: {
                alphaComponent = 255;
                if (currentCycle++ >= fadeInCycles + showCycles + startCycles) {
                    fadePhase = FadePhase.FADING_OUT;
                }
                break;
            }
            case FADING_OUT: {
                int a = currentCycle - showCycles - fadeInCycles - startCycles;
                float denom = (float)a / fadeOutCycles;
                alphaComponent = 255 - (int)(255 * denom);
                currentCycle++;
                if (currentCycle >= totalCycles) {
                    animating = false;
                }
                break;
            }
        }
        bitmapPaint.setAlpha(alphaComponent);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        levelNumberToDisplay = levelNumber + 1;

        if (levelNumberToDisplay < 10) {
            dest.set(
                    imageWidth / 2,
                    0,
                    imageWidth * 3 / 2,
                    displayedBitmapHeight);
            source.set(
                    0,
                    (int)(levelNumberToDisplay * displayedBitmapHeight),
                    (int)imageWidth - 1,
                    (int)((levelNumberToDisplay + 1) * displayedBitmapHeight - 1));
            canvas.drawBitmap(
                    digitImage,
                    source,
                    dest,
                    bitmapPaint);
        } else {
            int tens = (int)levelNumberToDisplay / 10;
            int ones = levelNumberToDisplay % 10;
            dest.set(
                    0,
                    0,
                    imageWidth,
                    displayedBitmapHeight);
            source.set(
                    0,
                    (int)(tens * displayedBitmapHeight),
                    (int)imageWidth - 1,
                    (int)((tens + 1) * displayedBitmapHeight - 1));
            canvas.drawBitmap(
                    digitImage,
                    source,
                    dest,
                    bitmapPaint);
            dest.set(
                    imageWidth,
                    0,
                    imageWidth * 2 - 1,
                    displayedBitmapHeight);
            source.set(
                    0,
                    (int)(ones * displayedBitmapHeight),
                    (int)imageWidth - 1,
                    (int)((ones + 1) * displayedBitmapHeight - 1));
            canvas.drawBitmap(
                    digitImage,
                    source,
                    dest,
                    bitmapPaint);
        }
    }

    /**
     *
     * @return
     */
    Bitmap getImage() {
        return displayedBitmap;

    }

    /**
     *
     * @param numberOfCycles
     */
    void startAnimation(int numberOfCycles) {
        animating = true;
        totalCycles = numberOfCycles;
        fadePhase = FadePhase.PAUSE_BEFORE_START;
        startCycles = (int)(numberOfCycles * PAUSE_BEFORE_START);
        fadeInCycles = (int)(numberOfCycles * FADE_IN);
        fadeOutCycles = (int)(numberOfCycles * FADE_OUT);
        showCycles = numberOfCycles - (fadeInCycles + fadeOutCycles + startCycles);
        currentCycle = 0;
    }

    void onSurfaceChanged(int screenWidth, int screenHeight, PlayAreaInfo playAreaInfo) {

        xCircleCentre = screenWidth / 2;
        yCircleCentre = playAreaInfo.topPanelHeight + screenWidth / 2;

        float ratioOfSourceHeightToWidth = digitImageSource.getHeight() / digitImageSource.getWidth();
        imageWidth = playAreaInfo.scaledDiameter * RATIO_OF_DIGIT_TO_CIRCLE_DIAMETER;
        imageHeight = imageWidth * ratioOfSourceHeightToWidth;
        displayedBitmapHeight = imageHeight / 13;

        digitImage = Bitmap.createScaledBitmap(
                digitImageSource,
                (int) imageWidth,
                (int) imageHeight,
                false);

        displayedBitmap = Bitmap.createBitmap((int) imageWidth * 2, (int) displayedBitmapHeight, Bitmap.Config.ARGB_8888);
        xPos = xCircleCentre - imageWidth;
        yPos = yCircleCentre - displayedBitmapHeight / 2;
        canvas = new Canvas(displayedBitmap);
    }
}
