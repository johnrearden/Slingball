package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 04/06/2016.
 */
public class TarRewarder extends AbstractTarget {

    int cyclesBeforeDeletion;
    boolean diedWithHisBootsOn;
    DifficultyLevel difficultyLevel;

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        float futureAngle = (float) Helpers.resolveAngle(alpha + velocity * cyclesUntilArrival);

        float minAngle = futureAngle - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
        float maxAngle = futureAngle + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;

        if (angle > minAngle && angle < maxAngle) {
            return true;
        } else {
            return false;
        }
    }

    enum BonusType {
        STANDARD_RANDOM,
        EXTRA_HEART
    }
    BonusType bonusType;


    public TarRewarder (TargetSize size,
                        int orbitIndex,
                        LevelManager levelManager,
                        DifficultyLevel difficultyLevel) {

        // Call the superclass constructor to initialize values set by IntRepConsts.
        super();
        this.levelManager = levelManager;
        levelManager.register(this);
        this.difficultyLevel = difficultyLevel;

        this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.REWARDER;
        spriteType = SpriteType.REWARDER;
        acceleration = IntRepConsts.TARGET_ACCELERATION;
        reportedAcceleration = 0;
        score = 0.0;
        energyValue = 0.0;
        cyclesBeforeDeletion = 300 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        diedWithHisBootsOn = false;

        color = TargetColor.GREEN;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();

        double randomNumber = Math.random();
        float threshold;
        switch (difficultyLevel) {
            case EASY:
            default :
                threshold = IntRepConsts.FLYING_HEART_BASE_PROBABILITY;
                break;
            case NORMAL:
                threshold = IntRepConsts.FLYING_HEART_BASE_PROBABILITY * 0.9f;
                break;
            case HARD:
                threshold = IntRepConsts.FLYING_HEART_BASE_PROBABILITY * 0.8f;
                break;
        }
        if (randomNumber < threshold) {
            bonusType = BonusType.EXTRA_HEART;
        } else bonusType = BonusType.STANDARD_RANDOM;
    }

    @Override
    void doTargetSpecificUpdate() {
        if (cyclesBeforeDeletion-- <= 0) {
            taggedForDeletion = true;
            diedWithHisBootsOn = true;
        }
    }

    @Override
    void configureTargetToCurrentLevel(int level) {

    }

    @Override
    public void updateConstants(int level) {

    }
}
