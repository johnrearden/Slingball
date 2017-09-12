package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 02/04/2016.
 */
public class TarFlicker extends AbstractTarget {

    LevelManager levelManager;

    int flickerMinPeriod;
    int flickerPeriodRange;
    int flickerDuration;
    int flickerOnCountdown;
    int flickerOffCountdown;
    float darknessIncrement;

    static final int numberOfSprites = IntRepConsts.FLICKER_NUMBER_OF_SPRITES;
    static final int flickerSpritePersistence = IntRepConsts.FLICKER_SPRITE_PERSISTENCE *
            IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
    int spriteSwitchCountdown;
    int spriteInUse;

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

    enum FlickerState {
        ON,
        FADING,
        OFF
    }
    FlickerState flickerState;

    TarFlicker(
            TargetSize size,
            int orbitIndex,
            LevelManager levelManager) {

        super();
        type = TargetType.FLICKER;
        this.size = size;
        this.orbitIndex = orbitIndex;
        this.levelManager = levelManager;
        levelManager.register(this);

        score = 20.0;
        energyValue = 13.0;
        color = TargetColor.OFF_WHITE;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        reportedAcceleration = 0;
        distFromOrigin = setDistFromOrigin();

        spriteType = SpriteType.FLICKER_OFF;
        flickerState = FlickerState.OFF;
        opacity = 0;
        swingballRange = IntRepConsts.DIAMETER * 0.15f;
        flickerOffCountdown = 0;
        flickerOnCountdown = 0;
        spriteSwitchCountdown = 0;
        spriteInUse = 0;
        flickerMinPeriod = IntRepConsts.FLICKER_MIN_PERIOD * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        flickerPeriodRange = IntRepConsts.FLICKER_PERIOD_RANGE * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        flickerDuration = IntRepConsts.FLICKER_DURATION * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        darknessIncrement = IntRepConsts.DARKNESS_INCREMENT / IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
    }

    @Override
    void doTargetSpecificUpdate() {

        // Light the flicker if the ball is within range.
        if (swingballWithinRange) {
            flickerOnCountdown = flickerDuration;
            flickerState = FlickerState.ON;
            spriteType = SpriteType.FLICKER_ON_1;
            opacity = 0;
        }
        switch (flickerState) {
            case ON: {
                // Check if it is time to switch off the juice.
                if (flickerOnCountdown-- <= 0) {
                    flickerState = FlickerState.FADING;
                    spriteType = SpriteType.FLICKER_OFF;
                }
                // Check if it is time to switch to the next sprite (to animate target)
                if (spriteSwitchCountdown-- <= 0) {
                    spriteInUse = (spriteInUse >= numberOfSprites - 1) ? 0 : ++spriteInUse;
                    changeSpriteType();
                    spriteSwitchCountdown = flickerSpritePersistence;
                }
                break;
            }
            case FADING: {
                // Check if darkness has reached 255. If so, switch to OFF.
                opacity += 2;
                if (opacity >= 255) {
                    opacity = 255;
                    flickerOffCountdown = (int)(flickerMinPeriod + (Math.random() * flickerPeriodRange));
                    flickerState = FlickerState.OFF;
                }
                break;
            }
            case OFF: {
                if (flickerOffCountdown-- <= 0) {
                    flickerOnCountdown = flickerDuration;
                    flickerState = FlickerState.ON;
                    spriteType = SpriteType.FLICKER_ON_1;
                    opacity = 0;
                }
                break;
            }
        }
    }

    void changeSpriteType() {
        if (spriteInUse == 0) {
            spriteType = SpriteType.FLICKER_ON_1;
        } else if (spriteInUse == 1){
            spriteType = SpriteType.FLICKER_ON_2;
        } else {
            spriteType = SpriteType.FLICKER_ON_3;
        }
    }

    @Override
    void configureTargetToCurrentLevel(int level) {
        float levelRatio = 3 * (float)level / IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
        flickerMinPeriod = IntRepConsts.FLICKER_MIN_PERIOD *
                IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE *
                (int)levelRatio;
        flickerPeriodRange = IntRepConsts.FLICKER_PERIOD_RANGE *
                IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE *
                (int)levelRatio;
    }

    @Override
    public void updateConstants(int level) {

    }

}
