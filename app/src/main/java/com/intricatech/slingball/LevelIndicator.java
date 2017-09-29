package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Bolgbolg on 04/04/2016.
 */
public class LevelIndicator {

    float xPos, yPos;
    float verticalOffset;
    float xBackground, yBackground;
    float radius, angle, backgroundCircleDiameter;
    float glyphWidth, glyphHeight;
    Rect source;
    RectF dest;
    int currentLevel;

    Bitmap digitsSource, scaledDigits;
    Bitmap displayedBitmap;
    Bitmap background;
    Paint backgroundPaint;
    Paint blankTransparentPaint, blackPaint;
    int backgroundColor;
    float blurThickness;

    private boolean shouldRedrawOnBB1;
    private boolean shouldRedrawOnBB2;

    Canvas displayedBitmapCanvas, backgroundCanvas;

    LevelIndicator(Resources resources, int startingLevel, Bitmap digitSource) {
        this.digitsSource = digitSource;
        source = new Rect();
        dest = new RectF();
        currentLevel = startingLevel;

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundColor = resources.getColor(R.color.outer_circle);
        backgroundPaint.setColor(backgroundColor);

        blackPaint = new Paint();
        blackPaint.setStyle(Paint.Style.FILL);
        blackPaint.setColor(Color.BLACK);

        blankTransparentPaint = new Paint();
        blankTransparentPaint.setStyle(Paint.Style.FILL);
        blankTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        blankTransparentPaint.setColor(Color.TRANSPARENT);
    }

    void showBlank() {
        //displayedBitmapCanvas.drawColor(Color.BLACK);
        displayedBitmapCanvas.drawBitmap(
                background,
                0, 0, null
        );
        shouldRedrawOnBB2 = true;
        shouldRedrawOnBB1 = true;
    }

    void updateLevelIndicator(int levelToShow) {
        //erasePreviousDigits();
        shouldRedrawOnBB1 = true;
        shouldRedrawOnBB2 = true;
        int level = levelToShow;
        if (level < 10) {
            displayedBitmapCanvas.drawColor(Color.BLACK);
            displayedBitmapCanvas.drawBitmap(
                    background,
                    0, 0, null
            );
            source.set(
                    0,
                    (int) (level * glyphHeight),
                    (int) (glyphWidth - 1),
                    (int) ((level + 1) * glyphHeight));
            dest.set(
                    (displayedBitmap.getWidth() / 2) - (glyphWidth / 2),
                    (displayedBitmap.getHeight() / 2) - (glyphHeight / 2),
                    (displayedBitmap.getWidth() / 2) + (glyphWidth / 2),
                    (displayedBitmap.getHeight() / 2) + (glyphHeight / 2));

        } else if (level >= 10) {
            displayedBitmapCanvas.drawColor(Color.BLACK);
            displayedBitmapCanvas.drawBitmap(
                    background,
                    0, 0, null
            );
            int units = level % 10;
            source.set(
                    0,
                    (int) (units * glyphHeight),
                    (int) (glyphWidth - 1),
                    (int) ((units + 1) * glyphHeight));
            dest.set(
                    displayedBitmap.getWidth() / 2,
                    (displayedBitmap.getHeight() / 2) - (glyphHeight / 2),
                    (displayedBitmap.getWidth() / 2) + glyphWidth,
                    (displayedBitmap.getHeight() / 2) + (glyphHeight / 2));
            displayedBitmapCanvas.drawBitmap(
                    scaledDigits,
                    source,
                    dest,
                    null);
            int tens = level / 10;
            source.set(
                    0,
                    (int) (tens * glyphHeight),
                    (int) (glyphWidth - 1),
                    (int) ((tens + 1) * glyphHeight));
            dest.set(
                    (displayedBitmap.getWidth() / 2) - glyphWidth,
                    (displayedBitmap.getHeight() / 2) - (glyphHeight / 2),
                    displayedBitmap.getWidth() / 2,
                    (displayedBitmap.getHeight() / 2) + (glyphHeight / 2));
        }
        displayedBitmapCanvas.drawBitmap(
                scaledDigits,
                source,
                dest,
                null);
    }

    public void redrawIfNecessary(Canvas canvas, boolean usingBB1) {
        if (shouldRedrawOnBB1 && usingBB1) {
            canvas.drawBitmap(
                    displayedBitmap,
                    xPos,
                    yPos,
                    null
            );
            shouldRedrawOnBB1 = false;
        }
        if (shouldRedrawOnBB2 && !usingBB1) {

            canvas.drawBitmap(
                    displayedBitmap,
                    xPos,
                    yPos,
                    null
            );
            shouldRedrawOnBB2 = false;
        }
    }

    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        radius = playAreaInfo.scaledDiameter * 0.5f * IntRepConsts.LEVEL_INDICATOR_RADIUS_RELATIVE_TO_CIRCLE;
        angle = IntRepConsts.LEVEL_INDICATOR_ANGLE;
        verticalOffset = IntRepConsts.CURVED_DISPLAYS_OFFSET * playAreaInfo.scaledDiameter;

        blurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;

        backgroundCircleDiameter = playAreaInfo.scaledDiameter * IntRepConsts.LEVEL_INDICATOR_DIAMETER_RATIO_TO_CIRCLE;
        glyphHeight = (float) (backgroundCircleDiameter / Math.sqrt(2)
                        * IntRepConsts.LEVEL_INDICATOR_RATIO_OF_GLYPHHEIGHT_TO_OVERALL_HEIGHT);
        glyphWidth = glyphHeight / 2;

        scaledDigits = Bitmap.createScaledBitmap(digitsSource, (int) (glyphWidth), (int) (glyphHeight * 13), false);

        background = Bitmap.createBitmap((int) backgroundCircleDiameter, (int) backgroundCircleDiameter, Bitmap.Config.ARGB_8888);
        backgroundCanvas = new Canvas(background);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setMaskFilter(new BlurMaskFilter(blurThickness, BlurMaskFilter.Blur.NORMAL));
        backgroundCanvas.drawCircle(
                backgroundCircleDiameter / 2,
                backgroundCircleDiameter / 2,
                backgroundCircleDiameter / 2.15f,
                backgroundPaint);

        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setMaskFilter(null);
        backgroundCanvas.drawCircle(
                backgroundCircleDiameter / 2,
                backgroundCircleDiameter / 2,
                backgroundCircleDiameter / 2.5f,
                backgroundPaint);
        xBackground = -(backgroundCircleDiameter / 2) + playAreaInfo.xCenterOfCircle + radius * (float)(Math.cos(angle));
        yBackground = -(backgroundCircleDiameter / 2) + playAreaInfo.yCenterOfCircle + radius * (float)(Math.sin(angle)) - verticalOffset;
        xPos = xBackground;
        yPos = yBackground;

        displayedBitmap = Bitmap.createBitmap((int) backgroundCircleDiameter, (int) backgroundCircleDiameter, Bitmap.Config.ARGB_8888);
        displayedBitmapCanvas = new Canvas(displayedBitmap);

    }

    void drawBackground(Canvas canvas) {
        if (background == null) {
            throw new AssertionError("background bitmap is null");
        }
        canvas.drawBitmap(
                background,
                xBackground,
                yBackground,
                null);
    }
}
