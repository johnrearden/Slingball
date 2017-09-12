package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by Bolgbolg on 10/01/2017.
 */
public class ControlButton {

    /**
     * This class provides the GameSurfaceView with a pair of identical (appearance and behaviour)
     * buttons to indicate to the player where to place his finger/thumb. The touch detection can
     * afford to be liberal in its reaction to off button touches as there is nothing else to
     * the interface.
     */

    enum ButtonState {
        FLASHING,
        WAITING_FOR_COUNTDOWN,
        NOT_FLASHING
    }
    ButtonState buttonState;

    Resources resources;

    private boolean flashOn;
    private PhysicsAndBGRenderer physicsAndBGRenderer;
    private static final int FLASH_ON_INTERVAL = 40;
    private static final int FLASH_OFF_INTERVAL = 30;
    private static final int PROMPT_COUNTDOWN_INITIAL_VALUE = 40;
    private int flashCountdown;
    private int countdownToPlayerPrompt;

    private float button1XPos, button1YPos, button2XPos, button2YPos;
    private float buttonWidth;
    private float marginWidth;

    static final float MARGIN_RATIO = 0.05f;
    static final float BUTTON_WIDTH_TO_SCREEN_WIDTH_RATIO = 0.15f;
    static final float EXTRA_WIDTH_RATIO = 0.2f;

    private Bitmap onBitmap, offBitmap;

    public ControlButton(Resources resources, PhysicsAndBGRenderer phy) {

        this.resources = resources;
        this.physicsAndBGRenderer = phy;

        buttonState = ButtonState.FLASHING;
        flashOn = false;
        flashCountdown = FLASH_OFF_INTERVAL;
    }

    public void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        buttonWidth = playAreaInfo.screenWidth * BUTTON_WIDTH_TO_SCREEN_WIDTH_RATIO;
        marginWidth = playAreaInfo.screenWidth * MARGIN_RATIO;
        float extraWidth = buttonWidth * EXTRA_WIDTH_RATIO;
        button1YPos = playAreaInfo.screenHeight - marginWidth - buttonWidth;
        button2YPos = button1YPos;
        button1XPos = marginWidth;
        button2XPos = playAreaInfo.screenWidth - marginWidth - buttonWidth;

        onBitmap = BitmapFactory.decodeResource(resources, R.drawable.control_button_on);
        offBitmap = BitmapFactory.decodeResource(resources, R.drawable.control_button_off);

        onBitmap = Bitmap.createScaledBitmap(
                onBitmap,
                (int) buttonWidth,
                (int) buttonWidth,
                false
        );
        offBitmap = Bitmap.createScaledBitmap(
                offBitmap,
                (int) buttonWidth,
                (int) buttonWidth,
                false
        );
        physicsAndBGRenderer.setControlButton1Bounds(
                0,
                button1YPos - extraWidth,
                button1XPos + buttonWidth + extraWidth * 2,
                playAreaInfo.screenHeight
        );
        physicsAndBGRenderer.setControlButton2Bounds(
                button2XPos - extraWidth,
                button2YPos - extraWidth,
                playAreaInfo.screenWidth,
                playAreaInfo.screenHeight
        );
    }

    public void drawButtons(Canvas canvas) {
        if (flashOn) {
            canvas.drawBitmap(onBitmap, button1XPos, button1YPos, null);
            canvas.drawBitmap(onBitmap, button2XPos, button2YPos, null);
        } else {
            canvas.drawBitmap(offBitmap, button1XPos, button1YPos, null);
            canvas.drawBitmap(offBitmap, button2XPos, button2YPos, null);
        }
    }

    public void update(boolean fingerDown) {
        switch (buttonState) {
            case FLASHING: {
                if (fingerDown) {
                    buttonState = ButtonState.NOT_FLASHING;
                    flashOn = true;
                } else {
                    if (flashCountdown-- <= 0) {
                        flashOn = !flashOn;
                        flashCountdown = flashOn ? FLASH_ON_INTERVAL : FLASH_OFF_INTERVAL;
                    }
                }
                break;
            }
            case WAITING_FOR_COUNTDOWN: {
                if (fingerDown) {
                    buttonState = ButtonState.NOT_FLASHING;
                    flashOn = false;
                } else {
                    if (countdownToPlayerPrompt-- <= 0) {
                        buttonState = ButtonState.FLASHING;
                        flashOn = true;
                        flashCountdown = FLASH_ON_INTERVAL;
                    }
                }
                break;
            }
            case NOT_FLASHING: {
                if (!fingerDown) {
                    buttonState = ButtonState.WAITING_FOR_COUNTDOWN;
                    flashOn = false;
                    countdownToPlayerPrompt = PROMPT_COUNTDOWN_INITIAL_VALUE;
                } else {
                    flashOn = true;
                }
                break;
            }
            default: {
                Log.d(getClass().getSimpleName().toString(), "update() : switch statement fell" +
                        "through to default");

            }
        }
    }
}
