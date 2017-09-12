package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 26/03/2016.
 */
public class TarDodger extends AbstractTarget {

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        if (state == State.DODGING
                || (state == State.NOT_DODGING && notDodgingCountdown < cyclesUntilArrival)) {
            return false;
        } else {
            float futureAngle = (float) Helpers.resolveAngle(alpha + velocity * cyclesUntilArrival);

            float minAngle = futureAngle - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
            float maxAngle = futureAngle + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;

            if (angle > minAngle && angle < maxAngle) {
                return true;
            } else {
                return false;
            }
        }
    }

    // Fields specific to TarDodger.
    enum State {
        DODGING,
        NOT_DODGING
    }

    static final int BASE_NOT_DODGING_RANGE = 120 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;

    State state;
    int dodgingCountdown;
    int notDodgingCountdown;
    int minNotDodgingCycles;
    int minDodgingCycles;
    int notDodgingRange;
    int dodgingRange;

    public TarDodger (TargetSize size,
                       int orbitIndex,
                       LevelManager levelManager) {

        // Call the superclass constructor to initialize values set by IntRepConsts.
        super();
        this.levelManager = levelManager;
        levelManager.register(this);

        this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.DODGER;
        spriteType = SpriteType.DODGER_ON;
        acceleration = IntRepConsts.TARGET_ACCELERATION;
        reportedAcceleration = 0;
        score = 30.0;
        energyValue = 18.0;

        color = TargetColor.GREEN;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();

        state = State.DODGING;
        behaviour = TargetBehaviour.AVOIDING;
        minNotDodgingCycles = 120 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        minDodgingCycles = 120 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
        notDodgingRange = BASE_NOT_DODGING_RANGE / (levelManager.getLevel() + 2);
        dodgingRange = 120 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;

        dodgingCountdown = (int)(minDodgingCycles + Math.random() * dodgingRange);
    }

    @Override
    public void updateConstants(int level) {


    }

    @Override
    void doTargetSpecificUpdate() {

        switch (state) {
            case DODGING: {
                // Decelerate to a halt.
                if (velocity > 0 && velocity < maxVel) {
                    velocity += acceleration;
                    reportedAcceleration = acceleration;
                    if (velocity > maxVel) velocity = maxVel;
                }
                if (velocity < 0 && velocity > -maxVel) {
                    velocity -= acceleration;
                    reportedAcceleration = -acceleration;
                    if (velocity < -maxVel) velocity = -maxVel;
                }

                if (--dodgingCountdown <= 0) {
                    notDodgingCountdown = (int)(minNotDodgingCycles + Math.random() * notDodgingRange);
                    state = State.NOT_DODGING;
                    behaviour = TargetBehaviour.STANDARD;
                    spriteType = SpriteType.DODGER_OFF;
                }
                break;
            }
            case NOT_DODGING: {
                // Accelerate to maxVel.

                if (velocity > 0 && velocity < maxVel) {
                    velocity += acceleration;
                    reportedAcceleration = acceleration;
                    if (velocity > maxVel) velocity = maxVel;
                }
                if (velocity < 0 && velocity > -maxVel) {
                    velocity -= acceleration;
                    reportedAcceleration = -acceleration;
                    if (velocity < -maxVel) velocity = -maxVel;
                }

                if (velocity > maxVel) {
                    reportedAcceleration = -avoidingAcceleration * AVOIDING_DECELERATION_TO_ACCELERATION_RATIO;
                    velocity += reportedAcceleration;
                }
                if (velocity < - maxVel) {
                    reportedAcceleration = avoidingAcceleration * AVOIDING_DECELERATION_TO_ACCELERATION_RATIO;
                    velocity += reportedAcceleration;
                }
                if (--notDodgingCountdown <= 0) {
                    dodgingCountdown = (int)(minDodgingCycles + Math.random() * dodgingRange);
                    state = State.DODGING;
                    behaviour = TargetBehaviour.AVOIDING;
                    spriteType = SpriteType.DODGER_ON;
                }
                break;
            }
            default : break;
        }
    }

    @Override
    void configureTargetToCurrentLevel(int level) {


    }

}
