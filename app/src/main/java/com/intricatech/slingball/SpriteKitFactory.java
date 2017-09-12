package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Created by Bolgbolg on 28/11/2015.
 *
 * Creates a series of SpriteKit objects, each corresponding to one of the distinct
 * types of target. Also is responsible for updating the SpriteKit objects in the event of
 * a change in screen size.
 *
 * Note - as Canvas.drawArc() draws the centre-line of an arc of arbitrary thickness at the EDGE
 * of the RectF specified as an argument, all RectFs for arc-drawing must be DECREASED IN SIZE by
 * twice the thickness of the arc. This value is cached for the targetThickness in playAreaInfo
 * as drawArcOffset.
 */
public class SpriteKitFactory
            implements DifficultyLevelObserver{

    private static final String TAG = "SpriteKitFactory";

    int numberOfPossibleOrbits;
    DifficultyLevelDirector difficultyLevelDirector;

    private static final Paint.Cap STROKE_CAP_TYPE = Paint.Cap.ROUND;

    Paint blockerStripePaint;
    Paint dodgerLightOnPaint;
    Paint dodgerLightOffPaint;
    Paint killerLightOnPaint;
    Paint killerLightOffPaint;
    Paint flickerBlankPaint;
    Paint flickerBoltPaint;
    Paint flickerHazePaint;
    Paint rewarderBlankPaint;
    Paint rewarderBodyPaint;
    Paint haloPaint;
    int flickerBlankColor;
    int rewarderColor;
    int haloColor;
    static final float HALO_REL_THICKNESS = 0.4f;
    static final float HALO_REL_DIST_FROM_TARGET = 1.1f;

    Resources resources;
    BitmapShader ballBitmapShader;
    BlurMaskFilter haloBlur;

    Bitmap tempDrawingAreaReport;

    private int totalMemoryUsed;

    /**
     * Constructor :
     */
    public SpriteKitFactory(DifficultyLevelDirector difficultyLevelDirector, Resources resources) {
        numberOfPossibleOrbits = IntRepConsts.MAX_NUMBER_OF_ORBITS;
        this.difficultyLevelDirector = difficultyLevelDirector;
        registerWithDifficultyLevelDirector();    // Register with the DifficultyLevelDirector at construction to ensure initialization of sizes.

        rewarderColor = resources.getColor(R.color.target_silver);

        blockerStripePaint = new Paint();
        blockerStripePaint.setStyle(Paint.Style.STROKE);
        blockerStripePaint.setColor(Color.DKGRAY);
        blockerStripePaint.setAntiAlias(IntRepConsts.ANTI_ALIASED);
        blockerStripePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        dodgerLightOnPaint = new Paint();
        dodgerLightOnPaint.setStyle(Paint.Style.STROKE);
        dodgerLightOnPaint.setARGB(255, 58, 102, 232);
        dodgerLightOnPaint.setStrokeCap(STROKE_CAP_TYPE);
        dodgerLightOnPaint.setAntiAlias(true);
        dodgerLightOnPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.INNER));

        dodgerLightOffPaint = new Paint();
        dodgerLightOffPaint.setStyle(Paint.Style.STROKE);
        dodgerLightOffPaint.setARGB(255, 16, 33, 82);
        dodgerLightOffPaint.setStrokeCap(STROKE_CAP_TYPE);
        dodgerLightOffPaint.setAntiAlias(true);
        dodgerLightOffPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.INNER));

        killerLightOnPaint = new Paint();
        killerLightOnPaint.setStyle(Paint.Style.STROKE);
        killerLightOnPaint.setARGB(255, 255, 0, 0);
        killerLightOnPaint.setStrokeCap(STROKE_CAP_TYPE);
        killerLightOnPaint.setAntiAlias(true);
        killerLightOnPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.INNER));

        killerLightOffPaint = new Paint();
        killerLightOffPaint.setStyle(Paint.Style.STROKE);
        killerLightOffPaint.setARGB(255, 100, 0, 0);
        killerLightOffPaint.setStrokeCap(STROKE_CAP_TYPE);
        killerLightOffPaint.setAntiAlias(true);
        killerLightOffPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.INNER));

        flickerBlankColor = resources.getColor(R.color.circle_background);
        flickerBlankPaint = new Paint();
        flickerBlankPaint.setStyle(Paint.Style.STROKE);
        flickerBlankPaint.setColor(flickerBlankColor);
        flickerBlankPaint.setAntiAlias(IntRepConsts.ANTI_ALIASED);
        // Temp experiment :
        flickerBlankPaint.setColor(Color.TRANSPARENT);
        flickerBlankPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        flickerBoltPaint = new Paint();
        flickerBoltPaint.setStyle(Paint.Style.STROKE);
        flickerBoltPaint.setARGB(255, 255, 255, 255);
        flickerBoltPaint.setAntiAlias(IntRepConsts.ANTI_ALIASED);

        flickerHazePaint = new Paint();
        flickerHazePaint.setStyle(Paint.Style.STROKE);
        flickerHazePaint.setAntiAlias(IntRepConsts.ANTI_ALIASED);
        flickerHazePaint.setARGB(100, 237, 168, 57);

        rewarderBlankPaint = new Paint();
        rewarderBlankPaint.setStyle(Paint.Style.STROKE);
        rewarderBlankPaint.setAntiAlias(true);
        rewarderBlankPaint.setColor(Color.TRANSPARENT);
        rewarderBlankPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        rewarderBodyPaint = new Paint();
        rewarderBodyPaint.setStyle(Paint.Style.STROKE);
        rewarderBodyPaint.setAntiAlias(true);
        rewarderBodyPaint.setColor(rewarderColor);
        rewarderBodyPaint.setAlpha(40);

        haloPaint = new Paint();

        BlurMaskFilter haloBlur;
        haloColor = resources.getColor(R.color.tadpolehalo);
        haloPaint.setColor(haloColor);
        haloPaint.setAlpha(200);
        haloPaint.setAntiAlias(true);
        haloPaint.setStyle(Paint.Style.STROKE);
        haloPaint.setStrokeCap(STROKE_CAP_TYPE);

        this.resources = resources;

    }

    public SpriteKit getSpriteKit(
            PlayAreaInfo playAreaInfo,
            SpriteType spriteType,
            TargetType targetType) {

        SpriteKit spriteKit = new SpriteKit(playAreaInfo, spriteType, targetType);

        dodgerLightOnPaint.setMaskFilter(new BlurMaskFilter(playAreaInfo.scaledTargetThickness / 4, BlurMaskFilter.Blur.INNER));

        float angSize, halfAlpha;
        float playWidth = playAreaInfo.outermostTargetRect.right - playAreaInfo.outermostTargetRect.left;

        float thickness = playAreaInfo.scaledTargetThickness;
        float incrementPerOrbit = playAreaInfo.scaledTargetThickness + playAreaInfo.scaledGapBetweenOrbits;
        float halfThickness = thickness / 2;
        float xCenter = playWidth / 2;
        float drawArcOffset = playAreaInfo.drawArcOffset;

        Canvas c;
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(STROKE_CAP_TYPE);
        p.setStrokeWidth(thickness);
        if (spriteType == SpriteType.BLOCKER){
            p.setStrokeWidth(thickness * 0.8f);
        }
        p.setARGB(
                255,
                targetType.getTargetColor().getRedComponent(),
                targetType.getTargetColor().getGreenComponent(),
                targetType.getTargetColor().getBlueComponent());
        if (spriteType == SpriteType.DECOY_TRANSPARENT) {
            p.setAlpha(127);
        } else p.setAlpha(255);

        p.setMaskFilter(new EmbossMaskFilter(new float[]{1, 1, 1.2f}, 0.8f, 10, 8));

        for (TargetSize targetSize : TargetSize.values()) {

            angSize = targetSize.getAngularSize();
            halfAlpha = angSize / 2;

            // For haloed targets.
            float haloThickness = playAreaInfo.scaledTargetThickness * HALO_REL_THICKNESS;

            RectF drawRect = new RectF();
            Bitmap bmap;
            for (int i = 0; i < numberOfPossibleOrbits; i++) {
                float radius = (playWidth / 2) - (incrementPerOrbit * i);
                float halfWidth = radius * (float) (Math.sin(halfAlpha)) + halfThickness;
                float height = radius + halfThickness - ((radius - halfThickness) * (float) (Math.cos(halfAlpha)));
                if (spriteType != SpriteType.TADPOLE_HALOED
                        && spriteType != SpriteType.KILLER_ON) { // TADPOLE_HALOED has a larger bitmap size to accom halo.

                    bmap = Bitmap.createBitmap(
                            (int) halfWidth * 2,
                            (int) height,
                            Bitmap.Config.ARGB_8888
                    );
                    c = new Canvas(bmap);

                    drawRect.set(
                            halfWidth - (radius - drawArcOffset),
                            0 + drawArcOffset,
                            halfWidth + (radius - drawArcOffset),
                            0 + (2 * radius) - drawArcOffset
                    );
                } else {
                    radius = (playWidth / 2) - (incrementPerOrbit * i);
                    halfWidth = haloThickness + radius * (float) (Math.sin(halfAlpha)) + halfThickness;
                    height = (2 * haloThickness) + radius + halfThickness - ((radius - halfThickness) * (float) (Math.cos(halfAlpha)));
                    drawRect = new RectF();
                    bmap = Bitmap.createBitmap(
                            (int) halfWidth * 2,
                            (int) height,
                            Bitmap.Config.ARGB_8888
                    );
                    drawRect.set(
                            halfWidth - (radius - drawArcOffset),
                            0 + haloThickness + drawArcOffset,
                            halfWidth + (radius - drawArcOffset),
                            0 + (2 * radius) + (2 * haloThickness) - drawArcOffset
                    );
                    c = new Canvas(bmap);
                }

                c.drawArc(drawRect,
                        (float) (Math.toDegrees((-Math.PI / 2) - angSize / 2)),
                        (float) (Math.toDegrees(angSize)),
                        false,
                        p);

                // Draw halo for haloed targets.
                if (spriteType == SpriteType.TADPOLE_HALOED) {
                    haloBlur = new BlurMaskFilter(haloThickness, BlurMaskFilter.Blur.OUTER);
                    haloPaint.setMaskFilter(haloBlur);
                    haloPaint.setColor(haloColor);
                    haloPaint.setStrokeWidth(thickness * HALO_REL_DIST_FROM_TARGET);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - angSize / 2)),
                            (float) (Math.toDegrees(angSize)),
                            false,
                            haloPaint);
                }
                if (spriteType == SpriteType.KILLER_ON) {
                    haloBlur = new BlurMaskFilter(haloThickness, BlurMaskFilter.Blur.OUTER);
                    haloPaint.setMaskFilter(haloBlur);
                    haloPaint.setARGB(255, 255, 0, 0);
                    haloPaint.setStrokeWidth(thickness * HALO_REL_DIST_FROM_TARGET);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - angSize / 2)),
                            (float) (Math.toDegrees(angSize)),
                            false,
                            haloPaint);
                }

                // If spriteType == DODGER_ON, draw the light on.
                if (spriteType == SpriteType.DODGER_ON) {
                    float lightAngSize = angSize * 0.9f;
                    float lightThickness = playAreaInfo.scaledTargetThickness * 0.7f;
                    dodgerLightOnPaint.setStrokeWidth(lightThickness);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - lightAngSize / 2)),
                            (float) (Math.toDegrees(lightAngSize)),
                            false,
                            dodgerLightOnPaint);
                }
                // If spriteType == DODGER_OFF, draw the light off.
                if (spriteType == SpriteType.DODGER_OFF) {
                    float lightAngSize = angSize * 0.9f;
                    float lightThickness = playAreaInfo.scaledTargetThickness * 0.7f;
                    dodgerLightOffPaint.setStrokeWidth(lightThickness);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - lightAngSize / 2)),
                            (float) (Math.toDegrees(lightAngSize)),
                            false,
                            dodgerLightOffPaint);
                }

                // If spriteType == KILLER_ON, draw the light on.
                if (spriteType == SpriteType.KILLER_ON) {
                    float lightAngSize = angSize * 0.9f;
                    float lightThickness = playAreaInfo.scaledTargetThickness * 0.7f;
                    killerLightOnPaint.setStrokeWidth(lightThickness);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - lightAngSize / 2)),
                            (float) (Math.toDegrees(lightAngSize)),
                            false,
                            killerLightOnPaint);
                }
                // If spriteType == KILLER_OFF, draw the light off.
                if (spriteType == SpriteType.KILLER_OFF) {
                    float lightAngSize = angSize * 0.9f;
                    float lightThickness = playAreaInfo.scaledTargetThickness * 0.7f;
                    killerLightOffPaint.setStrokeWidth(lightThickness);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - lightAngSize / 2)),
                            (float) (Math.toDegrees(lightAngSize)),
                            false,
                            killerLightOffPaint);
                }
                // If targetType == REWARDER, blank out the central arc.
                if (targetType == TargetType.REWARDER) {
                    rewarderBlankPaint.setStrokeWidth(thickness * 1.1f);
                    float blankArcSize = angSize * IntRepConsts.FLICKER_BLANK_ARC_SIZE;
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - blankArcSize / 2)),
                            (float) (Math.toDegrees(blankArcSize)),
                            false,
                            rewarderBlankPaint);
                    rewarderBodyPaint.setStrokeWidth(thickness);
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - blankArcSize / 2)),
                            (float) (Math.toDegrees(blankArcSize)),
                            false,
                            rewarderBodyPaint);
                }


                // If targetType == FLICKER, blank out the central arc.
                if (targetType == TargetType.FLICKER) {
                    flickerBlankPaint.setStrokeWidth(thickness * 1.1f);
                    flickerHazePaint.setStrokeWidth(thickness * 0.5f);
                    float blankArcSize = angSize * IntRepConsts.FLICKER_BLANK_ARC_SIZE;
                    c.drawArc(drawRect,
                            (float) (Math.toDegrees((-Math.PI / 2) - blankArcSize / 2)),
                            (float) (Math.toDegrees(blankArcSize)),
                            false,
                            flickerBlankPaint);
                    // For each FLICKER_ON_N spriteType, fill an array of points on the arc.
                    if (spriteType != SpriteType.FLICKER_OFF) {
                        int numberOfPoints = IntRepConsts.FLICKER_NUMBER_OF_SPARK_POINTS;
                        float sparkRadius = (drawRect.right - drawRect.left) / 2;
                        float angle, ratio;
                        float[] xCoors = new float[numberOfPoints + 1];
                        float[] yCoors = new float[numberOfPoints + 1];
                        float radiusDeviation, lateralDeviation;
                        Path path = new Path();
                        for (int j = 0; j <= numberOfPoints; j++) {
                            ratio = j / (float) numberOfPoints;
                            angle = ((float) (-Math.PI / 2)) - (blankArcSize / 2) + (blankArcSize * ratio);
                            radiusDeviation = -thickness / 2 + (float) (Math.random() * thickness);
                            lateralDeviation = (-0.25f + (float) (Math.random() * 0.5f))
                                    * blankArcSize / numberOfPoints;
                            if (j == 0 || j == numberOfPoints) {
                                xCoors[j] = (float) (halfWidth + Math.cos(angle) * (sparkRadius));
                                yCoors[j] = (float) (radius + Math.sin(angle) * (sparkRadius));
                            } else {
                                xCoors[j] = (float) (halfWidth + Math.cos(angle + lateralDeviation)
                                        * (sparkRadius + radiusDeviation));
                                yCoors[j] = (float) (radius + Math.sin(angle + lateralDeviation)
                                        * (sparkRadius + radiusDeviation));
                            }

                            if (j == 0) {
                                path.moveTo(xCoors[0], yCoors[0]);
                            } else {
                                path.lineTo(xCoors[j], yCoors[j]);
                            }
                        }
                        c.drawPath(path, flickerBoltPaint);
                        c.drawPath(path, flickerHazePaint);
                    }
                }

                // If targetType == DECOYS, draw the stripes on the target.
                if (targetType == TargetType.DECOYS) {
                    // Draw stripes.
                    float stripeThickness = halfWidth * 0.2f;
                    blockerStripePaint.setColor(Color.DKGRAY);
                    blockerStripePaint.setStrokeWidth(stripeThickness);
                    blockerStripePaint.setAlpha(127);
                    c.drawLine(
                           halfWidth * 0.2f,
                            height * 1.5f,
                            halfWidth * 0.35f,
                            -height * 0.5f,
                            blockerStripePaint
                    );
                    c.drawLine(
                            halfWidth * 0.5f,
                            height * 1.5f,
                            halfWidth * 0.65f,
                            - height * 0.5f,
                            blockerStripePaint
                    );
                }

                // If targetType == BLOCKER, draw the stripes on the target.
                if (targetType == TargetType.BLOCKER) {
                    // Draw stripes.
                    float stripeThickness = halfWidth * 0.2f;
                    blockerStripePaint.setStrokeWidth(stripeThickness * 1.3f);
                    blockerStripePaint.setARGB(255, 207, 65, 0);
                    blockerStripePaint.setARGB(255, 0, 0, 0);
                    blockerStripePaint.setAlpha(255);
                    c.drawLine(
                           halfWidth * 0.2f,
                            height * 1.5f,
                            halfWidth * 0.5f,
                            -height * 0.5f,
                            blockerStripePaint
                    );
                    c.drawLine(
                            halfWidth * 0.6f,
                            height * 1.5f,
                            halfWidth * 0.9f,
                            - height * 0.5f,
                            blockerStripePaint
                    );
                    c.drawLine(
                           halfWidth * 1.8f,
                            height * 1.5f,
                            halfWidth * 1.5f,
                            -height * 0.5f,
                            blockerStripePaint
                    );
                    c.drawLine(
                            halfWidth * 1.4f,
                            height * 1.5f,
                            halfWidth * 1.1f,
                            - height * 0.5f,
                            blockerStripePaint
                    );
                }

                // Calculate the coordinates of the top-left corner of each Sprite in the SpriteKit
                float xCoor = xCenter - halfWidth;
                float yCoor;
                if (spriteType != SpriteType.TADPOLE_HALOED && spriteType != SpriteType.KILLER_ON) {
                    yCoor = i * incrementPerOrbit;
                } else {
                    yCoor = i * incrementPerOrbit - haloThickness;
                }

                // For debugging, fill the spriteBitmap with an almost transparent white.
                //c.drawARGB(50, 255, 255, 255);

                // Create the Sprite and add it to the SpriteKit.
                SpriteKit.Sprite s = spriteKit.createSprite(xCoor, yCoor, bmap);
                /*int bmapMemoryUsed = bmap.getWidth() * bmap.getHeight() * 4;
                totalMemoryUsed += bmapMemoryUsed;
                Log.d(TAG, "totalMemoryUsed == " + totalMemoryUsed);*/
                bmap = null;
                spriteKit.spriteMap.get(targetSize)[i] = s;
            }
        }
        return spriteKit;
    }

    @Override
    public void registerWithDifficultyLevelDirector() {
        difficultyLevelDirector.register(this);
    }

    @Override
    public void unregisterWithDifficultyLevelDirector() {
        difficultyLevelDirector.unregister(this);
    }

    @Override
    public void updateDifficultyDependents(DifficultyLevel level) {

    }

}
