package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Bolgbolg on 17/07/2017.
 */
public class FlyingHeart {

    private float xPos, yPos;
    private float accel;
    private float velocity, direction;
    private Bitmap scaledHeartBitmap;
    private Canvas scaledHeartCanvas;
    private float xSize;
    float scaleFactor;
    private float maxVel;

    private PhysicsAndBGRenderer parent;
    private PlayAreaInfo playAreaInfo;
    private ScoreDisplayer scoreDisplayer;
    private PointF targetPoint;

    static final float RATIO_OF_ACCEL_WHILE_TURNING = 0.5f;
    static final float THRESHOLD_FOR_FLYING = 0.2f;
    static final float MAX_SIZE_MULTIPLIER = 3.0f;
    static final float SIZE_INCREMENT = 0.15f;
    static final float TURN_SIZE_FRACTION = 0.1f;

    private Rect scaledHeartSourceRect;
    private RectF scaledHeartDestRect;

    private float distToTargetSq, prevDistToTargetSq;

    enum State {
        OFF,
        TURNING,
        FLYING
    }
    enum Size {
        NORMAL,
        EMBIGGENING,
        LARGE,
        SHRINKING;

        int LARGE_COUNTDOWN_INIT = 15;
        int largeCountdown;
    }
    private State state;
    private Size size;

    FlyingHeart(Resources resources, ScoreDisplayer scoreDisplayer, PhysicsAndBGRenderer parent) {
        this.scoreDisplayer = scoreDisplayer;
        this.parent = parent;
        scaledHeartBitmap = BitmapFactory.decodeResource(resources, R.drawable.heart_icon_ec0000);
        velocity = 0;
        direction = 0;
        state = State.OFF;
        scaleFactor = 1.0f;
        size = Size.NORMAL;
        scaledHeartSourceRect = new Rect();
        scaledHeartDestRect = new RectF();
    }

    void update() {
        int cyclesBeforeMaxVel = ((int) ((maxVel - velocity) / (accel * RATIO_OF_ACCEL_WHILE_TURNING)));
        float directionToTarget = (float) Math.atan2(
                targetPoint.y - yPos,
                targetPoint.x - xPos
        );
        float differenceInAngle = (float) Math.min(
                Math.abs(Helpers.resolveAngle(directionToTarget - direction)),
                Math.abs(Helpers.resolveAngle(direction - directionToTarget))
        );
        float signum = Math.signum(differenceInAngle);

        switch (state) {
            case OFF:
                return;
            case TURNING: {
                if (Math.abs(differenceInAngle) > THRESHOLD_FOR_FLYING) {
/*
                    direction += IntRepConsts.FLYING_HEART_DIRECTION_INCREMENT * signum;
*/
                    direction += differenceInAngle * TURN_SIZE_FRACTION * signum;
                    if (velocity < maxVel) {
                        velocity += accel * RATIO_OF_ACCEL_WHILE_TURNING;
                    }
                } else {
                    state = State.FLYING;
                }
                break;
            }
            case FLYING: {
                if (Math.abs(differenceInAngle) > IntRepConsts.FLYING_HEART_DIRECTION_INCREMENT) {
                    direction += IntRepConsts.FLYING_HEART_DIRECTION_INCREMENT * signum;
                }
                if (velocity < maxVel) {
                    velocity += accel;
                }

                break;
            }
        }

        switch(size) {
            case NORMAL:
                break;
            case EMBIGGENING:
                scaleFactor += SIZE_INCREMENT;
                if (scaleFactor >= MAX_SIZE_MULTIPLIER) {
                    scaleFactor = MAX_SIZE_MULTIPLIER;
                    size = Size.LARGE;
                    size.largeCountdown = size.LARGE_COUNTDOWN_INIT;
                }
                break;
            case LARGE:
                if (size.largeCountdown-- <= 0) {
                    size = Size.SHRINKING;
                }
                break;
            case SHRINKING:
                scaleFactor -= SIZE_INCREMENT;
                if (scaleFactor <= 1.0f) {
                    size = Size.NORMAL;
                }
                break;
        }


        xPos += velocity * Math.cos(direction);
        yPos += velocity * Math.sin(direction);

        // Make turn toward targetPoint.


        // Check for arrival at targetPoint.
        prevDistToTargetSq = distToTargetSq;
        distToTargetSq = (targetPoint.y - yPos) * (targetPoint.y - yPos)
                + (targetPoint.x - xPos) * (targetPoint.x - xPos);
        if (distToTargetSq < Math.pow(maxVel * 5.0f, 2) && prevDistToTargetSq < distToTargetSq) {
            parent.adjustNumberOfLives(1);
            state = State.OFF;
        }
    }


    void drawHeart(Canvas canvas) {
        if (size != Size.NORMAL) {
            float xSize =  scaledHeartBitmap.getWidth() * scaleFactor;
            float ySize = scaledHeartBitmap.getHeight() * scaleFactor;
            scaledHeartDestRect.set(
                    xPos - xSize * 0.5f,
                    yPos - ySize * 0.5f,
                    xPos - xSize * 0.5f + xSize,
                    yPos - ySize * 0.5f + ySize
            );
            canvas.drawBitmap(
                    scaledHeartBitmap,
                    scaledHeartSourceRect,
                    scaledHeartDestRect,
                    null
            );
        } else {
            canvas.drawBitmap(
                    scaledHeartBitmap,
                    xPos - scaledHeartBitmap.getWidth() / 2,
                    yPos - scaledHeartBitmap.getHeight() / 2,
                    null
            );
        }

    }

    void onSurfaceChanged(PlayAreaInfo playAreaInfo) {
        this.playAreaInfo = playAreaInfo;
        xSize = IntRepConsts.HEART_STAR_HEIGHT_RATIO * IntRepConsts.SCORE_YSIZE
                * playAreaInfo.topPanelHeight;
        maxVel = IntRepConsts.FLYING_HEART_MAX_VEL * playAreaInfo.ratioOfActualToModel;
        accel = IntRepConsts.FLYING_HEART_ACCELERATION * playAreaInfo.ratioOfActualToModel;
        xPos = playAreaInfo.xCenterOfCircle;
        yPos = playAreaInfo.yCenterOfCircle;

        scaledHeartBitmap = Bitmap.createScaledBitmap(
                scaledHeartBitmap,
                (int) xSize,
                (int) xSize,
                false
        );
        scaledHeartCanvas = new Canvas(scaledHeartBitmap);
        targetPoint = scoreDisplayer.getCenterOfHeartIcon();
        scaledHeartSourceRect.set(
                0,
                0,
                scaledHeartBitmap.getWidth(),
                scaledHeartBitmap.getHeight()
        );
    }

    boolean activateFlyingHeart(int orbitIndex, float angle, float angVel) {
        if (state != State.OFF) {
            return false;
        } else {
            PointF position = playAreaInfo.getTargetCenterFromPolarCoors(orbitIndex, angle);
            xPos = position.x;
            yPos = position.y;

            float radiusOfOutermostOrbit = playAreaInfo.outermostTargetRect.height() / 2;
            float targetRadialDist = radiusOfOutermostOrbit - orbitIndex * playAreaInfo.scaledGapBetweenOrbits;
            velocity = angVel * targetRadialDist;
            velocity = maxVel * 0.25f;

            float signumVelocity = Math.signum(angVel);
           /* direction = (float) Helpers.resolveAngle(
                    angle + (float) (Math.PI / 2 * signumVelocity)
            );*/
            direction = (float) Helpers.resolveAngle(angle - Math.PI);
            /*direction = (float) Math.PI / 2;*/

            state = State.TURNING;
            size = Size.EMBIGGENING;
            return true;
        }
    }

    State getFlyingHeartState() {
        return state;
    }

    float getSizeFactor() {
        return scaleFactor;
    }
}


