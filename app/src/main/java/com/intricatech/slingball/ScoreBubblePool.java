package com.intricatech.slingball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bolgbolg on 19/04/2016.
 */
public class ScoreBubblePool {

    private static final String TAG = "ScoreBubblePool";

    static final int POOL_SIZE = IntRepConsts.SCORE_BUBBLE_POOL_SIZE;
    static final int DISPLAY_TIME = IntRepConsts.SCORE_BUBBLE_DISPLAY_FRAMES;
    static final int ALPHA_DECREMENT = IntRepConsts.SCORE_BUBBLE_ALPHA_DECREMENT;
    static final int BORDER_CIRCLE_THICKNESS = 3;
    float bubbleDiameter;
    float levelEndBubbleDiameter;

    ScoreBubble[] scoreBubbles;
    PlayAreaInfo playAreaInfo;
    Bitmap digitsSource;
    Bitmap bubbleSourceGraphics;
    Map<BubbleColor, Bitmap> bubbleBackgroundMap;
    Bitmap levelEndBubbleBackground;
    Paint opacityPaint;
    Paint circleBorderPaint;
    Paint bubbleBackgroundPaint;

    Rect bubbleSource;
    RectF bubbleDest;
    RectF levelEndBubbleDest;
    float glyphWidth, glyphHeight;

    /**
     * Private class describes a single scoreBubble.
     * NOTE : The alphaComponent acts also as a flag to signal that this scoreBubble is
     * unassigned. When the alphaComponent is 0, the bubble can be reassigned for another
     * score indication (this saves constant creation and garbage collection of the bubbles).
     */
    private class ScoreBubble {
        float xPos, yPos;
        boolean on;
        int alphaComponent;
        int countdownUntilFade;
        Bitmap scoreBubbleSprite;
        Canvas workerCanvas;
        BubbleColor color;
        BubbleColor levelEndBubbleColor;

        /**
         * No-argument constructor. The scoreBubbles are created when the bubblePool is
         * instantiated, and configured as needed.
         */
        ScoreBubble() {
            xPos = 0;
            yPos = 0;
            on = false;
            alphaComponent = 0;
            scoreBubbleSprite = null;
            countdownUntilFade = DISPLAY_TIME;
            color = BubbleColor.TRANSPARENT;
        }
    }

    /**
     * Enumerates the possible background colors for the scoreBubbles.
     */
    enum BubbleColor {
        TRANSPARENT(0, 0, 0, 0),
        RED(100, 255, 0, 0),
        GREEN(100, 0, 255, 0),
        GOLD(100, 214, 155, 26),
        WHITE(100, 255, 255, 255);

        int alpha, red, green, blue;

        BubbleColor(int alpha, int red, int green, int blue) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }
    BubbleColor levelEndBubbleColor;

    /**
     * No-argument constructor. The scoreBubblePool is created in advance and configured as
     * are needed.
     */
    ScoreBubblePool(Bitmap source) {
        this.digitsSource = source;
        playAreaInfo = null;
        scoreBubbles = new ScoreBubble[POOL_SIZE];
        for (int i = 0; i < scoreBubbles.length; i++) {
            scoreBubbles[i] = new ScoreBubble();
        }
        opacityPaint = new Paint();
        circleBorderPaint = new Paint();
        circleBorderPaint.setARGB(100, 255, 255, 255);
        circleBorderPaint.setStyle(Paint.Style.STROKE);
        circleBorderPaint.setAntiAlias(true);
        circleBorderPaint.setStrokeWidth(BORDER_CIRCLE_THICKNESS);
        bubbleBackgroundPaint = new Paint();
        bubbleBackgroundPaint.setAntiAlias(true);
        bubbleBackgroundPaint.setStyle(Paint.Style.FILL);

        bubbleSource = new Rect();
        bubbleDest = new RectF();
        levelEndBubbleDest = new RectF();
        levelEndBubbleColor = BubbleColor.WHITE;

        bubbleBackgroundMap = new HashMap<BubbleColor, Bitmap>();
    }

    /**
     * Method takes a score and assigns it to one of the scoreBubbles in the pool, as long as there
     * is one available. It then sets that bubbles opacity to 255, and sets the x and y positions
     * based on the radius and orbitIndex of the target just eliminated.
     *
     * @param score int - the score to be displayed.
     * @param angle float - the angle that the target was at when eliminated.
     * @param orbitIndex - the index of the target counting from the outermost orbit.
     * @return false if the pool is fully used, true otherwise.
     */
    boolean activateNewScoreBubble(int score, float angle, int orbitIndex, BubbleColor color) {
        int counter = 0;
        while (counter < POOL_SIZE) {
            if (counter == POOL_SIZE - 1) {
                return false; // No unassigned bubble exists.
            } else if (!scoreBubbles[counter].on) {
                break; // An unassigned bubble has been found.
            }
            counter++;
        }
        float radius = playAreaInfo.effectiveDiameter / 2
                - ((orbitIndex + 2) * playAreaInfo.scaledTargetThickness);
        float x = playAreaInfo.xCenterOfCircle + (float)(radius * Math.cos(angle)) - bubbleDiameter / 2;
        float y = playAreaInfo.yCenterOfCircle + (float)(radius * Math.sin(angle)) - bubbleDiameter / 2;
        configureScoreBubble(counter, score, x, y, color);
        return true;
    }

    /**
     * Method iterates through each ScoreBubble in the pool and, after running the countdown to
     * zero, decrements the alphaComponent to zero.
     */
    void updateScoreBubbles() {
        for (ScoreBubble bub : scoreBubbles) {
            if (bub.on) {
                if (bub.countdownUntilFade > 0) {
                    bub.countdownUntilFade--;
                } else if (bub.alphaComponent > 0) {
                    bub.alphaComponent -= ALPHA_DECREMENT;
                }
            }
            if (bub.alphaComponent <= 0) {
                bub.alphaComponent = 0;
                bub.on = false;
                bub.countdownUntilFade = DISPLAY_TIME;
            }
        }
    }

    /**
     * Method takes a reference to the invoking renderer's canvas and draws all of the active
     * scoreBubbles on it, having first set the alpha channel on the bitmap to the
     * bubble's alphaComponent.
     *
     * @param canvas The canvas supplied by the invoking method for drawing on.
     */
    void displayScoreBubbles(Canvas canvas) {
        for (ScoreBubble bubble : scoreBubbles) {
            if (bubble.on) {
                opacityPaint.setAlpha(bubble.alphaComponent);
                canvas.drawBitmap(
                        bubbleBackgroundMap.get(bubble.color),
                        bubble.xPos,
                        bubble.yPos,
                        opacityPaint);

                canvas.drawBitmap(
                        bubble.scoreBubbleSprite,
                        bubble.xPos,
                        bubble.yPos,
                        opacityPaint);

            }
        }
    }

    /**
     * Method is called by the gameSurfaceView's surfaceChanged() method, and informs the
     * scoreBubblePool of the relevant dimensions of the scree.
     *
     * @param pai Class containing the relevant information about screen dimensions.
     */
    void onSurfaceChanged(PlayAreaInfo pai) {
        this.playAreaInfo = pai;
        bubbleDiameter = IntRepConsts.SCORE_BUBBLE_DIAMETER_TO_CIRCLE_RATIO * playAreaInfo.scaledDiameter;
        levelEndBubbleDiameter = IntRepConsts.COUNTDOWN_YSIZE * 0.9f * playAreaInfo.topPanelHeight;
        glyphHeight = bubbleDiameter / 1.5f;
        glyphWidth = glyphHeight * 0.5f;

        // Create the source graphic for the scoreBubbles.
        bubbleSourceGraphics = Bitmap.createBitmap(
                (int) (bubbleDiameter * 10),
                (int) (bubbleDiameter * 2),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bubbleSourceGraphics);
        Rect source = new Rect();
        RectF dest = new RectF();
        int digitSourceWidth = digitsSource.getWidth();
        int digitSourceHeight = digitsSource.getHeight() / 13;

        // Draw the glyph combinations. Each combination (1 or 2 glyphs) occupies a square
        // inscribed in the scoreBubble circle.

        // First draw the single digit scores.
        for (int i = 0; i < 10; i++) {
            // Temp - draw a circle to check position.
            canvas.drawCircle(
                    (i + 0.5f) * bubbleDiameter,
                    bubbleDiameter / 2,
                    bubbleDiameter / 2 - BORDER_CIRCLE_THICKNESS,
                    circleBorderPaint);
            source.set(
                    0,
                    i * digitSourceHeight,
                    digitSourceWidth - 1,
                    (i + 1) * digitSourceHeight - 1);
            dest.set(
                    (i + 0.5f) * bubbleDiameter - (glyphWidth / 2),
                    bubbleDiameter / 2 - (glyphHeight / 2),
                    (i + 0.5f) * bubbleDiameter + (glyphWidth / 2),
                    bubbleDiameter / 2 + (glyphHeight / 2));
            canvas.drawBitmap(
                    digitsSource,
                    source,
                    dest,
                    null);
        }
        // Next draw the double digit scores.
        for (int j = 0; j < 10; j++) {
            canvas.drawCircle(
                    (j + 0.5f) * bubbleDiameter,
                    bubbleDiameter * 3 / 2,
                    bubbleDiameter / 2 - BORDER_CIRCLE_THICKNESS,
                    circleBorderPaint);
            source.set(
                    0,
                    j * digitSourceHeight,
                    digitSourceWidth - 1,
                    (j + 1) * digitSourceHeight - 1);
            dest.set(
                    (j + 0.5f) * bubbleDiameter - glyphWidth,
                    bubbleDiameter * 3 / 2 - glyphHeight / 2,
                    (j + 0.5f) * bubbleDiameter,
                    bubbleDiameter * 3 / 2 + glyphHeight / 2);
            canvas.drawBitmap(
                    digitsSource,
                    source,
                    dest,
                    null);
            dest.set(
                    (j + 0.5f) * bubbleDiameter,
                    bubbleDiameter * 3 / 2 - glyphHeight / 2,
                    (j + 0.5f) * bubbleDiameter + glyphWidth,
                    bubbleDiameter * 3 / 2 + glyphHeight / 2);
            canvas.drawBitmap(
                    digitsSource,
                    source,
                    dest,
                    null);
        }

        Log.d(TAG, "bubbleSourceGraphics bitmap size = "
                + String.valueOf(bubbleSourceGraphics.getWidth() * bubbleSourceGraphics.getHeight() * 4));

        // Create an empty bitmap for each scoreBubble, to be drawn on when
        // configureScoreBubble() is called.
        for (ScoreBubble b : scoreBubbles) {
            b.scoreBubbleSprite = Bitmap.createBitmap(
                    (int) bubbleDiameter,
                    (int) bubbleDiameter,
                    Bitmap.Config.ARGB_8888
            );
            b.workerCanvas = new Canvas(b.scoreBubbleSprite);
        }

        // Populate the bubbleBackgrounds map.
        for (BubbleColor color : BubbleColor.values()) {
            Bitmap bmp = Bitmap.createBitmap(
                    (int) bubbleDiameter,
                    (int) bubbleDiameter,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            if (color != BubbleColor.TRANSPARENT) {
                bubbleBackgroundPaint.setARGB(
                        color.alpha,
                        color.red,
                        color.green,
                        color.blue);
                c.drawCircle(bubbleDiameter / 2, bubbleDiameter / 2, bubbleDiameter / 2, bubbleBackgroundPaint);
            }
            bubbleBackgroundMap.put(color, bmp);
        }

        // Create the levelEndBubbleBackground.
        levelEndBubbleBackground = Bitmap.createBitmap(
                (int) levelEndBubbleDiameter,
                (int) levelEndBubbleDiameter,
                Bitmap.Config.ARGB_8888
        );
        Canvas c = new Canvas(levelEndBubbleBackground);

        bubbleBackgroundPaint.setARGB(
                levelEndBubbleColor.alpha,
                levelEndBubbleColor.red,
                levelEndBubbleColor.blue,
                levelEndBubbleColor.green
        );
        c.drawCircle(levelEndBubbleDiameter / 2, levelEndBubbleDiameter / 2, levelEndBubbleDiameter / 2, bubbleBackgroundPaint);

        digitsSource = null;
    }

    /**
     * Method configures an existing scoreBubble with a position and an appropriate Bitmap.
     * The opacityMask value is initialized to 0 (transparent).
     *
     * @param scoreValue The score that the bubble should display
     * @param xPos The initial xPosition of the bubble.
     * @param yPos The initial yPosition of the bubble.
     */
    void configureScoreBubble(int counter, int scoreValue, float xPos, float yPos, BubbleColor color) {

        scoreBubbles[counter].xPos = xPos;
        scoreBubbles[counter].yPos = yPos;
        scoreBubbles[counter].alphaComponent = 255;
        scoreBubbles[counter].color = color;

        // First, clear any existing pixels on the scoreBubble bitmap.
        scoreBubbles[counter].workerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int ones, tens;
        if (scoreValue > 99) {
            ones = 9;
            tens = 9;
        } else if (scoreValue < 0) {
            ones = 0;
            tens = 0;
        } else {
            ones = scoreValue % 10;
            tens = (int)(scoreValue / 10);
        }

        if (tens == 0) {
            // Draw a single digit in the center of the bubble.
            bubbleSource.set(
                    (int)(bubbleDiameter * ones),
                    0,
                    (int)(bubbleDiameter * (ones + 1) - 1),
                    (int)bubbleDiameter - 1);
            bubbleDest.set(0, 0, bubbleDiameter - 1, bubbleDiameter - 1);
            scoreBubbles[counter].workerCanvas.drawBitmap(
                    bubbleSourceGraphics,
                    bubbleSource,
                    bubbleDest,
                    null);
        } else {
            // Draw 2 digits in the bubble. First the tens on the left.
            bubbleSource.set(
                    (int)(bubbleDiameter * tens),
                    (int)bubbleDiameter,
                    (int)(bubbleDiameter * (tens + 0.5f)),
                    (int)(bubbleDiameter * 2) - 1);
            bubbleDest.set(0, 0, bubbleDiameter * 0.5f, bubbleDiameter);
            scoreBubbles[counter].workerCanvas.drawBitmap(
                    bubbleSourceGraphics,
                    bubbleSource,
                    bubbleDest,
                    null);
            // Now the ones on the right.
            bubbleSource.set(
                    (int)(bubbleDiameter * (ones + 0.5f)),
                    (int)bubbleDiameter,
                    (int)(bubbleDiameter * (ones + 1.0f)),
                    (int)(bubbleDiameter * 2) - 1);
            bubbleDest.set(
                    bubbleDiameter * 0.5f,
                    0,
                    bubbleDiameter,
                    bubbleDiameter);
            scoreBubbles[counter].workerCanvas.drawBitmap(
                    bubbleSourceGraphics,
                    bubbleSource,
                    bubbleDest,
                    null);
        }
        scoreBubbles[counter].on = true;


        //
    }
}
