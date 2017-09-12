package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 03/08/2017.
 */
public class TarKiller extends AbstractTarget {

    private final String TAG;
    private final float SLOW_TO_FAST_ACCEL_THRESHOLD = 0.1f;
    private final float SLOW_ACCEL_RATIO = 0.33f;
    private final float SLOW_DECEL_RATIO = 0.17f;

    enum State {
        STOPPED, ACCELERATING, AT_MAX_VEL, DECELERATING
    }
    private State state;

    enum AutopilotPhase {
        STOPPED, SLOW_ACCEL, FAST_ACCEL, FAST_DECEL, SLOW_DECEL;
        
        AutopilotPhase moveToNextPhase() {
            int ord = this.ordinal();
            AutopilotPhase[] array = AutopilotPhase.values();
            ord++;
            if (ord == array.length) {
                ord = 0;
            }
            return array[ord];
        }
    }
    AutopilotPhase autopilotPhase;
    AutopilotPhase autopilotEvaluator;

    private static final float BASE_STOPPED_PERIOD = 2.0f;
    private static final float BASE_MAX_VEL_PERIOD = 0.0f;

    private int stoppedPeriod;
    private int maxVelPeriod;
    private int direction;
    private float deceleration;
    private int counter;
    private int cyclesCounter;

    public TarKiller (TargetSize size,
                      int orbitIndex,
                      LevelManager levelManager) {

        // Call the superclass constructor to initialize values set by IntRepConsts.
        super();
        this.levelManager = levelManager;
        levelManager.register(this);

        TAG = getClass().getSimpleName();

        this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.KILLER;
        spriteType = SpriteType.KILLER_ON;
        state = State.STOPPED;
        autopilotPhase = AutopilotPhase.STOPPED;
        autopilotEvaluator = AutopilotPhase.STOPPED;
        invulnerable = true;

        acceleration = IntRepConsts.TARGET_ACCELERATION * 0.5f;
        deceleration = IntRepConsts.TARGET_ACCELERATION;
        reportedAcceleration = 0;
        score = 100.0;
        energyValue = 15.0;

        color = TargetColor.GOLD;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {

    }

    @Override
    void doTargetSpecificUpdate() {
        switch (state) {
            default:
            case STOPPED:
                if (counter-- <= 0) {
                    chooseRandomDirection();
                    state = State.ACCELERATING;
                    switchOffDamager();
                    counter = maxVelPeriod;
                }
                break;
            case ACCELERATING:
                if (Math.abs(velocity) < maxVel * SLOW_TO_FAST_ACCEL_THRESHOLD) {
                    velocity += direction * acceleration * SLOW_ACCEL_RATIO;
                    autopilotPhase = AutopilotPhase.SLOW_ACCEL;
                } else {
                    velocity += direction * acceleration;
                    autopilotPhase = AutopilotPhase.FAST_ACCEL;
                }

                if (Math.abs(velocity) >= maxVel) {
                    state = State.AT_MAX_VEL;

                }
                break;
            case AT_MAX_VEL:
                if (counter-- <= 0) {
                    state = State.DECELERATING;
                    counter = stoppedPeriod;
                }
                break;
            case DECELERATING:
                if (Math.abs(velocity) < maxVel * SLOW_TO_FAST_ACCEL_THRESHOLD) {
                    velocity -= direction * deceleration * SLOW_DECEL_RATIO;
                    autopilotPhase = AutopilotPhase.SLOW_DECEL;
                } else {
                    velocity -= direction * deceleration;
                    autopilotPhase = AutopilotPhase.FAST_DECEL;
                }
                if (Math.abs(velocity) <= deceleration) {
                    velocity = 0;
                    state = State.STOPPED;
                    autopilotPhase = AutopilotPhase.STOPPED;
                    switchOnDamager();
                }
                break;
        }

    }

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        autopilotEvaluator = autopilotPhase;
        float dist = 0;
        cyclesCounter = cyclesUntilArrival;
        while (cyclesCounter > 0) {
            dist += calculateDistForCurrentCycle();
            autopilotEvaluator = autopilotEvaluator.moveToNextPhase();
        }

        float newAngle = (float) Helpers.resolveAngle(alpha + dist);

        float minAngle, maxAngle;
        if (autopilotPhase == AutopilotPhase.STOPPED && cyclesUntilArrival < counter) {
            minAngle = newAngle - size.getAngularSize() * 1.5f - ballAngularWidth;
            maxAngle = newAngle + size.getAngularSize() * 1.5f + ballAngularWidth;
        } else {
            minAngle = newAngle - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
            maxAngle = newAngle + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
        }


        if (angle > minAngle && angle < maxAngle) {
            return true;
        } else {
            return false;
        }

    }

    private float calculateDistForCurrentCycle() {
        float distance = 0;
        switch (autopilotEvaluator) {

            case STOPPED:
                break;

            case SLOW_ACCEL:
                int cyclesTillFastAccel = (int)((maxVel * SLOW_TO_FAST_ACCEL_THRESHOLD - Math.abs(velocity))
                                            / Math.abs(acceleration * SLOW_ACCEL_RATIO));
                if (cyclesCounter < cyclesTillFastAccel) {
                    distance = velocity * cyclesCounter + (0.5f * acceleration * SLOW_ACCEL_RATIO * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillFastAccel
                            + (0.5f * acceleration * SLOW_ACCEL_RATIO * cyclesTillFastAccel * cyclesTillFastAccel);
                    cyclesCounter -=cyclesTillFastAccel;
                }
                break;

            case FAST_ACCEL:
                int cyclesTillFastDecel = (int) ((maxVel - Math.abs(velocity)) / Math.abs(acceleration));
                if (cyclesCounter < cyclesTillFastDecel) {
                    distance = velocity * cyclesCounter + (0.5f * acceleration * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillFastDecel + (0.5f * acceleration * cyclesTillFastDecel * cyclesTillFastDecel);
                    cyclesCounter -= cyclesTillFastDecel;
                }
                break;
            
            case FAST_DECEL:
                int cyclesTillSlowDecel = (int) ((Math.abs(velocity) - maxVel * SLOW_TO_FAST_ACCEL_THRESHOLD) 
                                            / Math.abs(deceleration));
                if (cyclesCounter < cyclesTillSlowDecel) {
                    distance = velocity * cyclesCounter + (0.5f * deceleration * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillSlowDecel + (0.5f * deceleration * cyclesTillSlowDecel * cyclesTillSlowDecel);
                    cyclesCounter -= cyclesTillSlowDecel;
                }
                break;
            
            case SLOW_DECEL:
                int cyclesTillStopped = (int) (Math.abs(velocity) / Math.abs(deceleration * SLOW_DECEL_RATIO));
                if (cyclesCounter < cyclesTillStopped) {
                    distance = velocity * cyclesCounter + (0.5f * deceleration * SLOW_DECEL_RATIO * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillStopped
                            + (0.5f * deceleration * SLOW_DECEL_RATIO * cyclesTillStopped * cyclesTillStopped);
                    cyclesCounter -= cyclesTillStopped;
                }
                break;
        }
        return distance;
    }

    public void configMe(DifficultyLevel difficultyLevel) {
        float modifier;
        switch (difficultyLevel) {
            default:
            case EASY:
                modifier = 1.0f;
                break;
            case NORMAL:
                modifier = 1.5f;
                break;
            case HARD:
                modifier = 2.0f;
                break;
        }
        stoppedPeriod = (int) (BASE_STOPPED_PERIOD * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE * 60 * modifier);
        counter = stoppedPeriod;
        maxVelPeriod = (int) (BASE_MAX_VEL_PERIOD * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE * 60 * modifier);
        chooseRandomDirection();
    }

    private void chooseRandomDirection() {
        direction = (int) (Math.round(Math.random()) * 2 - 1);
    }

    private void switchOnDamager() {
        spriteType = SpriteType.KILLER_ON;
        invulnerable = true;
    }

    private void switchOffDamager() {
        spriteType = SpriteType.KILLER_OFF;
        invulnerable = false;
    }

    @Override
    void configureTargetToCurrentLevel(int level) {

    }

    @Override
    public void updateConstants(int level) {

    }

    
}
