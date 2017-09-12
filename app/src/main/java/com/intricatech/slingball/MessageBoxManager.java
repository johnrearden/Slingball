package com.intricatech.slingball;

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
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bolgbolg on 08/08/2016.
 */
public class MessageBoxManager implements LevelChangeObserver {

    Resources resources;
    LevelChangeDirector levelManager;
    PlayAreaInfo playAreaInfo;
    static final String TAG = "MessageBoxManager";
    static final int TEXT_SIZE = 80;

    Canvas canvas;
    Map<MessageType, MessageBox> messageMap;
    Paint textPaint;
    int defaultColor;
    int redColor;
    int autopilotColor, shieldColor;
    Paint brightnessModifier;
    Paint blankTransparentPaint;
    Rect textBounds;

    static final String GAME_OVER = "GAME OVER";
    static final String OUT_OF_TIME = "Out of time!";
    static final String OUT_OF_ENERGY = "Out of energy!";
    static final String THREE_IN_A_ROW_BONUS_POINTS = "+ " + String.valueOf((int) IntRepConsts.SCORE_VALUE_OF_3_IN_A_ROW);
    static final String AUTOPILOT = "AUTOPILOT";
    static final String AUTOPILOT_TAKE_OVER = "Take Over....";
    static final String NEW_LEVEL_STRING = "Level";
    static final String TIME_BONUS_STRING = "Time Bonus";
    static final String GET_READY_STRING = "Get Ready ...";
    static final String SHIELD_STRING = "SHIELDED";
    static final String SUDDEN_DEATH_STRING = "HEAVY DAMAGE!";
    static final String LEVEL_20_1 = "The Infinite Level";
    static final String LEVEL_20_2 = "The level that goes on forever...";
    static final String MISSES = "Misses :";
    static final String GHOSTING = "BLOCKERS OFF";

    static final float COUNTDOWN_XCEN = 0.5f;
    static final float COUNTDOWN_YCEN = 0.7f;
    static final float COUNTDOWN_XSIZE = 0.2f;
    static final float COUNTDOWN_YSIZE = 0.4f;
    static final float MISSES_XCEN = 0.5f;
    static final float MISSES_YCEN = 0.7f;
    static final float MISSES_XSIZE = 0.3f;
    static final float MISSES_YSIZE = 0.5f;
    RectF autopilotDigitRect;
    RectF ghostBlockerDigitRect;
    static final float LEVEL_XCEN = 0.5f;
    static final float LEVEL_YCEN = 0.5f;
    static final float LEVEL_XSIZE = 0.25f;
    static final float LEVEL_YSIZE = 0.32f;
    RectF levelDigitRect;
    static final float TIME_BONUS_XRIGHT = 0.7f;
    static final float TIME_BONUS_XLEFT = 0.3f;
    static final float TIME_BONUS_XCENTER = (TIME_BONUS_XLEFT + TIME_BONUS_XRIGHT) / 2;
    static final float TIME_BONUS_YCEN = 0.6f;
    static final float TIME_BONUS_XSIZE = 0.7f;
    static final float TIME_BONUS_YSIZE = 0.2f;
    RectF timeBonusRect;

    int currentAutopilotCountdownDisplayed;
    int currentShieldCountdownDisplayed;
    int currentSuddenDeathCountdownDisplayed;
    int currentGhostBlockersCountdownDisplayed;
    int currentLevelDisplayed;
    int currentMissesDisplayed;
    int timeBonus;
    boolean isFirstLevelOrResume;

    private MessageType[] cachedValuesArray;

    // Icons :
    Bitmap skullAndCrossbones;


    enum MessageType {
        GAME_OVER_TIME_UP(MessageColor.DEFAULT),
        GAME_OVER_OUT_OF_ENERGY(MessageColor.DEFAULT),
        THREE_IN_A_ROW(MessageColor.DEFAULT),
        AUTOPILOT(MessageColor.AUTOPILOT),
        AUTOPILOT_TAKE(MessageColor.AUTOPILOT),
        NEW_LEVEL_BOX(MessageColor.DEFAULT),
        TIME_BONUS_BOX(MessageColor.DEFAULT),
        GET_READY(MessageColor.DEFAULT),
        LAST_LEVEL(MessageColor.DEFAULT),
        SHIELD(MessageColor.SHIELD),
        SUDDEN_DEATH(MessageColor.RED),
        MISSES(MessageColor.RED),
        GHOST_BLOCKERS(MessageColor.DEFAULT);

        MessageColor messageColor;

        MessageType(MessageColor color) {this.messageColor = color;}
    }

    enum MessageColor {
        DEFAULT,
        RED,
        AUTOPILOT,
        SHIELD
    }

    enum MessageSize {
        SMALL_WITHIN_ORBIT((float) IntRepConsts.PREFERRED_ORBIT_RADIUS / (IntRepConsts.DIAMETER * 0.5f) / 1.4142f),
        MEDIUM_40PC_DIAMETER(0.4f),
        LARGE_50PC_DIAMETER(0.5f),
        EXTRA_SMALL(0.2f);

        float relativeSize;
        MessageSize(float size) {
            this.relativeSize = size;
        }
    }

    enum FadeType {
        FADING_FAST(60, 8.0f),
        FADING_VERY_FAST(30, 8.0f),
        FADING_SLOW(60, 4.0f),
        FADING_VERY_SLOW(60, 2.0f),
        CONSTANT(0, 0);

        int initCountdown;
        float brightnessDec;
        FadeType(int init, float dec) {
            this.initCountdown = init;
            this.brightnessDec = dec;
        }
    }

    class MessageBox {
        Bitmap messageBitmap;
        final MessageType messageType;
        final MessageSize size;
        final FadeType fadeType;
        boolean fading;
        int constantCountdown;
        float brightnessDecrement;
        boolean on;
        float xPos, yPos;
        float xSize, ySize;
        float brightness;
        float priority;

        MessageBox(MessageType type, MessageSize size, FadeType fadeType) {
            this.messageType = type;
            this.size = size;
            this.fadeType = fadeType;
            constantCountdown = fadeType.initCountdown;
            brightnessDecrement = fadeType.brightnessDec;
            fading = false;
            on = false;
            xSize = playAreaInfo.scaledDiameter * size.relativeSize;
            ySize = xSize;
            messageBitmap = Bitmap.createBitmap((int) xSize, (int) ySize, Bitmap.Config.ARGB_8888);
            brightness = 255.0f;
            xPos = playAreaInfo.xCenterOfCircle - xSize / 2;
            yPos = playAreaInfo.yCenterOfCircle - ySize / 2;
            Canvas c = new Canvas(messageBitmap);

        }
    }

    MessageBoxManager(Resources resources, LevelChangeDirector levelManager) {

        cachedValuesArray = MessageType.values();
        this.resources = resources;
        this.levelManager = levelManager;
        messageMap = new HashMap<MessageType, MessageBox>();
        textBounds = new Rect();

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        defaultColor = resources.getColor(R.color.messagetextcolor);
        redColor = resources.getColor(R.color.sudden_death_red);
        autopilotColor = resources.getColor(R.color.reward_autopilot);
        shieldColor = resources.getColor(R.color.reward_shield);
        textPaint.setColor(defaultColor);

        blankTransparentPaint = new Paint();
        blankTransparentPaint.setColor(Color.TRANSPARENT);
        blankTransparentPaint.setStyle(Paint.Style.FILL);
        blankTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        brightnessModifier = new Paint();
        brightnessModifier.setAlpha(255);

        skullAndCrossbones = BitmapFactory.decodeResource(resources, R.drawable.skullandcrossbones);
        currentAutopilotCountdownDisplayed = 0;
        currentShieldCountdownDisplayed = 0;
        currentSuddenDeathCountdownDisplayed = 0;
        currentMissesDisplayed = 0;
        currentGhostBlockersCountdownDisplayed = 0;
        autopilotDigitRect = new RectF();
        ghostBlockerDigitRect = new RectF();

        currentLevelDisplayed = 0;
        levelDigitRect = new RectF();
        timeBonusRect = new RectF();

        levelManager.register(this);
    }

    void update() {
        for (MessageType type : cachedValuesArray) {
            MessageBox box = messageMap.get(type);
            if (box.on) {
                switch (box.fadeType) {
                    case CONSTANT: {
                        continue;
                    }
                    case FADING_VERY_FAST:
                    case FADING_FAST:
                    case FADING_SLOW:
                    case FADING_VERY_SLOW:{
                        if (!box.fading) {
                            if (box.constantCountdown-- <= 0) {
                                box.fading = true;
                            }
                        } else {
                            box.brightness -= box.brightnessDecrement;
                            if (box.brightness <= 0) {
                                box.on = false;
                                box.constantCountdown = box.fadeType.initCountdown;
                                box.brightness = 255;
                                box.fading = false;
                            }
                        }
                        break;
                    }
                    default : {
                        Log.d(TAG, "update() switch on fadeType fell through to default");
                    }
                }
            }
        }
    }


    void onSurfaceChanged (PlayAreaInfo playAreaInfo) {
        this.playAreaInfo = playAreaInfo;

        textPaint.setTextSize(TEXT_SIZE);

        populateMessageMap();
    }

    MessageBox createMessageBox(MessageType type, MessageSize size, FadeType fadeType) {
        MessageBox creation = new MessageBox(type, size, fadeType);
        return creation;
    }

    void populateMessageMap () {
        for (MessageType type : cachedValuesArray) {
            switch (type) {
                case MISSES: {
                    MessageBox box = createMessageBox(
                            MessageType.MISSES,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.FADING_VERY_FAST
                    );
                    drawTextOnMessageBox(box, MISSES, 0.5f, 0.2f, 0.5f, 0.3f);
                    box.priority = 0.4f;
                    messageMap.put(MessageType.MISSES, box);
                    break;
                }
                case GAME_OVER_TIME_UP: {
                    MessageBox box = createMessageBox(
                            MessageType.GAME_OVER_TIME_UP,
                            MessageSize.LARGE_50PC_DIAMETER,
                            FadeType.CONSTANT);
                    drawImageOnMessageBox(box, skullAndCrossbones, 0.5f, 0.55f, 0.4f, 0.4f);
                    drawTextOnMessageBox(box, GAME_OVER, 0.5f, 0.17f, 0.8f, 0.25f);
                    drawTextOnMessageBox(box, OUT_OF_TIME, 0.5f, 0.875f, 0.5f, 0.13f);
                    box.priority = 1.0f;
                    messageMap.put(MessageType.GAME_OVER_TIME_UP, box);
                    break;
                }
                case GAME_OVER_OUT_OF_ENERGY: {
                    MessageBox box = createMessageBox(
                            MessageType.GAME_OVER_OUT_OF_ENERGY,
                            MessageSize.LARGE_50PC_DIAMETER,
                            FadeType.CONSTANT);
                    drawImageOnMessageBox(box, skullAndCrossbones, 0.5f, 0.55f, 0.4f, 0.4f);
                    drawTextOnMessageBox(box, GAME_OVER, 0.5f, 0.17f, 0.8f, 0.25f);
                    drawTextOnMessageBox(box, OUT_OF_ENERGY, 0.5f, 0.875f, 0.5f, 0.13f);
                    box.priority = 1.0f;
                    messageMap.put(MessageType.GAME_OVER_OUT_OF_ENERGY, box);
                    break;
                }
                case THREE_IN_A_ROW: {
                    MessageBox box = createMessageBox(
                            MessageType.THREE_IN_A_ROW,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.FADING_FAST);
                    drawTextOnMessageBox(box, "3", 0.5f, 0.2f, 0.2f, 0.4f);
                    drawTextOnMessageBox(box, "in a row", 0.5f, 0.5f, 0.4f, 0.1f);
                    drawTextOnMessageBox(box, THREE_IN_A_ROW_BONUS_POINTS, 0.5f, 0.7f, 0.45f, 0.17f);
                    box.priority = 0.2f;
                    messageMap.put(MessageType.THREE_IN_A_ROW, box);
                    break;
                }
                case AUTOPILOT: {
                    MessageBox box = createMessageBox(
                            MessageType.AUTOPILOT,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.CONSTANT);
                    drawTextOnMessageBox(box, AUTOPILOT, 0.5f, 0.25f, 0.7f, 0.2f);
                    box.priority = 0.5f;
                    messageMap.put(MessageType.AUTOPILOT, box);
                    break;
                }
                case SHIELD: {
                    MessageBox box = createMessageBox(
                            MessageType.SHIELD,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.CONSTANT
                    );
                    drawTextOnMessageBox(box, SHIELD_STRING, 0.5f, 0.25f, 0.7f, 0.2f);
                    box.priority = 0.5f;
                    messageMap.put(MessageType.SHIELD, box);
                    break;
                }
                case SUDDEN_DEATH : {
                    MessageBox box = createMessageBox(
                            MessageType.SUDDEN_DEATH,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.CONSTANT
                    );
                    drawTextOnMessageBox(box, SUDDEN_DEATH_STRING, 0.5f, 0.25f, 0.95f, 0.2f);
                    box.priority = 0.51f;
                    messageMap.put(MessageType.SUDDEN_DEATH, box);
                    break;
                }
                case GHOST_BLOCKERS: {
                    MessageBox box = createMessageBox(
                            MessageType.GHOST_BLOCKERS,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.CONSTANT
                    );
                    drawTextOnMessageBox(box, GHOSTING, 0.5f, 0.25f, 0.95f, 0.2f);
                    box.priority = 0.51f;
                    messageMap.put(MessageType.GHOST_BLOCKERS, box);
                    break;
                }
                case AUTOPILOT_TAKE: {
                    MessageBox box = createMessageBox(
                            MessageType.AUTOPILOT_TAKE,
                            MessageSize.SMALL_WITHIN_ORBIT,
                            FadeType.FADING_FAST
                    );
                    drawTextOnMessageBox(box, AUTOPILOT_TAKE_OVER, 0.5f, 0.5f, 0.8f, 0.3f);
                    box.priority = 0.5f;
                    messageMap.put(MessageType.AUTOPILOT_TAKE, box);
                    break;
                }
                case NEW_LEVEL_BOX: {
                    MessageBox box = createMessageBox(
                            MessageType.NEW_LEVEL_BOX,
                            MessageSize.LARGE_50PC_DIAMETER,
                            FadeType.FADING_SLOW
                    );
                    drawTextOnMessageBox(box, NEW_LEVEL_STRING, 0.5f, 0.15f, 0.4f, 0.1f);
                    box.priority = 0.9f;
                    messageMap.put(MessageType.NEW_LEVEL_BOX, box);
                    break;
                }
                case TIME_BONUS_BOX: {
                    MessageBox box = createMessageBox(
                            MessageType.TIME_BONUS_BOX,
                            MessageSize.LARGE_50PC_DIAMETER,
                            FadeType.FADING_SLOW
                    );
                    drawTextOnMessageBox(box, TIME_BONUS_STRING, 0.5f, 0.25f, 0.6f, 0.2f);
                    box.priority = 0.9f;
                    messageMap.put(MessageType.TIME_BONUS_BOX, box);
                    break;
                }
                case GET_READY: {
                    MessageBox box = createMessageBox(
                            MessageType.GET_READY,
                            MessageSize.MEDIUM_40PC_DIAMETER,
                            FadeType.FADING_SLOW
                    );
                    drawTextOnMessageBox(box, GET_READY_STRING, 0.5f, 0.5f, 0.7f, 0.25f);
                    box.priority = 0.91f;
                    box.on = true;
                    messageMap.put(MessageType.GET_READY, box);
                    break;
                }
                case LAST_LEVEL: {
                    MessageBox box = createMessageBox(
                            MessageType.LAST_LEVEL,
                            MessageSize.LARGE_50PC_DIAMETER,
                            FadeType.FADING_VERY_SLOW
                    );
                    drawTextOnMessageBox(box, LEVEL_20_1, 0.5f, 0.3f, 0.99f, 0.25f);
                    drawTextOnMessageBox(box, LEVEL_20_2, 0.5f, 0.7f, 0.9f, 0.18f);
                    box.priority = 0.91f;
                    messageMap.put(MessageType.LAST_LEVEL, box);
                    break;
                }
                default : {
                    Log.d(TAG, "populateMessageMap() switch statement fell through to default, type == " + type);
                }
            }
        }
    }

    void drawMessageBox(Canvas canvas, MessageBox box) {
        brightnessModifier.setAlpha((int) box.brightness);
        canvas.drawBitmap(
                box.messageBitmap,
                box.xPos,
                box.yPos,
                brightnessModifier);
    }

    void drawTopPriorityMessageBox(Canvas canvas) {
        MessageType boxTypeToDraw = null;
        float highestPriority = -1.0f;
        for (MessageType type : cachedValuesArray) {
            MessageBox box = messageMap.get(type);
            if (box.on && box.priority > highestPriority) {
                boxTypeToDraw = box.messageType;
                highestPriority = box.priority;
            }
        }
        if (highestPriority > -1.0f) {
            drawMessageBox(canvas, messageMap.get(boxTypeToDraw));
        }
    }

    /**
     * Method draws a String on a MessageBox, with position and size specified relative to the
     * size of the messageBox bitmap.
     *
     * @param box The MessageBox to be drawn on.
     * @param text The String of text to be drawn on the MessageBox.
     * @param x The relative horizontal position of the center of the text String (0 < x < 1.0f)
     * @param y The relative vertical position of the center of the text String (0 < y < 1.0f)
     * @param xProp The relative horizontal size of the String (0 < xProp < 1.0f)
     * @param yProp The relative vertical size of the String (0 < yProp < 1.0f)
     */
    void drawTextOnMessageBox(MessageBox box, String text, float x, float y, float xProp, float yProp) {

        switch(box.messageType.messageColor) {

            case DEFAULT:
                textPaint.setColor(defaultColor);
                break;
            case RED:
                textPaint.setColor(redColor);
                break;
            case AUTOPILOT:
                textPaint.setColor(autopilotColor);
                break;
            case SHIELD:
                textPaint.setColor(shieldColor);
                break;
        }
        Bitmap stringBmap = createTextBitmap(text, textPaint);

        Rect source = new Rect();
        source.set(0, 0, stringBmap.getWidth(), stringBmap.getHeight());

        RectF dest = new RectF();
        float wid = box.xSize * xProp;
        float hei = box.ySize * yProp;
        dest.set(
                (box.xSize * x) - wid / 2,
                (box.ySize * y) - hei / 2,
                (box.xSize * x) + wid / 2,
                (box.ySize * y) + hei / 2
        );
        canvas = new Canvas(box.messageBitmap);
        canvas.drawBitmap(stringBmap, source, dest, null);
    }

    /**
     * Method draws a Bitmap image on a MessageBox, with position and size specified relative to the
     * size of the messageBox bitmap. All size and position parameters should be N > 0 and N < 1.0f.
     *
     * @param box The messageBox provided to be drawn on.
     * @param bitmap The bitmap to be drawn.
     * @param x The horizontal position of the bitmap's center relative to the messageBox.
     * @param y The vertical position of the bitmap's center relative to the messageBox.
     * @param xProp The x-size of the bitmap relative to the size of the messageBox.
     * @param yProp The y-size of the bitmap relative to the size of the messageBox.
     */
    void drawImageOnMessageBox(MessageBox box, Bitmap bitmap, float x, float y, float xProp, float yProp) {
        Rect source = new Rect();
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());

        RectF dest = new RectF();
        float wid = box.xSize * xProp;
        float hei = box.ySize * yProp;
        dest.set(
                (box.xSize * x) - wid / 2,
                (box.ySize * y) - hei / 2,
                (box.xSize * x) + wid / 2,
                (box.ySize * y) + hei / 2
        );
        canvas = new Canvas(box.messageBitmap);
        canvas.drawBitmap(bitmap, source, dest, null);
    }


    /**
     * Method creates a Bitmap containing the text supplied as a parameter, using the characteristics
     * specified by the Paint object also supplied. The horizontal size of the bitmap is calculated using
     * the float-returning Paint.measureText(), while the vertical size of the bitmap must utilize
     * the int-returning Paint.getBounds() incremented once.
     *
     * @param text The String to be drawn.
     * @param paint The Paint object used to draw the String.
     * @return
     */
    Bitmap createTextBitmap(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int height = rect.bottom - rect.top + 1;
        float width = paint.measureText(text);
        Bitmap bitmap = Bitmap.createBitmap((int) width + 1, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        //c.drawARGB(100, 255, 255, 255);
        c.drawText(text, 0, height - (rect.bottom + 1), paint);
        rect = null;
        c = null;

        return bitmap;
    }

    /**
     * Method checks to see if the countdown digits currently shown match the digits passed in by the
     * autopilot itself. If so, it just returns. If not, it redraws the autopilot messageBox, first
     * blanking the area occupied by the digits, and then drawing the new digit(s).
     *
     * @param timeRem The time remaining as passed in by swingball.autopilot.
     */
    void updateAutopilot(int timeRem) {
        if (timeRem != currentAutopilotCountdownDisplayed) {
            float xSize;
            erasePreviousAutopilotDigits();
            // Redraw correct digit.
            currentAutopilotCountdownDisplayed = timeRem;
            xSize = timeRem != 0 ? COUNTDOWN_XSIZE * ((int) Math.log10(timeRem) + 1) : COUNTDOWN_XSIZE;
            drawTextOnMessageBox(
                    messageMap.get(MessageType.AUTOPILOT),
                    timeRem != 0 ? String.valueOf(timeRem) : "0",
                    COUNTDOWN_XCEN, COUNTDOWN_YCEN, xSize, COUNTDOWN_YSIZE);
        }
    }

    void updateShield(int timeRem) {
        if (timeRem != currentShieldCountdownDisplayed) {
            float xSize;
            erasePreviousShieldDigits();
            // Redraw correct digit.
            currentShieldCountdownDisplayed = timeRem;
            xSize = timeRem != 0 ? COUNTDOWN_XSIZE * ((int) Math.log10(timeRem) + 1) : COUNTDOWN_XSIZE;
            drawTextOnMessageBox(
                    messageMap.get(MessageType.SHIELD),
                    timeRem != 0 ? String.valueOf(timeRem) : "0",
                    COUNTDOWN_XCEN, COUNTDOWN_YCEN, xSize, COUNTDOWN_YSIZE);
        }
    }

    void updateSuddenDeath(int timeRem) {
        if (timeRem != currentSuddenDeathCountdownDisplayed) {
            float xSize;
            erasePreviousSuddenDeathDigits();

            currentSuddenDeathCountdownDisplayed = timeRem;
            xSize = timeRem != 0 ? COUNTDOWN_XSIZE * ((int) Math.log10(timeRem) +1) : COUNTDOWN_XSIZE;
            drawTextOnMessageBox(
                    messageMap.get(MessageType.SUDDEN_DEATH),
                    timeRem != 0 ? String.valueOf(timeRem) : "0",
                    COUNTDOWN_XCEN, COUNTDOWN_YCEN, xSize, COUNTDOWN_YSIZE);
        }
    }

    void updateGhostBlocker(int timeRem) {
        if (timeRem != currentGhostBlockersCountdownDisplayed) {
            float xSize;
            erasePreviousGhostBlockerDigits();

            currentGhostBlockersCountdownDisplayed = timeRem;
            xSize = timeRem != 0 ? COUNTDOWN_XSIZE * ((int) Math.log10(timeRem) +1) : COUNTDOWN_XSIZE;
            drawTextOnMessageBox(
                    messageMap.get(MessageType.GHOST_BLOCKERS),
                    timeRem != 0 ? String.valueOf(timeRem) : "0",
                    COUNTDOWN_XCEN, COUNTDOWN_YCEN, xSize, COUNTDOWN_YSIZE);
        }
    }

    void updateMisses(int misses) {
        float xSize;
        erasePreviousMissesDigits();
        currentMissesDisplayed = misses;
        xSize = misses != 0 ? MISSES_XSIZE * ((int) Math.log10(misses) +1) : MISSES_XSIZE;
        drawTextOnMessageBox(
                messageMap.get(MessageType.MISSES),
                String.valueOf(misses),
                MISSES_XCEN, MISSES_YCEN, xSize, MISSES_YSIZE
        );
    }

    void erasePreviousMissesDigits() {
        // Blank the area.
        float xSize = currentMissesDisplayed != 0 ?
                MISSES_XSIZE * ((int) Math.log10(currentMissesDisplayed) + 1)
                : MISSES_XSIZE;
        MessageBox box = messageMap.get(MessageType.MISSES);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        autopilotDigitRect.set(
                base * MISSES_XCEN - base * xSize / 2,
                base * MISSES_YCEN - base * MISSES_YSIZE / 2,
                base * MISSES_XCEN + base * xSize / 2,
                base * MISSES_YCEN + base * MISSES_YSIZE / 2);
        canvas.drawRect(autopilotDigitRect, blankTransparentPaint);
    }

    void erasePreviousSuddenDeathDigits() {
        // Blank the area.
        float xSize = currentSuddenDeathCountdownDisplayed != 0 ?
                COUNTDOWN_XSIZE * ((int) Math.log10(currentSuddenDeathCountdownDisplayed) + 1)
                : COUNTDOWN_XSIZE;
        MessageBox box = messageMap.get(MessageType.SUDDEN_DEATH);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        autopilotDigitRect.set(
                base * COUNTDOWN_XCEN - base * xSize / 2,
                base * COUNTDOWN_YCEN - base * COUNTDOWN_YSIZE / 2,
                base * COUNTDOWN_XCEN + base * xSize / 2,
                base * COUNTDOWN_YCEN + base * COUNTDOWN_YSIZE / 2);
        canvas.drawRect(autopilotDigitRect, blankTransparentPaint);
    }

    void erasePreviousGhostBlockerDigits() {
        // Blank the area.
        float xSize = currentGhostBlockersCountdownDisplayed != 0 ?
                COUNTDOWN_XSIZE * ((int) Math.log10(currentGhostBlockersCountdownDisplayed) + 1)
                : COUNTDOWN_XSIZE;
        MessageBox box = messageMap.get(MessageType.GHOST_BLOCKERS);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        ghostBlockerDigitRect.set(
                base * COUNTDOWN_XCEN - base * xSize / 2,
                base * COUNTDOWN_YCEN - base * COUNTDOWN_YSIZE / 2,
                base * COUNTDOWN_XCEN + base * xSize / 2,
                base * COUNTDOWN_YCEN + base * COUNTDOWN_YSIZE / 2);
        canvas.drawRect(ghostBlockerDigitRect, blankTransparentPaint);
    }


    void erasePreviousAutopilotDigits() {
        // Blank the area.
        float xSize = currentAutopilotCountdownDisplayed != 0 ?
                COUNTDOWN_XSIZE * ((int) Math.log10(currentAutopilotCountdownDisplayed) + 1)
                : COUNTDOWN_XSIZE;
        MessageBox box = messageMap.get(MessageType.AUTOPILOT);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        autopilotDigitRect.set(
                base * COUNTDOWN_XCEN - base * xSize / 2,
                base * COUNTDOWN_YCEN - base * COUNTDOWN_YSIZE / 2,
                base * COUNTDOWN_XCEN + base * xSize / 2,
                base * COUNTDOWN_YCEN + base * COUNTDOWN_YSIZE / 2);
        canvas.drawRect(autopilotDigitRect, blankTransparentPaint);
    }

    void erasePreviousShieldDigits() {
        // Blank the area.
        float xSize = currentShieldCountdownDisplayed != 0 ?
                COUNTDOWN_XSIZE * ((int) Math.log10(currentShieldCountdownDisplayed) + 1)
                : COUNTDOWN_XSIZE;
        MessageBox box = messageMap.get(MessageType.SHIELD);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        autopilotDigitRect.set(
                base * COUNTDOWN_XCEN - base * xSize / 2,
                base * COUNTDOWN_YCEN - base * COUNTDOWN_YSIZE / 2,
                base * COUNTDOWN_XCEN + base * xSize / 2,
                base * COUNTDOWN_YCEN + base * COUNTDOWN_YSIZE / 2);
        canvas.drawRect(autopilotDigitRect, blankTransparentPaint);
    }

    void erasePreviousLevelDigits() {
        float xSize = currentLevelDisplayed != 0 ?
                LEVEL_XSIZE * ((int) Math.log10(currentLevelDisplayed) + 1)
                : LEVEL_XSIZE;
        MessageBox box = messageMap.get(MessageType.NEW_LEVEL_BOX);
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        levelDigitRect.set(
                0,
                base * LEVEL_YCEN - base * LEVEL_YSIZE / 2,
                base,
                base * LEVEL_YCEN + base * LEVEL_YSIZE / 2
        );
        canvas.drawRect(levelDigitRect, blankTransparentPaint);
    }

    void erasePreviousTimeBonus(MessageBox box) {
        canvas = new Canvas(box.messageBitmap);
        float base = box.xSize;
        timeBonusRect.set(
                0,
                base * TIME_BONUS_YCEN - base * TIME_BONUS_YSIZE,
                base,
                base * TIME_BONUS_YCEN + base * TIME_BONUS_YSIZE);
        canvas.drawRect(timeBonusRect, blankTransparentPaint);
    }

    void drawNewTimeBonus() {
        MessageBox box = messageMap.get(MessageType.TIME_BONUS_BOX);
        if (isFirstLevelOrResume) {
            //messageMap.get(MessageType.GET_READY).on = true;
            return;
        } else {
                String scoreValueString = "+ " + String.valueOf(timeBonus) + " pts";
                float w = TIME_BONUS_XSIZE/* * scoreValueString.length()*/;
                erasePreviousTimeBonus(box);
                drawTextOnMessageBox (
                        box,
                        scoreValueString,
                        TIME_BONUS_XCENTER,
                        TIME_BONUS_YCEN,
                        w,
                        TIME_BONUS_YSIZE);
        }
    }

    void drawNewLevelDigits(int newLevel) {
        float xSize = newLevel != 0 ? LEVEL_XSIZE * ((int) Math.log10(newLevel) + 1) : LEVEL_XSIZE;
        drawTextOnMessageBox(
                messageMap.get(MessageType.NEW_LEVEL_BOX),
                newLevel != 0 ? String.valueOf(newLevel) : "0",
                LEVEL_XCEN, LEVEL_YCEN, xSize, LEVEL_YSIZE);

    }

    void killAllMessages() {
        for (MessageType type : MessageType.values()) {
            messageMap.get(type).on = false;
        }
    }
    @Override
    public void updateConstants(int level) {
        erasePreviousLevelDigits();
        erasePreviousTimeBonus(messageMap.get(MessageType.TIME_BONUS_BOX));
        drawNewLevelDigits(level + 1);
        drawNewTimeBonus();
    }

}
