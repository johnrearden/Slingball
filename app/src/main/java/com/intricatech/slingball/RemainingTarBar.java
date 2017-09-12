package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Created by Bolgbolg on 19/03/2016.
 */
public class RemainingTarBar implements LevelChangeObserver {

    static final String TAG = "RemaingingTarBar";

    LevelChangeDirector levelChangeDirector;
    DifficultyLevelDirector difficultyLevelDirector;
    //OrbitSetupCatalogue orbitSetupCatalogue;
    LevelCatalogue levelCatalogue;

    float radius;
    float thickness;
    private float blurThickness;
    float buttonRadius;
    float startAngle;
    float finishAngle;
    float totalArcSize;
    float arcPerButton;
    float[] buttonXPositions;
    float[] buttonYPositions;
    float xCenterOfCircle;
    float yCenterOfCircle;
    float verticalOffset;
    RectF arcRect;
    Path textPath;
    RectF textArcRect;

    int redrawCountdown;

    int totalNumberOfTargets;
    int remainingTargets;

    Paint liveTargetButtonPaint;
    Paint deadTargetButtonPaint;
    Paint backgroundPaint;
    Paint textPaint;
    int backgroundColor;

    int shouldRedrawBackgroundCounter;

    OrbitSetupCatalogue.OrbitDetail orbitDetail;

    static final int MAX_TARGETS_PER_LEVEL = IntRepConsts.MAX_TARGETS_PER_LEVEL;

    RemainingTarBar(
            LevelChangeDirector levelChangeDirector,
            DifficultyLevelDirector difficultyLevelDirector,
            Resources resources) {
        this.levelChangeDirector = levelChangeDirector;
        this.difficultyLevelDirector = difficultyLevelDirector;
        levelChangeDirector.register(this);
        //orbitSetupCatalogue = OrbitSetupCatalogue.getInstance();
        levelCatalogue = LevelCatalogue.getInstance();

        liveTargetButtonPaint = new Paint();
        liveTargetButtonPaint.setARGB(255, 194, 226, 237);
        liveTargetButtonPaint.setStyle(Paint.Style.FILL);

        deadTargetButtonPaint = new Paint();
        deadTargetButtonPaint.setARGB(255, 80, 80, 80);
        deadTargetButtonPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundColor = resources.getColor(R.color.outer_circle);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint();
        textPaint.setColor(resources.getColor(R.color.readout_label_text_color));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        startAngle = IntRepConsts.REM_TAR_BAR_START_ANGLE;
        finishAngle = IntRepConsts.REM_TAR_BAR_FINISH_ANGLE;
        totalArcSize = Math.abs(finishAngle - startAngle);
        buttonXPositions = new float[levelCatalogue.MAX_TARGETS_PER_ORBIT];
        buttonYPositions = new float[levelCatalogue.MAX_TARGETS_PER_ORBIT];
        arcRect = new RectF();
        textArcRect = new RectF();
        textPath = new Path();
        shouldRedrawBackgroundCounter = 0;
        redrawCountdown = 3;
    }

    void updateRemainingTarBar(int targetsRemaining) {
        remainingTargets = targetsRemaining;
        redrawCountdown = 3;
    }

    void drawRemainingTarBar(Canvas canvas) {

        // If the level has changed, redraw the background to erase the previous buttons. (Buttons are
        // spaced according to their number, which changes with the level.
        if (shouldRedrawBackgroundCounter > 0) {
            drawInnerBackground(canvas);
            shouldRedrawBackgroundCounter--;
        }
        for (int i = 0; i < remainingTargets; i++) {
            canvas.drawCircle(
                    buttonXPositions[i],
                    buttonYPositions[i],
                    buttonRadius,
                    liveTargetButtonPaint
            );
        }
        for (int i = remainingTargets; i < totalNumberOfTargets; i++) {
            canvas.drawCircle(
                    buttonXPositions[i],
                    buttonYPositions[i],
                    buttonRadius,
                    deadTargetButtonPaint
            );
        }
        redrawCountdown--;
    }

    void drawBackground(Canvas canvas) {
        float extraArc = totalArcSize * 0.02f;
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeWidth(thickness *
                IntRepConsts.REM_TAR_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS);
        backgroundPaint.setMaskFilter(new BlurMaskFilter(blurThickness, BlurMaskFilter.Blur.NORMAL));
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - extraArc),
                (float) Math.toDegrees(totalArcSize + extraArc * 2),
                false,
                backgroundPaint);

        drawInnerBackground(canvas);

        // Draw text label.
        canvas.drawTextOnPath(
                "     TARGETS LEFT",
                textPath,
                0,
                thickness / 2,
                textPaint
        );

    }
    private void drawInnerBackground(Canvas canvas){
        float extraArc = totalArcSize * 0.02f;
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStrokeWidth(thickness *
                IntRepConsts.ENERGY_BAT_THICKNESS_OF_BLACK_REL_TO_OWN_THICKNESS);
        backgroundPaint.setMaskFilter(null);
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - extraArc),
                (float) Math.toDegrees(totalArcSize + extraArc * 2),
                false,
                backgroundPaint);
    }

    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {

        thickness = IntRepConsts.REM_TAR_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * playAreaInfo.scaledDiameter;
        textPaint.setTextSize(thickness * 1.2f);
        blurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;
        verticalOffset = IntRepConsts.CURVED_DISPLAYS_OFFSET * playAreaInfo.scaledDiameter;
        buttonRadius = thickness * 0.25f;


        float rectWidth = playAreaInfo.outermostTargetRect.right - playAreaInfo.outermostTargetRect.left;
        float rectHeight = playAreaInfo.outermostTargetRect.bottom - playAreaInfo.outermostTargetRect.top;
        float extraWidth = rectWidth
                    * (IntRepConsts.REM_TAR_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        float extraHeight = rectHeight
                * (IntRepConsts.REM_TAR_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        arcRect.set(playAreaInfo.outermostTargetRect.left - extraWidth / 2,
                playAreaInfo.outermostTargetRect.top - extraHeight / 2 - verticalOffset,
                playAreaInfo.outermostTargetRect.right + extraWidth / 2,
                playAreaInfo.outermostTargetRect.bottom + extraHeight / 2 - verticalOffset);
        radius = (arcRect.right - arcRect.left) / 2;
        xCenterOfCircle = playAreaInfo.xCenterOfCircle;
        yCenterOfCircle = playAreaInfo.yCenterOfCircle;

        float extraWidthForText = rectWidth * IntRepConsts.REM_TAR_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * 2;
        textArcRect.set(
                playAreaInfo.outermostTargetRect.left - extraWidth / 2 - extraWidthForText,
                playAreaInfo.outermostTargetRect.top - extraWidth / 2 - extraWidthForText - verticalOffset,
                playAreaInfo.outermostTargetRect.right + extraWidth / 2 + extraWidthForText,
                playAreaInfo.outermostTargetRect.bottom + extraWidth / 2 + extraWidthForText - verticalOffset
        );
        textPath.addArc(
                textArcRect,
                (float) Math.toDegrees(startAngle),
                (float) Math.toDegrees(totalArcSize)
        );

    }

    void setButtonPositions() {
        // Set the positions of the buttons for this level.
        for (int i = 0; i < totalNumberOfTargets; i++) {
            buttonXPositions[i] = xCenterOfCircle + radius *
                    (float) Math.cos(startAngle + (arcPerButton * 0.5) + (i * arcPerButton));
            buttonYPositions[i] = yCenterOfCircle - verticalOffset + radius *
                    (float) Math.sin(startAngle + (arcPerButton * 0.5) + (i * arcPerButton));
        }
    }

    @Override
    public void updateConstants(int level) {
        /*orbitDetail = orbitSetupCatalogue.getOrbitDetail(level, difficultyLevelDirector.getDiffLev());
        totalNumberOfTargets = orbitDetail.getTotalNumberOfTargets();*/
        LevelConfig levelConfig = levelCatalogue.getLevelConfig(level, difficultyLevelDirector.getDiffLev());
        totalNumberOfTargets = levelConfig.getTotalNumberOfTargets();
        arcPerButton = totalArcSize / /*totalNumberOfTargets*/MAX_TARGETS_PER_LEVEL;

        setButtonPositions();
        shouldRedrawBackgroundCounter = 2;
    }
}
