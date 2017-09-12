package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bolgbolg on 20/02/2016.
 */
public class EffectsFactory {

    static final String TAG = "EffectsFactory";

    Resources resources;
    Bitmap ballSource;
    Bitmap ballHaloedSource;
    Bitmap ballShieldedSource;
    Bitmap ball;
    Bitmap ballHaloed;
    Bitmap explosionBmp, explosionTemp;
    Canvas explosionBmpCan, explosionTempCanvas;

    Map<TargetSize, ExplosionSprite[]> targetExplosionMap;

/*
    static final float[] explosionXSizes = {1.2f, 0.75f, 0.5f, 0.25f};
*/
    static final float[] explosionXSizes = {1.5f, 1.0f, 0.8f, 0.35f};
/*
    static final float[] explosionYSizes = {1.0f, 0.5f, 0.3f, 0.15f};
*/
    static final float[] explosionYSizes = {2.0f, 1.0f, 0.6f, 0.3f};
    static final int numberOfExplosionSizes = explosionXSizes.length;

    Paint ballPaint;
    Paint explosionPaint;
    Paint clearPaint;
    BlurMaskFilter ballBlurFilter;
    EmbossMaskFilter ballEmbossFilter;

    int width, height;
    float ballDiameter;
    float ballShadowDiameter;
    float scaleFactor;
    boolean initialized;                // Flags that onSurfaceChanged has been called at least once.


    // Note - there is no yPos, as the same explosion is used for each orbit. The client should
    // calculate the yPos as along the centerline of the appropriate orbit.
    class ExplosionSprite {
        Bitmap bmap;
        float xPos;
        float yPos;

        ExplosionSprite(Bitmap bmp, float xPos, float yPos) {
            this.bmap = bmp;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }

    EffectsFactory(Resources resources) {
        this.resources = resources;
        ballSource = BitmapFactory.decodeResource(resources, R.drawable.ghostball2);
        ballHaloedSource = BitmapFactory.decodeResource(resources, R.drawable.ghostball_haloed);
        ballShieldedSource = BitmapFactory.decodeResource(resources, R.drawable.ghostball2_shielded);
        initialized = false;

        ballPaint = new Paint();
        ballPaint.setARGB(255, 100, 100, 255);
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setAntiAlias(true);

        explosionPaint = new Paint();
        explosionPaint.setARGB(255, 200, 200, 200);
        explosionPaint.setStyle(Paint.Style.FILL);
        explosionPaint.setAntiAlias(true);

        clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);

        targetExplosionMap = new HashMap<TargetSize, ExplosionSprite[]>();

    }

    void createTargetExplosions(PlayAreaInfo pai) {

        // Declare local reusable local variables.
        float spriteWidth, spriteHeight;
        float halfWidth, halfHeight;
        float scaledWidth, scaledHeight;
        float xPosition, yPosition;
        float halfTargetThickness = pai.scaledTargetThickness * 0.5f;
        float radiusOfOutermostOrbit = ((pai.outermostTargetRect.bottom - pai.outermostTargetRect.top) / 2);
        RectF ovalRect = new RectF();
        Rect source = new Rect();
        RectF dest = new RectF();

        for (TargetSize targetSize : TargetSize.values()) {

            Bitmap[] bmps = new Bitmap[numberOfExplosionSizes];

            // Draw the (1.0f width by 1.0f height) explosion bitmap for this targetSize.
            float arcWidth = targetSize.getAngularSize();
            spriteWidth = (float)(2 * radiusOfOutermostOrbit * Math.sin(arcWidth * 0.5f));
            spriteHeight = halfTargetThickness * 2;

            explosionTemp = Bitmap.createBitmap((int) spriteWidth, (int) spriteHeight, Bitmap.Config.ARGB_8888);
            explosionTempCanvas = new Canvas(explosionTemp);
            explosionTempCanvas.drawColor(Color.WHITE);
            ovalRect.set(0, 0, spriteWidth, spriteHeight);
            explosionTempCanvas.drawOval(ovalRect, clearPaint);

            // Rearrange the quadrants.
            halfWidth = spriteWidth / 2;
            halfHeight = spriteHeight / 2;
            explosionBmp = Bitmap.createBitmap((int) spriteWidth, (int) spriteHeight, Bitmap.Config.ARGB_8888);
            explosionBmpCan = new Canvas(explosionBmp);
            // 1st quadrant.
            source.set(0, 0, (int) halfWidth, (int) halfHeight);
            dest.set(spriteWidth / 2, spriteHeight / 2, spriteWidth, spriteHeight);
            explosionBmpCan.drawBitmap(explosionTemp, source, dest, null);
            // 2nd quadrant.
            source.set((int)(halfWidth), 0, (int)spriteWidth, (int)halfHeight);
            dest.set(0, halfHeight, halfWidth, spriteHeight);
            explosionBmpCan.drawBitmap(explosionTemp, source, dest, null);
            // 3rd quadrant.
            source.set(0, (int)halfHeight, (int)halfWidth, (int)spriteHeight);
            dest.set(halfWidth, 0, spriteWidth, halfHeight);
            explosionBmpCan.drawBitmap(explosionTemp, source, dest, null);
            // 4th quadrant.
            source.set((int)halfWidth, (int)halfHeight, (int)spriteWidth, (int)spriteHeight);
            dest.set(0, 0, halfWidth, halfHeight);
            explosionBmpCan.drawBitmap(explosionTemp, source, dest, null);

            // Scale it to each size and add it to the map.
            ExplosionSprite[] explosionSprites = new ExplosionSprite[numberOfExplosionSizes];
            for (int i = 0; i < explosionXSizes.length; i++) {

                scaledWidth = spriteWidth * explosionXSizes[i];
                scaledHeight = spriteHeight * explosionYSizes[i];
                bmps[i] = Bitmap.createBitmap((int) scaledWidth, (int) scaledHeight, Bitmap.Config.ARGB_8888);
                explosionBmpCan = new Canvas(bmps[i]);
                source.set(0, 0, (int)(spriteWidth), (int) (spriteHeight));
                dest.set(0, 0, scaledWidth, scaledHeight);
                explosionBmpCan.drawBitmap(explosionBmp, source, dest, null);
                xPosition = radiusOfOutermostOrbit - scaledWidth / 2;
                yPosition = spriteHeight * ((1 - explosionYSizes[i]) / 2);

                explosionSprites[i] = new ExplosionSprite(bmps[i], xPosition, yPosition);
            }

            targetExplosionMap.put(targetSize, explosionSprites);
        }
    }

    ExplosionSprite[] getExplosionSprites(TargetSize size) {
        return targetExplosionMap.get(size);
    }

    Bitmap getBallImage() {
        if (!initialized) {
            return null;
        } else {
            ball = Bitmap.createScaledBitmap(ballSource, (int) ballDiameter, (int) ballDiameter, false);
            return ball;
        }
    }

    Bitmap getBallShieldedImage() {
        if (!initialized) {
            return null;
        } else {
            ball = Bitmap.createScaledBitmap(ballShieldedSource, (int) ballDiameter, (int) ballDiameter, false);
            return ball;
        }
    }

    Bitmap getBallHaloedImage() {
        if (!initialized) {
            return null;
        } else {
            ball = Bitmap.createScaledBitmap(
                    ballHaloedSource,
                    (int) (ballDiameter * IntRepConsts.RATIO_OF_HALOED_IMAGE_TO_PLAIN_IMAGE),
                    (int) (ballDiameter * IntRepConsts.RATIO_OF_HALOED_IMAGE_TO_PLAIN_IMAGE),
                    false);
            return ball;
        }
    }


    void onSurfaceChanged(int width, int height, PlayAreaInfo pai) {
        initialized = true;
        this.width = width;
        this.height = height;
        scaleFactor = pai.ratioOfActualToModel;
        ballDiameter = IntRepConsts.BALL_RADIUS * 2 * scaleFactor;
        ballShadowDiameter = IntRepConsts.BALL_SHADOW_DIAMETER * scaleFactor;

        ballEmbossFilter = new EmbossMaskFilter(new float[]{1,1,1.5f}, 0.115f, 2.0f, ballDiameter / 2);
        ballBlurFilter  = new BlurMaskFilter(ballDiameter / 2, BlurMaskFilter.Blur.INNER);
    }
}
