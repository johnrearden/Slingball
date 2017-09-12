package com.intricatech.slingball;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Bolgbolg on 24/04/2016.
 */
public class TarDecoys extends AbstractTarget {

    LevelManager levelManager;
    DecoyBehaviour decoyBehaviour;
    private final String TAG;
    private static final float PI = (float)Math.PI;
    private static final float PI_DIV_2 = (float) (Math.PI / 2);

    List<Float> positionList;
    List<Float> availablePositions = new ArrayList<>(Arrays.asList(0.0f, PI_DIV_2, PI, -PI_DIV_2,
            PI / 4, PI * 3 / 4, -PI / 4, -PI * 3 / 4));
    int stoppedPeriod;
    int stoppedCountdown;
    private DifficultyLevel diffLevel;
    private int numberOfDestinations;

    private float destination;
    private int nextIndex;

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        if (decoyBehaviour == DecoyBehaviour.STOPPED && cyclesUntilArrival < stoppedCountdown) {
            float futureAngle = (float) Helpers.resolveAngle(alpha + velocity * cyclesUntilArrival);
            float minAngle = futureAngle - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
            float maxAngle = futureAngle + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;

            if (angle > minAngle && angle < maxAngle) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    enum DecoyBehaviour {
        STOPPED,
        MOVING
    }

    TarDecoys (
            TargetSize size,
            int orbitIndex,
            LevelManager levelManager
    ) {
        super();
        TAG = getClass().getSimpleName();
        this.levelManager = levelManager;
        levelManager.register(this);
        this.orbitIndex = orbitIndex;
        this.size = size;
        behaviour = TargetBehaviour.STANDARD;
        decoyBehaviour = DecoyBehaviour.MOVING;
        stoppedCountdown = stoppedPeriod;
        type = TargetType.DECOYS;
        spriteType = SpriteType.DECOY_NORMAL;
        acceleration = IntRepConsts.TARGET_ACCELERATION * 6;
        rangeToDeceleration = IntRepConsts.RANGE_TO_DECELERATION / 2;

        score = 40.0;
        energyValue = 15.0;
        color = TargetColor.DECOY_RED;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();
    }

    @Override
    void doTargetSpecificUpdate() {
        switch(decoyBehaviour) {
            case STOPPED: {
                if (--stoppedCountdown <= 0) {
                    decoyBehaviour = DecoyBehaviour.MOVING;
                    nextIndex++;
                    if (nextIndex == positionList.size()) {
                        nextIndex = 0;
                    }
                    destination = positionList.get(nextIndex);
                }
                break;
            }
            case MOVING: {
                // If the target is already moving, then check if it is closer to the destination than the
                // RANGE_TO_DECELERATION. If it is, then slow to a chauffer stop at the destination. If not
                // accelerate in the direction of the destination (limited by maxVel).
                double diffInAngle = Helpers.resolveAngle(destination - alpha);
                double absoluteDiffInAngle = Math.abs(diffInAngle);
                int directionOfDiffInAngle = (int) Math.signum(diffInAngle);

                if (absoluteDiffInAngle < rangeToDeceleration) {
                    float terminalDecel = (float)(Math.pow(velocity, 2) / (2 * diffInAngle));
                    velocity = velocity - terminalDecel;
                    reportedAcceleration = -terminalDecel;

                    // If the target is very close to its destination, switch to STOPPED phase.
                    if (absoluteDiffInAngle < 0.005) {
                        velocity = 0;
                        decoyBehaviour = DecoyBehaviour.STOPPED;
                        stoppedCountdown = stoppedPeriod;
                    }
                } else {
                    reportedAcceleration = acceleration * directionOfDiffInAngle;
                    velocity = velocity + reportedAcceleration;

                }
                break;
            }
        }
    }

    private void createNewPositionList() {

    }

    @Override
    void configureTargetToCurrentLevel(int level) {

    }

    @Override
    public void updateConstants(int level) {

    }

    public void setDifficultyLevel(DifficultyLevel diffLevel) {
        this.diffLevel = diffLevel;
        switch (diffLevel) {
            case EASY: {
                stoppedPeriod = 70 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
                numberOfDestinations = 2;
                break;
            }
            case NORMAL: {
                stoppedPeriod = 50 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
                numberOfDestinations = 3;
                break;
            }
            case HARD: {
                stoppedPeriod = 30 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
                numberOfDestinations = 3;
                break;
            }
        }
        getNewPositionList();
        nextIndex = 0;
        destination = positionList.get(0);
    }

    private void getNewPositionList() {
        positionList = new ArrayList<>();
        /*while (availablePositions.size() > 0) {
            int index = (int) (Math.random() * availablePositions.size());
            positionList.add(availablePositions.remove(index));
        }*/
        for (int i = 0; i < numberOfDestinations; i++) {
            int index = (int) (Math.random() * availablePositions.size());
            positionList.add(availablePositions.remove(index));
        }
        for (float f : positionList) {
            Log.d(TAG, String.valueOf(f));
        }
    }
}
