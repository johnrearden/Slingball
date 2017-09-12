package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by Bolgbolg on 11/03/2017.
 */
public class BonusDisplayer {

    private PlayAreaInfo playAreaInfo;
    private TapToContinueMonitor tapToContinueMonitor;

    public boolean readyForAd;

    private Bitmap bonusPanel;
    private Bitmap shownBonusPanel;
    private Canvas bonusPanelCanvas;
    private Bitmap background;
    private Bitmap backgroundSubset;
    private Bitmap starOnBitmap, starOffBitmap;
    private Bitmap textBoxBitmap, scaledTextBoxBitmap;
    private Canvas utilityCanvas;
    private int opacity;

    private int hitsInARow;
    private float timeLeftOver;
    private int misses;
    private int numberOfStars;
    private DecimalFormat hitRateFormat = new DecimalFormat("0.#");
    int bonus;

    private static final float OVERALL_HEIGHT_RATIO = 0.5f;
    private static final float OVERALL_WIDTH_RATIO = 0.6f;
    private static final float TITLE_HGT = 5.0f;
    private static final float BONUS_ELEMENT_HEIGHT = 2.3f;
    private static final float BONUS_TOTAL_HEIGHT = 3.0f;
    private static final float STAR_DISPLAY_HEIGHT = 4.0f;
    private static final float PROMPT_HEIGHT = 2.0f;
    private static final float ELEMENT_TO_OVERALL_WIDTH_RATIO = 0.65f;
    private static final float TOTAL_OF_TEXT_HEIGHTS =
            TITLE_HGT + (BONUS_ELEMENT_HEIGHT * 4) + PROMPT_HEIGHT + STAR_DISPLAY_HEIGHT;
    private static final float INNER_PADDING = 0.028f; // 0.028
    private static final float OUTER_PADDING = 0.1f;

    private static final String TITLE_STRING = "Level Complete";
    private static final String HITS_IN_A_ROW_STRING = "Best run :";
    private static final String MISSES_STRING = "Misses :";
    private static final String TIME_LEFTOVER_STRING = "Time left :";
    private static final String BONUS_STRING = "Bonus :";
    private static final String PROMPT_STRING = "TAP TO CONTINUE";
    private static final String LEVEL_HIT_RATE = "Hit Rate (10 sec) : ";

    private Paint titlePaint, titleBackgroundPaint, elementPaint, promptPaint;
    private Paint panelPaint;

    private float panelXPos, panelYPos;
    private float panelWidth, panelHeight, halfPanelWidth;
    private float titleHeight, bonusElementHeight, promptHeight, starHeight, bonusTotalHeight;
    private float titleHeightNoPad, bonusElementHeightNoPad, promptHeightNoPad, starHeightNoPad, bonusTotalHeightNoPad;
    private float bonusElementWidth;
    private float innerPadding, outerPadding, innerPaddingDoubled, outerPaddingDoubled;

    // experimental fields for rotating bonusDisplayer.
    public float bonusPanelController;
    private float bonusWidthIncrement, bonusWidthIncrementPrime;
    private static final int TOTAL_PANEL_ROTATION = 180;

    enum DisplayState {
        OFF,
        APPEARING,
        VISIBLE,
        DISAPPEARING,
        READY_FOR_AD
    }
    DisplayState displayState;


    BonusDisplayer(Resources resources) {

        displayState = DisplayState.OFF;
        readyForAd = false;

        starOnBitmap = BitmapFactory.decodeResource(resources, R.drawable.bonus_star_on);
        starOffBitmap = BitmapFactory.decodeResource(resources, R.drawable.bonus_star_off);
        background = BitmapFactory.decodeResource(resources, R.drawable.bonus_displayer_bckbox);

        panelPaint = new Paint();
        panelPaint.setARGB(255, 80, 80, 80);
        panelPaint.setStyle(Paint.Style.FILL);

        titlePaint = new Paint();
        titlePaint.setARGB(255, 200, 200, 200);
        titlePaint.setTextSize(50);
        titlePaint.setAntiAlias(true);
        titlePaint.setFakeBoldText(true);

        titleBackgroundPaint = new Paint();
        titleBackgroundPaint.setStyle(Paint.Style.FILL);
        titleBackgroundPaint.setARGB(255, 120, 120, 120);

        elementPaint = new Paint();
        elementPaint.setARGB(255, 200, 200, 200);
        elementPaint.setTextSize(50);
        elementPaint.setAntiAlias(true);
        elementPaint.setTextSkewX(-0.15f);

        promptPaint = new Paint();

        bonusPanelController = 0.0f;
        bonusWidthIncrement = 0.01f;
        bonusWidthIncrementPrime = 0.00175f;
    }

    void updateDisplay(boolean fingerDown) {
        switch (displayState) {
            case APPEARING: {
                if (bonusPanelController < 1.0f) {
                    bonusPanelController += bonusWidthIncrement;
                    bonusWidthIncrement += bonusWidthIncrementPrime;
                    panelXPos = playAreaInfo.screenWidth / 2
                            - panelWidth * 0.5f * bonusPanelController
                            + playAreaInfo.screenWidth / 2 * (1 - bonusPanelController);
                    panelYPos = playAreaInfo.screenHeight / 2
                            - panelHeight * 0.5f * bonusPanelController
                            + playAreaInfo.screenHeight / 2 * (1 - bonusPanelController);
                }
                if (bonusPanelController > 1.0f) {
                    bonusPanelController = 1.0f;
                    displayState = DisplayState.VISIBLE;
                    tapToContinueMonitor = new TapToContinueMonitor();
                }
                break;
            }
            case VISIBLE : {
                if (tapToContinueMonitor.hasTapOccurred(fingerDown)) {
                    displayState = DisplayState.READY_FOR_AD;
                }
                break;
            }
            case READY_FOR_AD: {
                break; // Note : transition to DISAPPEARING is handled by physicsAndBGRenderer.
            }
            case DISAPPEARING: {
                if (bonusPanelController > 0.0f) {
                    bonusPanelController -= bonusWidthIncrement;
                    bonusWidthIncrement -= bonusWidthIncrementPrime;
                    panelXPos = playAreaInfo.screenWidth / 2
                            - panelWidth * 0.5f * bonusPanelController
                            - playAreaInfo.screenWidth / 2 * (1 - bonusPanelController);
                    panelYPos = playAreaInfo.screenHeight / 2
                            - panelHeight * 0.5f * bonusPanelController
                            + playAreaInfo.screenHeight / 2 * (1 - bonusPanelController);
                }
                if (bonusPanelController <= 0.0f) {
                    bonusPanelController = 0.0f;
                    displayState = DisplayState.OFF;
                }
                break;
            }
            case OFF :
            default  : {

            }
        }

    }

    void configureDisplayAndMakeVisible(float levelHitRate, int hitsInARow, float timeLeftOver, int misses, int numberOfStars, int bonus) {
        this.hitsInARow = hitsInARow;
        this.timeLeftOver = timeLeftOver;
        this.misses = misses;
        this.numberOfStars = numberOfStars;
        this.bonus = bonus;

        bonusPanelController = 0.0f;
        bonusWidthIncrement = 0.01f;
        bonusWidthIncrementPrime = 0.00175f;

        // Erase the previous figures.
        bonusPanelCanvas.drawBitmap(
                backgroundSubset,
                (int) bonusElementWidth,
                (int) titleHeightNoPad,
                null
        );
        // Draw the hits-in-a-row number.
        String formattedString = hitRateFormat.format(levelHitRate);
        Bitmap toDraw = scaleTextBox(
                drawTextBox(formattedString, elementPaint),
                bonusElementHeight,
                panelWidth - bonusElementWidth - innerPadding
        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth + innerPadding,
                titleHeightNoPad + bonusElementHeightNoPad / 2 - toDraw.getHeight() / 2,
                null
        );
        // Draw the time left number.
        toDraw = scaleTextBox(
                drawTextBox(String.format("%.1f" + "s" +
                        "", timeLeftOver), elementPaint),
                bonusElementHeight,
                panelWidth - bonusElementWidth - innerPadding        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth + innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 3) - toDraw.getHeight() / 2,
                null
        );

        // Draw the misses text.
        if (misses > 0) {
            elementPaint.setARGB(255, 255, 60, 60);
        }

        toDraw = scaleTextBox(
                drawTextBox(String.valueOf(misses), elementPaint),
                bonusElementHeight,
                panelWidth - bonusElementWidth - innerPadding);
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth + innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 5) - toDraw.getHeight() / 2,
                null
        );
        elementPaint.setARGB(255, 200, 200, 200);

        // Draw the bonus total text.
        toDraw = scaleTextBox(
                drawTextBox(String.valueOf(bonus), elementPaint),
                bonusTotalHeight,
                panelWidth - bonusElementWidth - innerPadding);
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth + innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 7) - toDraw.getHeight() / 2,
                null
        );
        // Draw the darkened stars.
        float starYPos = titleHeightNoPad + (bonusElementHeightNoPad * 3) + bonusTotalHeightNoPad + innerPadding;
        float starXPos;
        for (int i = 0; i < 5; i++) {
            starXPos = halfPanelWidth - (starHeight * 2.5f) + (starHeight * i);
            bonusPanelCanvas.drawBitmap(
                    starOffBitmap,
                    starXPos,
                    starYPos,
                    null
            );
        }
        for (int i = 0; i < numberOfStars; i++) {
            starXPos = halfPanelWidth - (starHeight * 2.5f) + (starHeight * i);
            bonusPanelCanvas.drawBitmap(
                    starOnBitmap,
                    starXPos,
                    starYPos,
                    null
            );
        }

        displayState = DisplayState.APPEARING;
    }

    void drawDisplay(Canvas canvas) {

        canvas.drawARGB((int) (180 * bonusPanelController), 0, 0, 0);

        if (bonusPanelController != 1.0f) {
            int w = (int) (bonusPanel.getWidth() * bonusPanelController);
            int h = (int) (bonusPanel.getHeight() * bonusPanelController);
            shownBonusPanel = Bitmap.createScaledBitmap(
                    bonusPanel,
                    w > 0 ? w : 1,
                    h > 0 ? h : 1,
                    false
            );
        } else shownBonusPanel = bonusPanel;
        float amountToRotate =  TOTAL_PANEL_ROTATION- (TOTAL_PANEL_ROTATION * bonusPanelController);
        canvas.rotate(
                amountToRotate,
                playAreaInfo.screenWidth / 2,
                playAreaInfo.screenHeight / 2);
        canvas.drawBitmap(
                shownBonusPanel,
                panelXPos,
                panelYPos,
                panelPaint
        );
        canvas.rotate(
                -amountToRotate,
                playAreaInfo.screenWidth / 2,
                playAreaInfo.screenHeight / 2);
    }

    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {

        this.playAreaInfo = playAreaInfo;

        panelWidth = playAreaInfo.screenWidth * OVERALL_WIDTH_RATIO;
        halfPanelWidth = panelWidth * 0.5f;
        panelHeight = playAreaInfo.screenHeight * OVERALL_HEIGHT_RATIO;
        panelXPos = playAreaInfo.screenWidth * 0.5f - panelWidth * 0.5f;
        panelYPos = playAreaInfo.screenHeight * 0.5f - panelHeight * 0.5f;

        panelWidth = panelHeight = playAreaInfo.scaledDiameter / (float) Math.sqrt(2);
        panelXPos = playAreaInfo.screenWidth * 0.5f - panelWidth * 0.5f;
        panelYPos = playAreaInfo.yCenterOfCircle - panelHeight * 0.5f;
        halfPanelWidth = panelWidth * 0.5f;

        innerPadding = INNER_PADDING * panelHeight;
        innerPaddingDoubled = innerPadding * 2;

        bonusPanel = Bitmap.createBitmap((int) panelWidth, (int) panelHeight, Bitmap.Config.ARGB_8888);
        bonusPanelCanvas = new Canvas(bonusPanel);

        bonusElementHeight = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * BONUS_ELEMENT_HEIGHT) - innerPaddingDoubled;
        bonusElementHeightNoPad = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * BONUS_ELEMENT_HEIGHT);
        titleHeight = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * TITLE_HGT) - innerPaddingDoubled;
        titleHeightNoPad = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * TITLE_HGT);
        promptHeight = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * PROMPT_HEIGHT) - innerPaddingDoubled;
        promptHeightNoPad = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * PROMPT_HEIGHT);
        bonusTotalHeight = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * BONUS_TOTAL_HEIGHT) - innerPaddingDoubled;
        bonusTotalHeightNoPad = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * BONUS_TOTAL_HEIGHT);
        starHeight = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * STAR_DISPLAY_HEIGHT) - innerPaddingDoubled;
        starHeightNoPad = (panelHeight / TOTAL_OF_TEXT_HEIGHTS * STAR_DISPLAY_HEIGHT) - innerPaddingDoubled;
        bonusElementWidth = panelWidth * ELEMENT_TO_OVERALL_WIDTH_RATIO;


        starOnBitmap = Bitmap.createScaledBitmap(starOnBitmap, (int) starHeight, (int) starHeight, false);
        starOffBitmap = Bitmap.createScaledBitmap(starOffBitmap, (int) starHeight, (int) starHeight, false);
        background = Bitmap.createScaledBitmap(background, (int)(panelWidth - 1), (int)(panelHeight - 1), false);
        backgroundSubset = Bitmap.createBitmap(
                background,
                (int) bonusElementWidth,
                (int) titleHeightNoPad,
                (int) (panelWidth - (bonusElementWidth + 1)),
                (int) (titleHeightNoPad + 4 * bonusElementHeight));

        bonusPanelCanvas.drawBitmap(background, 0, 0, null);

        // Draw the title text.
        Bitmap toDraw = scaleTextBox(
                drawTextBox(TITLE_STRING, titlePaint),
                titleHeight,
                panelWidth * 0.9f - innerPaddingDoubled);
        //bonusPanelCanvas.drawRect(0, 0, panelWidth, titleHeightNoPad, titleBackgroundPaint);
        bonusPanelCanvas.drawBitmap(
                toDraw,
                halfPanelWidth - toDraw.getWidth() * 0.5f,
                titleHeightNoPad / 2 - toDraw.getHeight() * 0.5f,
                null);

        // Draw the hits-in-a-row text.
        toDraw = scaleTextBox(
                drawTextBox(LEVEL_HIT_RATE, elementPaint),
                bonusElementHeight,
                bonusElementWidth - innerPadding
        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth - toDraw.getWidth() - innerPadding,
                titleHeightNoPad + bonusElementHeightNoPad / 2 - toDraw.getHeight() / 2,
                null
        );

        // Draw the time left text.
        toDraw = scaleTextBox(
                drawTextBox(TIME_LEFTOVER_STRING, elementPaint),
                bonusElementHeight,
                bonusElementWidth - innerPadding
        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth - toDraw.getWidth() - innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 3) - toDraw.getHeight() / 2,
                null
        );

        // Draw the misses text.
        toDraw = scaleTextBox(
                drawTextBox(MISSES_STRING, elementPaint),
                bonusElementHeight,
                bonusElementWidth - innerPadding
        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth - toDraw.getWidth() - innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 5) - toDraw.getHeight() / 2,
                null
        );

        // Draw the bonus total text.
        toDraw = scaleTextBox(
                drawTextBox(BONUS_STRING, elementPaint),
                bonusTotalHeight,
                bonusElementWidth - innerPadding
        );
        bonusPanelCanvas.drawBitmap(
                toDraw,
                bonusElementWidth - toDraw.getWidth() - innerPadding,
                titleHeightNoPad + (bonusElementHeightNoPad / 2 * 7) - toDraw.getHeight() / 2,
                null
        );

        // Draw the darkened stars.
        float starYPos = titleHeightNoPad + (bonusElementHeightNoPad * 3) + bonusTotalHeightNoPad + innerPadding;
        float starXPos;
        for (int i = 0; i < 5; i++) {
            starXPos = halfPanelWidth - (starHeight * 2.5f) + (starHeight * i);
            bonusPanelCanvas.drawBitmap(
                    starOffBitmap,
                    starXPos,
                    starYPos,
                    null
            );
        }
    }

    Bitmap drawTextBox(String text, Paint paint) {
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        int height = textBounds.height();
        float width = paint.measureText(text);

        textBoxBitmap = Bitmap.createBitmap((int) width + 1, height + 1, Bitmap.Config.ARGB_8888);
        utilityCanvas = new Canvas(textBoxBitmap);
        //utilityCanvas.drawARGB(50, 255, 255, 255);
        utilityCanvas.drawText(text, 0, height - (textBounds.bottom), paint);

        return textBoxBitmap;
    }

    Bitmap scaleTextBox(Bitmap source, float requiredHeight, float widthAvailable) {
        float sourceHeight = (float) source.getHeight();
        float sourceWidth = (float) source.getWidth();
        float scaleFactor = requiredHeight / sourceHeight;
        if (sourceWidth * scaleFactor > widthAvailable) {
            Log.d(getClass().getSimpleName(), "text too wide");
            scaleFactor = widthAvailable / sourceWidth;
        }
        scaledTextBoxBitmap = Bitmap.createScaledBitmap(
                source,
                (int) (sourceWidth * scaleFactor),
                (int) (sourceHeight * scaleFactor),
                false
        );

        return scaledTextBoxBitmap;
    }

}
