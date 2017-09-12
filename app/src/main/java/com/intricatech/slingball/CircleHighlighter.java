package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Bolgbolg on 02/05/2017.
 */
public class CircleHighlighter {

    private String TAG;

    enum State {ON, FADING, FIRST_DRAW_OFF,  OFF, SUDDEN_DEATH_ON}
    State state;

    private int highlightColor, backgroundColor, suddenDeathColor;
    private float fadeIndex;
    private PlayAreaInfo playAreaInfo;
    private Paint circlePaint;
    private int red, green, blue;
    private int HIGH_RED, HIGH_GREEN, HIGH_BLUE;
    private int OFF_RED, OFF_GREEN, OFF_BLUE;
    private int S_DEATH_RED, S_DEATH_GREEN, S_DEATH_BLUE;
    private boolean shouldRedrawOnBB1, shouldRedrawOnBB2;

    private static final int ALPHA = 255;
    private static final float fadeDecrement = 0.04f;

    public CircleHighlighter(Resources resources) {

        TAG = getClass().getSimpleName();

        highlightColor = resources.getColor(R.color.outer_circle_highlighted);
        HIGH_RED = Color.red(highlightColor);
        HIGH_GREEN = Color.green(highlightColor);
        HIGH_BLUE = Color.blue(highlightColor);
        backgroundColor = resources.getColor(R.color.outer_circle);
        OFF_RED = Color.red(backgroundColor);
        OFF_GREEN = Color.green(backgroundColor);
        OFF_BLUE = Color.blue(backgroundColor);
        suddenDeathColor = resources.getColor(R.color.sudden_death_red);
        S_DEATH_RED = Color.red(suddenDeathColor);
        S_DEATH_GREEN = Color.green(suddenDeathColor);
        S_DEATH_BLUE = Color.blue(suddenDeathColor);
        fadeIndex = 0.0f;

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);

        state = State.FIRST_DRAW_OFF;
        shouldRedrawOnBB1 = true;
        shouldRedrawOnBB2 = true;
    }

    public void update() {

        switch (state) {
            case ON: // Not currently needed.
                if (true) {
                    state = State.FADING;
                    break;
                }

            case FADING: {
                fadeIndex -= fadeDecrement;
                if (fadeIndex < 0) {
                    state = State.FIRST_DRAW_OFF;
                }
                break;
            }

            case FIRST_DRAW_OFF: {
                if (!shouldRedrawOnBB1 && !shouldRedrawOnBB2) {
                    state = State.OFF;
                }
                break;
            }
            case OFF: {
                break;
            }
            case SUDDEN_DEATH_ON: {
                break;
            }
        }
    }

    public void fire() {
        fadeIndex = 1.0f;
        state = State.FADING;
        shouldRedrawOnBB1 = true;
        shouldRedrawOnBB2 = true;
    }

    public void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        this. playAreaInfo = playAreaInfo;
        circlePaint.setStrokeWidth(playAreaInfo.scaledOuterCircleThickness);
    }

    public void draw(Canvas canvas, boolean usingBB1) {
        if (shouldRedrawOnBB1 && usingBB1) {
            setCircleColor();
            canvas.drawCircle(
                    playAreaInfo.xCenterOfCircle,
                    playAreaInfo.yCenterOfCircle,
                    playAreaInfo.scaledDiameter * 0.5f - playAreaInfo.scaledOuterCircleThickness / 2,
                    circlePaint);
            if (state == State.FIRST_DRAW_OFF) {
                shouldRedrawOnBB1 = false;
            }
        }
        if (shouldRedrawOnBB2 && !usingBB1) {
            setCircleColor();
            canvas.drawCircle(
                    playAreaInfo.xCenterOfCircle,
                    playAreaInfo.yCenterOfCircle,
                    playAreaInfo.scaledDiameter * 0.5f - playAreaInfo.scaledOuterCircleThickness / 2,
                    circlePaint);
        }

        if (state == State.FIRST_DRAW_OFF) {
            shouldRedrawOnBB2 = false;
        }
    }

    public void turnOnSuddenDeath() {
        shouldRedrawOnBB2 = true;
        shouldRedrawOnBB1 = true;
        state = State.SUDDEN_DEATH_ON;
    }

    public void turnOffSuddenDeath() {
        shouldRedrawOnBB2 = true;
        shouldRedrawOnBB1 = true;
        state = State.OFF;
        fadeIndex = 0;
    }

    private void setCircleColor() {

        if (state != State.SUDDEN_DEATH_ON) {
            red = OFF_RED + (int) ((HIGH_RED - OFF_RED) * fadeIndex);
            green = OFF_GREEN + (int) ((HIGH_GREEN - OFF_GREEN) * fadeIndex);
            blue = OFF_BLUE + (int) ((HIGH_BLUE - OFF_BLUE) * fadeIndex);
        } else {
            red = S_DEATH_RED;
            green = S_DEATH_GREEN;
            blue = S_DEATH_BLUE;
        }

        circlePaint.setARGB(ALPHA, red, green, blue);
    }
}
