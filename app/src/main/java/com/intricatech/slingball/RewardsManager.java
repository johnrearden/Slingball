package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by Bolgbolg on 11/05/2016.
 */
public class RewardsManager implements LevelChangeObserver{

    static String TAG;

    static final int NUM_OF_TYPES = RewardType.values().length;
    static final float TAG_ANG_SIZE = IntRepConsts.REWARD_TAG_ANGULAR_SIZE;
    static final float TAG_ANG_POSITION = 0;
    static final String TAG_OFF_TEXT = "REWARD";

    PhysicsAndBGRenderer physicsAndBGRenderer;
    private DifficultyLevelDirector difficultyLevelDirector;
    Resources resources;
    PlayAreaInfo playAreaInfo;
    Swingball ball;
    TargetManager targetManager;
    CountdownTimer timer;
    SoundManager soundManager;
    private LevelCatalogue levelCatalogue;
    private LevelConfig levelConfig;

    private RectF rewardActivatorBounds;
    int level;
    int tagBackgroundColor, tagBacklightColor;
    int rewardsTextOnColor, rewardsTextOffColor;
    int backgroundColor, outerCircleDefaultColor;
    float blurRadius;

    float backlightThickness, tagThickness;
    int currentReward;
    Tag[] tagArray;
    static Tag offTag;
    float[] tagBoundaries;
    int[] tagWeightingBoundaries;

    RectF outerCircleRect;
    Paint outerCirclePaint;

    @Override
    public void updateConstants(int level) {
        this.level = level;
        levelConfig = levelCatalogue.getLevelConfig(level, difficultyLevelDirector.getDiffLev());
    }


    enum RewardType {
        AUTOPILOT("AUTOPILOT"),
        POWER_UP("MORE POWER"),
        EXTRA_TIME("EXTRA-TIME"),
        SHIELD("SHIELD"),
        SUDDEN_DEATH("EXTRA DAMAGE"),
        NO_BLOCKERS("NO BLOCKERS");

        String string;

        RewardType(String string) {
            this.string = string;
        }
    }

    class Tag {
        RewardType type;
        Bitmap onBitmap;
        Bitmap offBitmap;
        float angularPosition;
        float xPos, yPos;
        boolean on;
        boolean shouldRedrawOnBB1;
        boolean shouldRedrawOnBB2;

        String text;

        Tag() {
            shouldRedrawOnBB1 = true;
            shouldRedrawOnBB2 = true;
        }
    }

    RewardsManager(
            PhysicsAndBGRenderer physicsAndBGRenderer,
            Resources resources,
            Swingball ball,
            TargetManager targetManager,
            CountdownTimer timer,
            SoundManager soundManager,
            LevelChangeDirector levelChangeDirector,
            DifficultyLevelDirector difficultyLevelDirector) {

        TAG = getClass().getSimpleName();

        this.physicsAndBGRenderer = physicsAndBGRenderer;
        this.difficultyLevelDirector = difficultyLevelDirector;
        this.resources = resources;
        this.ball = ball;
        this.targetManager = targetManager;
        this.timer = timer;
        this.soundManager = soundManager;
        levelChangeDirector.register(this);
        levelCatalogue = LevelCatalogue.getInstance();
        currentReward = -1;
        rewardsTextOnColor = resources.getColor(R.color.rewards_text_on);
        rewardsTextOffColor = resources.getColor(R.color.rewards_text_off);
        backgroundColor = resources.getColor(R.color.game_background);
        tagBackgroundColor = resources.getColor(R.color.rewards_tag_background);
        tagBacklightColor = resources.getColor(R.color.rewards_tag_backlight);
        outerCircleDefaultColor = resources.getColor(R.color.outer_circle);

        outerCirclePaint = new Paint();
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setColor(rewardsTextOnColor);
        outerCirclePaint.setStyle(Paint.Style.STROKE);

        tagArray = new Tag[NUM_OF_TYPES];
        for (int i = 0; i < NUM_OF_TYPES; i++) {
            tagArray[i] = new Tag();
            tagArray[i].shouldRedrawOnBB1 = true;
            tagArray[i].shouldRedrawOnBB2 = true;
            tagArray[i].angularPosition = TAG_ANG_POSITION;
            tagArray[i].on = false;
            tagArray[i].type = RewardType.values()[i];
            tagArray[i].text = tagArray[i].type.string;
        }
        offTag = new Tag();
        offTag.shouldRedrawOnBB1 = true;
        offTag.shouldRedrawOnBB2 = true;
        offTag.angularPosition = TAG_ANG_POSITION;
        offTag.on = false;
        offTag.type = RewardType.AUTOPILOT;
        offTag.text = "REWARD";


        tagBoundaries = new float[NUM_OF_TYPES * 2];

        for (int i = 0, j = 0; i < NUM_OF_TYPES * 2; i += 2, j++) {
            tagBoundaries[i] = (float)(Math.PI) / 2 + tagArray[j].angularPosition + TAG_ANG_SIZE / 2;
            tagBoundaries[i + 1] = (float)(Math.PI / 2) + tagArray[j].angularPosition - TAG_ANG_SIZE / 2;
        }

        tagWeightingBoundaries = new int[NUM_OF_TYPES + 1];

        rewardActivatorBounds = new RectF();

    }

    boolean update(boolean fingerDown, float[] touchX, float[] touchY) {

        ball.setRewardArcOn(currentReward != -1);
        if (ball.ballCollidedWithRewarder) {
            ball.ballCollidedWithRewarder = false;
            soundManager.playRewardAvailableSound();
            int choice = pickRandomTagFromAvailables();
            if (choice != RewardType.SUDDEN_DEATH.ordinal()) {
                initializeNewReward(choice);
            } else {
                currentReward = choice;
                RewardType rewardType = RewardType.values()[currentReward];
                activateReward(rewardType);
            }
        }

        int pointerCount = 0;
        boolean playerWantsToReverse = false;
        if (fingerDown) {
            for (int i = 0; i < touchX.length; i++) {
                if (touchX[i] != 0.0f) {
                    pointerCount++;
                    if (!rewardActivatorBounds.contains(touchX[i], touchY[i]) && pointerCount > 1) {
                        playerWantsToReverse = true;
                    }
                }
            }
        }

        return playerWantsToReverse;
    }

    boolean checkIfRewardButtonPressed(float[] touchX, float[] touchY) {
        for (int i = 0; i < touchX.length; i++) {
            if (rewardActivatorBounds.contains(touchX[i], touchY[i]) && currentReward != -1) {
                RewardType rewardType = RewardType.values()[currentReward];
                activateReward(rewardType);
                return true;
                //physicsAndBGRenderer.clearTouchArrays(); // to prevent next reward being triggered immediately
            }
        }
        return false;
    }

    void initializeNewReward(int choice) {
        if (choice != -1) {
            for (Tag tag : tagArray) {
                tag.on = false;
            }
            tagArray[choice].on = true;
            currentReward = choice;
            tagArray[choice].shouldRedrawOnBB1 = true;
            tagArray[choice].shouldRedrawOnBB2 = true;
        }
    }

    void onSurfaceChanged(PlayAreaInfo pai, Bitmap backBuffer1) {
        playAreaInfo = pai;
        float halfThickness = playAreaInfo.scaledOuterCircleThickness / 2;
        outerCircleRect = new RectF(
                playAreaInfo.xCenterOfCircle - (playAreaInfo.scaledDiameter / 2) + halfThickness,
                playAreaInfo.yCenterOfCircle - (playAreaInfo.scaledDiameter / 2) + halfThickness,
                playAreaInfo.xCenterOfCircle + (playAreaInfo.scaledDiameter / 2) - halfThickness,
                playAreaInfo.yCenterOfCircle + (playAreaInfo.scaledDiameter / 2) - halfThickness
        );
        outerCirclePaint.setStrokeWidth(playAreaInfo.scaledOuterCircleThickness * 0.6f);
        blurRadius = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;

        // Create the tags
        createTags(pai, backBuffer1);

    }

    /**
     * Method called by GameSurfaceView which redraws any tags that have recently changed state.
     *
     * @param canvas The canvas passed to the method by GameSurfaceView.
     */
    void displayUpdatedTags(Canvas canvas, boolean usingBB1) {
        Tag tag;
        if (currentReward == -1) {
            tag = offTag;
        } else {
            tag = tagArray[currentReward];
        }
        if (tag.shouldRedrawOnBB1 && usingBB1) {
            displayTag(canvas, tag);
            tag.shouldRedrawOnBB1 = false;
        }
        if (tag.shouldRedrawOnBB2 && !usingBB1) {
            displayTag(canvas, tag);
            tag.shouldRedrawOnBB2 = false;
        }
    }

    /**
     * Method draws a single tag by first rotating the canvas to its angular position and
     * then drawing the tag at its x and y positions relative to the center of the circle. The
     * bitmap drawn is either the lit version if the tag is on, or the unlit version if not.
     *
     * @param canvas The canvas passed in by the invoking method.
     * @param tag The tag to draw.
     */
    void displayTag(Canvas canvas, Tag tag) {
        Bitmap bmp = tag.on ? tag.onBitmap : tag.offBitmap;
        float amountToRotate = (float) Math.toDegrees(tag.angularPosition);
        canvas.rotate(amountToRotate, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
        canvas.drawBitmap(bmp, tag.xPos, tag.yPos, null);
        canvas.rotate(-amountToRotate, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);

        // If the tag is on, draw a light arc on the outer circle. If not, draw the original arc.
        /*outerCirclePaint.setColor(
                tag.on ? assignColor(tagArray[currentReward].type) : outerCircleDefaultColor
        );

        outerCirclePaint.setStrokeWidth(tag.on ? playAreaInfo.scaledOuterCircleThickness * 0.8f :
                                                 playAreaInfo.scaledOuterCircleThickness);
        float size = tag.on ? TAG_ANG_SIZE * 0.95f : TAG_ANG_SIZE;
        canvas.drawArc(
                outerCircleRect,
                (float) Math.toDegrees(Math.PI / 2 + (tag.angularPosition - size / 2)),
                (float) Math.toDegrees(size),
                false,
                outerCirclePaint
        );*/
    }

    /**
     * Method called internally by onSurfaceChanged() to create the Tag instances. The Tag bitmaps
     * are all oriented to an angle of -PI / 2, and the canvas used to draw them must be rotated by
     * their angular position (and back!) in order to draw them on the screen.
     *
     * @param pai The PlayAreaInfo object passed into onSurfaceChanged() by GameSurfaceView.
     */
    private void createTags(PlayAreaInfo pai, Bitmap backBuffer1) {
        backlightThickness = pai.scaledDiameter * IntRepConsts.REWARD_TAG_THICKNESS_RATIO;
        tagThickness = backlightThickness * IntRepConsts.REWARD_TAG_INNER_THICKNESS_RATIO;
        float innerBacklightRadius = (pai.scaledDiameter / 2 - pai.scaledOuterCircleThickness)
                * IntRepConsts.REWARD_TAG_GAP_TO_CIRCLE_RATIO;
        float outerBacklightRadius = innerBacklightRadius + backlightThickness;
        float halfAngle = TAG_ANG_SIZE / 2;
        float halfWidth = outerBacklightRadius * (float) Math.sin(halfAngle) + backlightThickness / 2;
        float height = backlightThickness + innerBacklightRadius - (float)(innerBacklightRadius * Math.cos(halfAngle));

        rewardActivatorBounds.set(
                pai.xCenterOfCircle - halfWidth,
                pai.yCenterOfCircle + outerBacklightRadius - height,
                pai.xCenterOfCircle + halfWidth,
                pai.yCenterOfCircle + outerBacklightRadius
        );

        Canvas canvas;
        Paint textPaint = new Paint();
        float textSize = tagThickness;
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        Rect textBounds = new Rect();
        String str = "NO BLOCKERS";
        textPaint.getTextBounds(str, 0, str.length(), textBounds);
        float textHeight = textBounds.bottom - textBounds.top;

        Path textPath = new Path();
        float adjustedRadius = outerBacklightRadius - backlightThickness / 2;
        float adjustedInnerRadius = outerBacklightRadius - tagThickness / 2;
        RectF completeArea = new RectF(
                -adjustedRadius + halfWidth,
                (height - backlightThickness / 2) - adjustedRadius * 2,
                adjustedRadius + halfWidth,
                height - backlightThickness / 2);
        textPath.addArc(
                completeArea,
                (float) Math.toDegrees((Math.PI / 2) + halfAngle),
                (float) Math.toDegrees(-halfAngle * 2));

        Paint tagPaint = new Paint();
        tagPaint.setStyle(Paint.Style.STROKE);
        tagPaint.setStrokeWidth(tagThickness);
        tagPaint.setStrokeCap(Paint.Cap.ROUND);
        tagPaint.setAntiAlias(true);

        Paint highlightPaint = new Paint();
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(tagThickness);
        highlightPaint.setStrokeCap(Paint.Cap.ROUND);
        highlightPaint.setAntiAlias(true);


        for (Tag tag : tagArray) {
            tag.xPos = pai.xCenterOfCircle - halfWidth;
            tag.yPos = pai.yCenterOfCircle + outerBacklightRadius - height;

            // Draw the onBitmap.
            int color = assignColor(tag.type);
            /*tag.onBitmap = Bitmap.createBitmap(
                    (int) (halfWidth * 2 + 0.5f),
                    (int) (height + 0.5f),
                    Bitmap.Config.ARGB_8888);*/
            tag.onBitmap = Bitmap.createBitmap(
                    backBuffer1,
                    (int) (tag.xPos),
                    (int) tag.yPos,
                    (int) ((halfWidth) * 2),
                    (int) height);
            canvas = new Canvas(tag.onBitmap);
            //canvas.drawColor(Color.WHITE);
            highlightPaint.setColor(color);
            /*canvas.drawArc(
                    completeArea,
                    (float) Math.toDegrees(Math.PI / 2 - (halfAngle * 0.9f)),
                    (float) Math.toDegrees((halfAngle * 0.9f) * 2),
                    false,
                    highlightPaint);*/
            highlightPaint.setMaskFilter(new BlurMaskFilter(tagThickness, BlurMaskFilter.Blur.NORMAL));
            canvas.drawArc(
                    completeArea,
                    (float) Math.toDegrees(Math.PI / 2 - (halfAngle * 1.1f)),
                    (float) Math.toDegrees((halfAngle * 1.1f) * 2),
                    false,
                    highlightPaint);
            /*highlightPaint.setMaskFilter(new BlurMaskFilter(tagThickness, BlurMaskFilter.Blur.OUTER));
            canvas.drawArc(
                    completeArea,
                    (float) Math.toDegrees(Math.PI / 2 - (halfAngle * 1.1f)),
                    (float) Math.toDegrees((halfAngle * 1.1f) * 2),
                    false,
                    highlightPaint);*/
            tagPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
            tagPaint.setColor(tagBackgroundColor);
            canvas.drawArc(
                    completeArea,
                    (float) Math.toDegrees(Math.PI / 2 - halfAngle * 0.9f),
                    (float) Math.toDegrees((halfAngle * 0.9f) * 2),
                    false,
                    tagPaint);
            tagPaint.setMaskFilter(null);
            textPaint.setColor(color);
            canvas.drawTextOnPath(
                    tag.text,
                    textPath,
                    0,
                    textHeight / 2,
                    textPaint
            );

            // Draw the offBitmap.
            /*tag.offBitmap = Bitmap.createBitmap(
                    (int) (halfWidth * 2 + 0.5f),
                    (int) (height + 0.5f),
                    Bitmap.Config.ARGB_8888
            );*/
            tag.offBitmap = Bitmap.createBitmap(
                    backBuffer1,
                    (int) tag.xPos,
                    (int) tag.yPos,
                    (int) (halfWidth * 2),
                    (int) height);
            canvas = new Canvas(tag.offBitmap);
            //canvas.drawColor(backgroundColor);
            tagPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
            canvas.drawArc(
                    completeArea,
                    (float) Math.toDegrees(Math.PI / 2 - halfAngle * 0.9f),
                    (float) Math.toDegrees((halfAngle * 0.9f) * 2),
                    false,
                    tagPaint);
            tagPaint.setMaskFilter(null);
            textPaint.setColor(rewardsTextOffColor);
            canvas.drawTextOnPath(
                    TAG_OFF_TEXT,
                    textPath,
                    0,
                    textHeight / 2,
                    textPaint
            );

        }
        offTag.offBitmap = tagArray[0].offBitmap;
        offTag.onBitmap = tagArray[0].offBitmap;
        offTag.xPos = tagArray[0].xPos;
        offTag.yPos = tagArray[0].yPos;
        canvas = null;
    }

    /**
     * Method chooses one of the available Tags (those that are currently off), at random, while
     * respecting the weightings specified in the enum RewardType declaration.
     *
     * @return the index of the rewardType chosen (the ordinal), or -1 if none are available.
     */
    int pickRandomTagFromAvailables() {

        /**
         * To help the player, there are two short circuits here:
         *
         * First : If the time remaining in the level is less than the panic level, automatically
         *          give them a time bonus.
         * Second : If 1 is not the case, if the ball energy is less than the panic amount,
         *          automatically give the an energy bonus.
         */

        if (timer.getDisplayedTime() < CountdownTimer.RED_THRESHOLD) {
            return 2;
        } else if (ball.energy < EnergyBar.WARNING_THRESHOLD) {
            return 1;
        }

        /**
         * Otherwise choose a tag using the normal weighting system.
         */
        RewardType chosenType = levelConfig.chooseWeightedRandomRewardType();
        if (ball.energy > IntRepConsts.ENERGY_THRESHOLD_FOR_NO_POWERUP_REWARD) {
            for (int i = 0; i < 5; i++) {
                chosenType = levelConfig.chooseWeightedRandomRewardType();
                if (chosenType != RewardType.POWER_UP) {
                    break;
                }
            }
        }
        return chosenType.ordinal();
    }

    public void cancelAllRewards() {
        currentReward = -1;
        offTag.shouldRedrawOnBB1 = true;
        offTag.shouldRedrawOnBB2 = true;
    }

    void activateReward(RewardType rt) {
        switch (rt) {
            case AUTOPILOT: {
                tagArray[rt.ordinal()].on = false;
                tagArray[rt.ordinal()].shouldRedrawOnBB1 = true;
                tagArray[rt.ordinal()].shouldRedrawOnBB2 = true;
                ball.turnOnAutopilot();
                soundManager.playAutopilotVoice();
                cancelAllRewards();
                break;
            }
            case POWER_UP: {
                tagArray[rt.ordinal()].on = false;
                tagArray[rt.ordinal()].shouldRedrawOnBB1 = true;
                tagArray[rt.ordinal()].shouldRedrawOnBB2 = true;
                ball.energy += 50;
                if (ball.energy > 100) {
                    ball.energy = 100;
                }
                soundManager.playPowerUpVoice();

                cancelAllRewards();
                break;
            }
            case EXTRA_TIME: {
                tagArray[rt.ordinal()].on = false;

                tagArray[rt.ordinal()].shouldRedrawOnBB1 = true;
                tagArray[rt.ordinal()].shouldRedrawOnBB2 = true;
                if (level >= 19) {
                    soundManager.playTwentyMoreSeconds();
                    timer.increaseTimeRemaining(IntRepConsts.REWARD_EXTRA_TIME_LEVEL_20);
                } else {
                    soundManager.playTenMoreSeconds();
                    timer.increaseTimeRemaining(IntRepConsts.REWARD_EXTRA_TIME);
                }
                cancelAllRewards();
                break;
            }
            case SHIELD: {
                tagArray[rt.ordinal()].on = false;
                tagArray[rt.ordinal()].shouldRedrawOnBB1 = true;
                tagArray[rt.ordinal()].shouldRedrawOnBB2 = true;
                ball.turnOnShield();
                soundManager.playProtectMe();
                if (ball.suddenDeath == Swingball.SuddenDeath.ON) {
                    ball.turnOffSuddenDeath();
                }
                cancelAllRewards();
                break;
            }
            case SUDDEN_DEATH: {
                ball.turnOnSuddenDeath();
                if (ball.shield == Swingball.Shield.ON) {
                    ball.turnOffShield();
                }
                soundManager.playDontTouchTheEdge();
                cancelAllRewards();
                break;
            }
            case NO_BLOCKERS: {
                tagArray[rt.ordinal()].on = false;
                tagArray[rt.ordinal()].shouldRedrawOnBB1 = true;
                tagArray[rt.ordinal()].shouldRedrawOnBB2 = true;
                targetManager.turnOffBlockers();
                soundManager.playBlockersBegone();
                cancelAllRewards();
                break;
            }
            /*case EXTRA_HEART: {
                targetManager
                break;
            }*/
            default: {
                Log.d(getClass().getName(), "activateReward default called");
            }
        }
    }

    int assignColor(RewardType rt) {
        switch (rt) {
            case AUTOPILOT: {
                return resources.getColor(R.color.reward_autopilot);
            }
            case EXTRA_TIME: {
                return resources.getColor(R.color.reward_extratime);
            }
            case POWER_UP: {
                return resources.getColor(R.color.reward_powerup);
            }
            case SHIELD: {
                return resources.getColor(R.color.reward_shield);
            }
            case NO_BLOCKERS:
                return Color.WHITE;
            default : {
                return resources.getColor(R.color.reward_autopilot);
            }
        }
    }

}
