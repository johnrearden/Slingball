package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by Bolgbolg on 14/06/2016.
 *
 * Fireflies is a class representing a collection of Fireflies - small particles of variable
 * luminosity which are confined in a Target and released when the Target is destroyed.
 */
public class Fireflies {

    static final String TAG = "Fireflies";
    static final boolean USE_IMPORTED_PNG = true;
    static final float PI = (float) Math.PI;

    Resources resources;
    PlayAreaInfo playAreaInfo;
    static final int NUMBER_OF_FIREFLIES = IntRepConsts.FIREFLY_POPULATION;
    static final float XCENTER = IntRepConsts.DIAMETER / 2;
    Firefly[] fireflies;
    Behaviour firefliesBehaviour;
    TargetManager targetManager;

    Paint tempPaint;
    Paint flyCorePaint, flyOuterPaint, brightnessModifier;
    Paint importedPNGPaint;
    int flyColor;
    Bitmap flyBitmap, flyBitmapImported;
    float scaledFlyCoreDiameter, scaledFlyOuterDiameter;

    float screenRatio;
    boolean containingTargetDestroyedFlag;
    int looseCountdown;
    final int LOOSE_COUNTDOWN_INITIAL_VALUE = IntRepConsts.FIREFLY_LOOSE_COUNTDOWN_INITIAL_VALUE;
    int fadingCountdown;
    final int FADING_COUNTDOWN_INITIAL_VALUE = IntRepConsts.FIREFLY_FADING_COUNTDOWN_INITIAL_VALUE;
    final float BRIGHTNESS_DECREMENT = 255.0f / FADING_COUNTDOWN_INITIAL_VALUE;

    // Fields associated with the AbstractTarget within which the fireflies are confined.
    float targetLeftEnd, targetRightEnd, targetAngle;
    float targetSize;
    float tangentialDirection;
    float targetOuterRadiusSq, targetInnerRadiusSq;
    float targetOuterRadius, targetInnerRadius;
    float totalTargetThickness;
    static final float CIRCLE_RADIUS_SQ = (float) Math.pow(
            IntRepConsts.DIAMETER / 2 - IntRepConsts.OUTER_CIRCLE_THICKNESS, 2);

    enum Behaviour {
        CONFINED,
        LOOSE,
        FADING,
        OFF
    }

    Fireflies (PlayAreaInfo playAreaInfo, TargetManager tm, Resources res) {
        this.playAreaInfo = playAreaInfo;
        this.targetManager = tm;
        this.resources = res;

        tm.rewarderTargetAllowed = true;
        fireflies = new Firefly[NUMBER_OF_FIREFLIES];
        for (int i = 0; i < NUMBER_OF_FIREFLIES; i++) {
            fireflies[i] = new Firefly();
        }
        firefliesBehaviour = Behaviour.OFF;

        flyColor = resources.getColor(R.color.target_silver);
        tempPaint = new Paint();
        tempPaint.setStyle(Paint.Style.FILL);
        tempPaint.setARGB(255, 255, 255, 255);

        flyCorePaint = new Paint();
        flyCorePaint.setColor(flyColor);
        flyCorePaint.setAntiAlias(true);
        flyCorePaint.setStyle(Paint.Style.FILL);

        flyOuterPaint = new Paint();
        flyOuterPaint.setColor(flyColor);
        flyOuterPaint.setAntiAlias(true);
        flyOuterPaint.setStyle(Paint.Style.FILL);

        importedPNGPaint = new Paint();

        brightnessModifier = new Paint();

        flyBitmapImported = BitmapFactory.decodeResource(resources, R.drawable.firefly);

    }

    void updateTarget(AbstractTarget target) {
        if (target != null) {
            targetSize = target.size.getAngularSize();
            targetAngle = target.alpha;
            targetLeftEnd = ((float) Helpers.resolveAngle(target.alpha - targetSize / 2));
            targetRightEnd = (float)Helpers.resolveAngle(target.alpha + targetSize / 2);
            tangentialDirection = (float)Helpers.resolveAngle(target.alpha + Math.PI / 2);
            totalTargetThickness = IntRepConsts.TARGET_THICKNESS + IntRepConsts.GAP_BETWEEN_ORBITS;
            float outermostRadius = IntRepConsts.DIAMETER / 2 - IntRepConsts.OUTER_CIRCLE_THICKNESS;
            targetOuterRadius = outermostRadius - (target.orbitIndex * totalTargetThickness);
            targetInnerRadius = outermostRadius - (target.orbitIndex + 1) * totalTargetThickness;
            targetOuterRadiusSq = targetOuterRadius * targetOuterRadius;
            targetInnerRadiusSq = targetInnerRadius * targetInnerRadius;
        } else {
            containingTargetDestroyedFlag = true;
        }
    }

    /**
     * Method is called on each cycle by physicsAndBGRenderer, but returns immediately if it has
     * no work to do. If a rewarderTarget exists, firefliesBehaviour is set to CONFINED. The Fireflies
     * object then handles its own lifecycle, setting itself to OFF once it has faded.
     */
    void updateFireflies() {
        switch (firefliesBehaviour) {
            case OFF : {
                if (targetManager.rewarderTargetExists) {
                    firefliesBehaviour = Behaviour.CONFINED;
                    updateTarget(targetManager.orbits[targetManager.rewarderTargetIndex]);
                    lockFliesToTarget();
                    targetManager.rewarderTargetAllowed = false;
                } else {
                    return;
                }
                break;
            }
            case CONFINED: {
                if (!targetManager.rewarderTargetExists) {
                    firefliesBehaviour = Behaviour.LOOSE;
                    looseCountdown = LOOSE_COUNTDOWN_INITIAL_VALUE;
                    break;
                }
                updateTarget(targetManager.orbits[targetManager.rewarderTargetIndex]);
                for (Firefly fly : fireflies) {
                    fly.checkForCollisionWithinTarget();
                    fly.updatePosition();
                }

                break;
            }
            case LOOSE: {
                for (Firefly fly : fireflies) {
                    fly.checkForCollisionWithCircle();
                    fly.updatePosition();

                }
                if (looseCountdown-- <= 0) {
                    firefliesBehaviour = Behaviour.FADING;
                    fadingCountdown = FADING_COUNTDOWN_INITIAL_VALUE;
                }
                break;
            }
            case FADING: {
                for (Firefly fly : fireflies) {
                    fly.checkForCollisionWithCircle();
                    fly.updatePosition();
                    fly.brightness -= BRIGHTNESS_DECREMENT;
                    if (fly.brightness < 0) {
                        fly.brightness = 0;
                        firefliesBehaviour = Behaviour.OFF;
                        targetManager.rewarderTargetAllowed = true;
                    }
                }



                break;

            }
            default: {
                Log.d(TAG, "fireflies.update() switch statement fell through to default");
                firefliesBehaviour = Behaviour.OFF;
            }
        }
    }

    void turnOffAll() {
        targetManager.rewarderTargetExists = false;
        firefliesBehaviour = Behaviour.OFF;
        targetManager.rewarderTargetAllowed = true;
    }

    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        this.playAreaInfo = playAreaInfo;
        screenRatio = playAreaInfo.ratioOfActualToModel;
        scaledFlyCoreDiameter = IntRepConsts.FIREFLY_CORE_DIAMETER * screenRatio;
        scaledFlyOuterDiameter = IntRepConsts.FIREFLY_OUTER_DIAMETER * screenRatio;
        flyBitmap = Bitmap.createBitmap(
                (int) scaledFlyOuterDiameter,
                (int) scaledFlyOuterDiameter,
                Bitmap.Config.ARGB_8888
        );
        Canvas c = new Canvas(flyBitmap);
        c.drawCircle(
                scaledFlyOuterDiameter / 2,
                scaledFlyOuterDiameter / 2,
                scaledFlyCoreDiameter / 2,
                flyCorePaint
        );
        flyOuterPaint.setMaskFilter(new BlurMaskFilter(scaledFlyOuterDiameter / 2, BlurMaskFilter.Blur.OUTER));
        c.drawCircle(
                scaledFlyOuterDiameter / 2,
                scaledFlyOuterDiameter / 2,
                scaledFlyCoreDiameter / 2,
                flyOuterPaint
        );

        flyBitmapImported = Bitmap.createScaledBitmap(
                flyBitmapImported,
                (int) scaledFlyOuterDiameter,
                (int) scaledFlyOuterDiameter,
                false);

    }

    void displayFireflies(Canvas canvas) {
        float x, y, offset;
        float halfDiam = IntRepConsts.DIAMETER / 2;
        for (Firefly fly : fireflies) {
            if (!USE_IMPORTED_PNG) {
                x = playAreaInfo.xCenterOfCircle + (fly.xPos - halfDiam) * screenRatio;
                y = playAreaInfo.yCenterOfCircle + (fly.yPos - halfDiam) * screenRatio;
                offset = scaledFlyOuterDiameter / 2;
                brightnessModifier.setAlpha((int) fly.brightness);
                canvas.drawBitmap(
                        flyBitmap,
                        x - offset,
                        y - offset,
                        brightnessModifier
                );
            } else {
                x = playAreaInfo.xCenterOfCircle + (fly.xPos - halfDiam) * screenRatio;
                y = playAreaInfo.yCenterOfCircle + (fly.yPos - halfDiam) * screenRatio;
                offset = scaledFlyOuterDiameter / 2;
                importedPNGPaint.setAlpha((int)fly.brightness);
                canvas.drawBitmap(
                        flyBitmapImported,
                        x - offset,
                        y - offset,
                        importedPNGPaint);
            }
        }
    }

    void lockFliesToTarget() {
        for (Firefly fly : fireflies) {
            // Select a random position.
            float r = (targetOuterRadius - totalTargetThickness / 2);
            fly.xPos = XCENTER + r * (float) Math.cos(targetAngle);
            fly.yPos = XCENTER + r * (float) Math.sin(targetAngle);
            fly.brightness = 255.0f;

            // Select a random direction.
            fly.direction = (float)(-Math.PI + Math.random() * Math.PI * 2);

            // Set initial velocity.
            fly.velocity = IntRepConsts.FIREFLY_INITIAL_VELOCITY;
        }


    }

    class Firefly {
        float xPos, yPos, angle, alpha, radialDistSq;
        float radiusSq;
        float velocity, direction;
        float velRadial, velTangential;
        float brightness;
        boolean tangentialCollisionAlreadyOccurring;
        boolean radialCollisionAlreadyOccurring;

        Firefly() {
            radiusSq = (IntRepConsts.FIREFLY_CORE_DIAMETER / 2) * (IntRepConsts.FIREFLY_CORE_DIAMETER / 2);
            brightness = 255;
        }

        boolean checkForCollisionWithinTarget() {

            if (firefliesBehaviour == Behaviour.CONFINED) {
            }

            boolean collisionThisCycle = false;
            // Calculate radialDistance and angle.
            radialDistSq = (xPos - XCENTER) * (xPos - XCENTER) + (yPos - XCENTER) * (yPos - XCENTER);
            angle = (float) Math.atan2(yPos - XCENTER, xPos - XCENTER);

            // Resolve velocity into radial and tangential vectors relative to the direction of the firefly.
            alpha = direction - angle;
            velRadial = velocity * (float) Math.cos(alpha);
            velTangential = velocity * (float) Math.sin(alpha);

            // Check for collision with the ends of the target. Each collision should only be counted
            // once, irrespective of how many cycles it lasts for.
            if ((angle < targetLeftEnd && targetLeftEnd - angle < 0.5f)
                    || (angle > targetRightEnd) && angle - targetRightEnd < 0.5f) {
                if (!tangentialCollisionAlreadyOccurring) {
                    velTangential = -velTangential;
                    collisionThisCycle = true;
                    tangentialCollisionAlreadyOccurring = true;
                }
            } else {
                collisionThisCycle = false;
                tangentialCollisionAlreadyOccurring = false;
            }
            if (targetLeftEnd < targetRightEnd) {
                if (angle < targetLeftEnd && Helpers.resolveAngle(targetLeftEnd - angle) < PI) {
                    angle = targetAngle;
                }
                if (angle > targetRightEnd && Helpers.resolveAngle(angle - targetRightEnd) < 0.5f) {
                    angle = targetAngle;
                }
            } else {
                if (targetAngle < targetLeftEnd) { // target would always fail leftEnd test
                    if (angle + (PI * 2) < targetLeftEnd && Helpers.resolveAngle(targetLeftEnd - angle) < PI) {
                        angle = targetAngle;
                    }
                    if (angle > targetRightEnd && Helpers.resolveAngle(angle - targetRightEnd) < 0.5f) {
                        angle = targetAngle;
                    }
                }
                if (targetAngle > targetRightEnd) { // target would always fail rightEnd test
                    if (angle < targetLeftEnd && Helpers.resolveAngle(targetLeftEnd - angle) < PI) {
                        angle = targetAngle;
                    }
                    if (angle - (PI * 2) > targetRightEnd && Helpers.resolveAngle(angle - targetRightEnd) < 0.5f) {
                        angle = targetAngle;
                    }
                }
            }


            // Check for collision with the inner and outer edges of the target.
            if (radialDistSq >= targetOuterRadiusSq || radialDistSq <= targetInnerRadiusSq) {
                if (!radialCollisionAlreadyOccurring) {
                    velRadial = - velRadial;
                    collisionThisCycle = true;
                    radialCollisionAlreadyOccurring = true;
                }
            } else {
                collisionThisCycle = false;
                radialCollisionAlreadyOccurring = false;
            }


            // Collapse vectors back into velocity and direction.
            velocity = (float) Math.sqrt(velRadial * velRadial + velTangential * velTangential);
            alpha = (float) Math.atan2(velTangential, velRadial);
            direction = alpha + angle;

            // Derive xPos and yPos from radialDistSq and angle.
            xPos = (float) (XCENTER +  Math.sqrt(radialDistSq) * Math.cos(angle));
            yPos = (float) (XCENTER +  Math.sqrt(radialDistSq) * Math.sin(angle));

            return collisionThisCycle;
        }

        void updatePosition() {
            xPos += velocity * (float) Math.cos(direction);
            yPos += velocity * (float) Math.sin(direction);
        }

        boolean checkForCollisionWithCircle() {
            boolean collisionThisCycle = false;
            // Calculate radialDistance and angle.
            radialDistSq = (xPos - XCENTER) * (xPos - XCENTER) + (yPos - XCENTER) * (yPos - XCENTER);
            angle = (float) Math.atan2(yPos - XCENTER, xPos - XCENTER);

            // Resolve velocity into radial and tangential vectors relative to the direction of the firefly.
            alpha = (float) Helpers.resolveAngle(direction - angle);
            velRadial = velocity * (float) Math.cos(alpha);
            velTangential = velocity * (float) Math.sin(alpha);

            // If the target has moved beyond the radius of the circle, a collision is detected.
            if (radialDistSq >= CIRCLE_RADIUS_SQ) {
                if (!radialCollisionAlreadyOccurring) {
                    radialCollisionAlreadyOccurring = true;
                    collisionThisCycle = true;
                    velRadial = -velRadial;
                }
            } else {
                radialCollisionAlreadyOccurring = false;
                collisionThisCycle = false;
            }


            // Recombine the vectors.
            velocity = (float) Math.sqrt(velRadial * velRadial + velTangential * velTangential);
            alpha = (float) Math.atan2(velTangential, velRadial);
            direction = (float)Helpers.resolveAngle(alpha + angle);

            return collisionThisCycle;
        }
    }
}
