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
 * Created by Bolgbolg on 14/03/2016.
 */
public class EnergyBar {
    float radius;
    float thickness;
    float startAngle;
    float finishAngle;
    float totalArcSize;
    float verticalOffset;
    private float blurThickness;


    float incrementPerCycle; // blah

    float currentEnergyDisplayed;
    float correctEnergy;

    Paint energyBarPaint;
    Paint energyBarBackgroundPaint;
    Paint textPaint;
    int backgroundColor, blackColor;
    RectF arcRect;
    Path textPath;
    RectF textArcRect;

    int flashCounter;
    boolean flashOn;
    static final int CRITICAL_THRESHOLD = 10;
    static final int WARNING_THRESHOLD = 25;
    static final int LOW_THRESHOLD = 50;

    enum State {
        SAFE(12, 245, 12, false),
        LOW(245, 241, 12, false),
        WARNING(245, 12, 12, false),
        CRITICAL(245, 12, 12, false),
        SHIELDED(44, 58, 133, false);

        int redComp, greenComp, blueComp;
        boolean flashing;

        State(int r, int g, int b, boolean flashing) {
            this.redComp = r;
            this.greenComp = g;
            this.blueComp = b;
            this.flashing = flashing;
        }

    }
    State state;
    State previousState;
    boolean fullRedraw;

    /**
     *  Constructor takes no parameters .... appearance is configured in onSurfaceChanged().
     */
    EnergyBar(Resources resources) {
        flashCounter = 0;
        flashOn = true;

        energyBarPaint = new Paint();
        energyBarPaint.setStyle(Paint.Style.STROKE);
        energyBarPaint.setStrokeCap(Paint.Cap.ROUND);
        energyBarPaint.setAntiAlias(true);

        energyBarBackgroundPaint = new Paint();
        energyBarBackgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundColor = resources.getColor(R.color.outer_circle);
        energyBarBackgroundPaint.setColor(backgroundColor);
        energyBarBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        energyBarBackgroundPaint.setAntiAlias(false);

        textPaint = new Paint();
        textPaint.setColor(resources.getColor(R.color.readout_label_text_color));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        state = State.SAFE;
        previousState = State.SAFE;
        fullRedraw = false;

        incrementPerCycle = IntRepConsts.ENERGY_BAR_INCREMENT_PER_CYCLE;
        startAngle = IntRepConsts.ENERGY_BAR_START_ANGLE;
        finishAngle = IntRepConsts.ENERGY_BAR_FINISH_ANGLE;
        arcRect = new RectF();

        textArcRect = new RectF();
        textPath = new Path();
    }

    /**
     *  Adjusts the energyBar to reflect the current swingball.energy.
     *  @param energy : the energy field from swingball (between 0 and 100)
     */
    void update(double energy, Swingball ball) {
        this.correctEnergy = (float)energy;

        if (currentEnergyDisplayed < correctEnergy) {
            currentEnergyDisplayed += incrementPerCycle;
            if (currentEnergyDisplayed > correctEnergy) {
                currentEnergyDisplayed = correctEnergy;
            } // in case of overshoot.
        } else if (currentEnergyDisplayed > correctEnergy) {
            currentEnergyDisplayed -= incrementPerCycle;
            if (currentEnergyDisplayed < correctEnergy) {
                currentEnergyDisplayed = correctEnergy;
            } // in case of overshoot.
        }

        // Set the state of the energyBar.
        if (ball.shield == Swingball.Shield.ON) {
            state = State.SHIELDED;
        } else {
            if (energy < CRITICAL_THRESHOLD) {
                state = State.CRITICAL;
            } else if (energy < WARNING_THRESHOLD) {
                state = State.WARNING;
            } else if (energy < LOW_THRESHOLD) {
                state = State.LOW;
            } else {
                state = State.SAFE;
            }
        }

        if (state != previousState) {
            fullRedraw = true;
        }
        previousState = state;

        energyBarPaint.setARGB(255, state.redComp, state.greenComp, state.blueComp);

    }

    /**
     * Draws the energyBar arc to the supplied canvas.
     * @param canvas
     */
    void drawToCanvas(Canvas canvas) {
        // Blank the current bar.
        canvas.drawArc(
                arcRect,
                (float) (Math.toDegrees(startAngle)),
                (float) (Math.toDegrees(totalArcSize)),
                false,
                energyBarBackgroundPaint);

        if (true) {
            canvas.drawArc(arcRect,
                    (float) Math.toDegrees(startAngle),
                    (float) Math.toDegrees(totalArcSize * currentEnergyDisplayed / 100),
                    false,
                    energyBarPaint);
        }
    }


    /**
     * Method draws the background for the energy bar on the 2 backbuffers. Called by
     * gameSurfaceView.drawBackgroundOnBuffers().
     *
     * @param canvas The canvas supplied by the invoking method in gameSurfaceView.
     */
    void drawEnergyBarBackground(Canvas canvas) {
        float extraArc = totalArcSize * 0.02f;
        energyBarBackgroundPaint.setStrokeWidth(thickness
                * IntRepConsts.ENERGY_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS);
        energyBarBackgroundPaint.setAntiAlias(true);
        energyBarBackgroundPaint.setMaskFilter(new BlurMaskFilter(
                blurThickness, BlurMaskFilter.Blur.NORMAL
        ));
        energyBarBackgroundPaint.setColor(backgroundColor);
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - extraArc),
                (float) Math.toDegrees(totalArcSize + extraArc * 2),
                false,
                energyBarBackgroundPaint);
        energyBarBackgroundPaint.setMaskFilter(null);
        energyBarBackgroundPaint.setColor(Color.BLACK);
        energyBarBackgroundPaint.setStrokeWidth(thickness *
                IntRepConsts.ENERGY_BAT_THICKNESS_OF_BLACK_REL_TO_OWN_THICKNESS);
        canvas.drawArc(arcRect,
                (float) Math.toDegrees(startAngle - extraArc),
                (float) Math.toDegrees(totalArcSize + extraArc * 2),
                false,
                energyBarBackgroundPaint);
        energyBarBackgroundPaint.setStrokeWidth(thickness * 1.2f);

        // Draw text label.
        canvas.drawTextOnPath(
                "ENERGY",
                textPath,
                0,
                thickness / 2,
                textPaint
        );
    }

    /**
     * called by gameSurfaceView.surfaceChanged() when the surface dimensions are known.
     * @param playAreaInfo the details of the current screen as set by gameSurfaceView.
     */
    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        radius = playAreaInfo.scaledDiameter / 2;
        thickness = IntRepConsts.ENERGY_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * playAreaInfo.scaledDiameter;
        textPaint.setTextSize(thickness * 1.2f);
        verticalOffset = IntRepConsts.CURVED_DISPLAYS_OFFSET * playAreaInfo.scaledDiameter;
        energyBarPaint.setStrokeWidth(thickness);
        energyBarBackgroundPaint.setStrokeWidth(thickness *
            IntRepConsts.ENERGY_BAR_RELATIVE_THICKNESS_OF_BLANK);
        totalArcSize = Math.abs(finishAngle - startAngle);

        float rectWidth = playAreaInfo.outermostTargetRect.right - playAreaInfo.outermostTargetRect.left;
        float rectHeight = playAreaInfo.outermostTargetRect.bottom - playAreaInfo.outermostTargetRect.top;
        float extraWidth = rectWidth
                * (IntRepConsts.ENERGY_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        float extraHeight = rectHeight
                * (IntRepConsts.ENERGY_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT - 1);
        arcRect.set(playAreaInfo.outermostTargetRect.left - extraWidth / 2,
                playAreaInfo.outermostTargetRect.top - extraHeight / 2 - verticalOffset,
                playAreaInfo.outermostTargetRect.right + extraWidth / 2,
                playAreaInfo.outermostTargetRect.bottom + extraHeight / 2 - verticalOffset);
        blurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;

        float extraWidthForText = rectWidth * IntRepConsts.ENERGY_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * 2;
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
}
