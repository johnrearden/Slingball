package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by Bolgbolg on 10/06/2017.
 */
public class HitRateDisplayer {

    static String TAG;

    private int hitCounter;

    private static final float MAXIMUM_HITRATE = IntRepConsts.HITRATE_HIGHEST_POSSIBLE_HITRATE;

    private float radius;
    private float thickness;
    private float startAngle;
    private float finishAngle;
    private float totalArcSize;
    private float verticalOffset;
    private float blurThickness;
    private int signumSweepAngle;

    private Path textPath;
    private RectF textArcRect;
    private Paint textPaint;

    float currentHitrate;
    float proportionOfBarLit;

    Paint hitrateBarPaint;
    Paint hitrateBackgroundPaint;
    int backgroundColor, blackColor;
    RectF arcRect;

    public HitRateDisplayer(Resources resources) {

        TAG = getClass().getSimpleName();

        hitCounter = 0;

        hitrateBarPaint = new Paint();
        hitrateBarPaint.setStyle(Paint.Style.STROKE);
        hitrateBarPaint.setStrokeCap(Paint.Cap.ROUND);
        hitrateBarPaint.setAntiAlias(true);
        hitrateBarPaint.setColor(Color.CYAN);

        hitrateBackgroundPaint = new Paint();
        hitrateBackgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundColor = resources.getColor(R.color.outer_circle);
        hitrateBackgroundPaint.setColor(backgroundColor);
        hitrateBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        hitrateBackgroundPaint.setAntiAlias(false);

        textPaint = new Paint();
        textPaint.setColor(resources.getColor(R.color.readout_label_text_color));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        startAngle = IntRepConsts.HITRATE_BAR_START_ANGLE;
        finishAngle = IntRepConsts.HITRATE_BAR_FINISH_ANGLE;
        if (finishAngle < startAngle) {
            signumSweepAngle = -1;
        } else {
            signumSweepAngle = 1;
        }
        arcRect = new RectF();
        textArcRect = new RectF();
        textPath = new Path();
    }

    public void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        boolean b = -0.0f < 0.0f;
        Log.d(TAG, "-0.0f < 0.0f == " + b);
        radius = playAreaInfo.scaledDiameter / 2;
        thickness = IntRepConsts.HITRATE_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * playAreaInfo.scaledDiameter;
        textPaint.setTextSize(thickness * 2);
        verticalOffset = IntRepConsts.CURVED_DISPLAYS_OFFSET * playAreaInfo.scaledDiameter;
        hitrateBarPaint.setStrokeWidth(thickness);
        hitrateBackgroundPaint.setStrokeWidth(thickness *
                IntRepConsts.HITRATE_BAR_RELATIVE_THICKNESS_OF_BLANK);
        totalArcSize = Math.abs(finishAngle - startAngle);


        float rectWidth = playAreaInfo.outermostTargetRect.right - playAreaInfo.outermostTargetRect.left;
        float rectHeight = playAreaInfo.outermostTargetRect.bottom - playAreaInfo.outermostTargetRect.top;
        float extraWidth = rectWidth
                * (IntRepConsts.HITRATE_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        float extraHeight = rectHeight
                * (IntRepConsts.HITRATE_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        arcRect.set(playAreaInfo.outermostTargetRect.left - extraWidth / 2,
                playAreaInfo.outermostTargetRect.top - extraHeight / 2 - verticalOffset,
                playAreaInfo.outermostTargetRect.right + extraWidth / 2,
                playAreaInfo.outermostTargetRect.bottom + extraHeight / 2 - verticalOffset);
        float extraWidthForText = rectWidth * IntRepConsts.HITRATE_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * 2;
        textArcRect.set(
                playAreaInfo.outermostTargetRect.left - extraWidth / 2 - extraWidthForText,
                playAreaInfo.outermostTargetRect.top - extraWidth / 2 - extraWidthForText - verticalOffset,
                playAreaInfo.outermostTargetRect.right + extraWidth / 2 + extraWidthForText,
                playAreaInfo.outermostTargetRect.bottom + extraWidth / 2 + extraWidthForText - verticalOffset
        );
        textPath.addArc(
                textArcRect,
                (float) Math.toDegrees(startAngle),
                (float) Math.toDegrees(totalArcSize * signumSweepAngle)
        );

        blurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;
    }

    public void updateHitRate(long timeElapsed) {

        float timeInSeconds = (float) timeElapsed / 1000000000;
        if (timeInSeconds > 0) {
            currentHitrate = (float) hitCounter / timeInSeconds;
        } else {
            currentHitrate = 0;
        }
        proportionOfBarLit = currentHitrate / MAXIMUM_HITRATE;

        if (proportionOfBarLit > 1.0f) {
            proportionOfBarLit = 1.0f;
        } else if (proportionOfBarLit < 0.0f) {
            proportionOfBarLit = 0.0f;
        }
    }

    public void registerHits(int hits) {
        hitCounter += hits;
    }

    public void resetHitrateCounter() {
        hitCounter = 0;
    }

    public void drawHitRate(Canvas canvas) {
        canvas.drawArc(
                arcRect,
                (float) (Math.toDegrees(startAngle)),
                (float) (Math.toDegrees(totalArcSize) * signumSweepAngle),
                false,
                hitrateBackgroundPaint);

        float sweepAngle = (float) Math.toDegrees(totalArcSize * proportionOfBarLit) * signumSweepAngle;
        if (proportionOfBarLit > 0.0f) {

            canvas.drawArc(arcRect,
                    (float) Math.toDegrees(startAngle),
                    sweepAngle,
                    false,
                    hitrateBarPaint);
        }
    }

    public void drawHitRateBackground(Canvas canvas) {
        float extraArc = totalArcSize * 0.02f;
        hitrateBackgroundPaint.setStrokeWidth(thickness
                * IntRepConsts.ENERGY_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS);
        hitrateBackgroundPaint.setAntiAlias(true);
        hitrateBackgroundPaint.setMaskFilter(new BlurMaskFilter(
                blurThickness, BlurMaskFilter.Blur.NORMAL
        ));
        hitrateBackgroundPaint.setColor(backgroundColor);
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - (extraArc * signumSweepAngle)),
                (float) Math.toDegrees(totalArcSize + (extraArc * 2)) * signumSweepAngle,
                false,
                hitrateBackgroundPaint);
        hitrateBackgroundPaint.setMaskFilter(null);
        hitrateBackgroundPaint.setColor(Color.BLACK);
        hitrateBackgroundPaint.setStrokeWidth(thickness *
                IntRepConsts.ENERGY_BAT_THICKNESS_OF_BLACK_REL_TO_OWN_THICKNESS);
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - (extraArc * signumSweepAngle)),
                (float) Math.toDegrees(totalArcSize + (extraArc) * 2) * signumSweepAngle,
                false,
                hitrateBackgroundPaint);
        hitrateBackgroundPaint.setStrokeWidth(thickness * 1.2f);

        // Draw text label.
        canvas.drawTextOnPath(
                "HIT RATE",
                textPath,
                0,
                thickness,
                textPaint
        );
    }

}
