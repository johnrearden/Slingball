package com.intricatech.slingball;

public class TarTadpole extends AbstractTarget {
	
	// Instance fields particular to TarTadpole :-
    private float minVelocity, maxVelocity;		// Highest and lowest speeds respectively
    private float tadpoleAccel, tadpoleDecel;		// Rate of increase and decrease in speed respectively
    private int cyclesAtMinimumVelocity;			// Length of time (in updateDifficultyDependents cycles) to spend at the minVelocity
    private int counter;							// counts the length of cycles at minVelocity;
	float signumDirection;
    private int cyclesCounter;


	enum TadpolePhase {ACCELERATING,
    				   DECELERATING,
    				   CONSTANT
    }
    TadpolePhase tadpolePhase;

	enum AutopilotPhase {
        ACCELERATING, DECELERATING, CONSTANT;

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
    AutopilotPhase autopilotPhase, autopilotEvaluator;

    
    public TarTadpole (TargetSize size,
                       int orbitIndex,
                       LevelManager levelManager) {
    	// Call the superclass constructor to initialize values set by IntRepConsts.
    	super();
    	this.levelManager = levelManager;
    	levelManager.register(this);
    	
    	this.size = size;
        type = TargetType.TADPOLE;
		spriteType = SpriteType.TADPOLE_HALOED;
        minVelocity = IntRepConsts.TARGET_MAX_VELOCITY * 0.35f;
        maxVelocity = IntRepConsts.TARGET_MAX_VELOCITY * 1.7f;
        tadpoleAccel = maxVelocity * 0.01f;
        tadpoleDecel = maxVelocity * 0.0015f;
		cyclesAtMinimumVelocity = 0;
		reportedAcceleration = 0;
        this.orbitIndex = orbitIndex;
        score = 10.0;
		energyValue = 12.0;
		isShielded = true;
		shieldHealth = 100.0f;
		shieldHealthIncrement = 50.0f;
        
        counter = 0;
        tadpolePhase = TadpolePhase.ACCELERATING;
        autopilotPhase = AutopilotPhase.ACCELERATING;
        autopilotEvaluator = AutopilotPhase.ACCELERATING;

        color = TargetColor.BLUE;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();
    }
    
    void doTargetSpecificUpdate() {
		// If target is still shielded, show TADPOLE_HALO sprite, otherwise show normal TADPOLE
		if (isShielded) {
			spriteType = SpriteType.TADPOLE_HALOED;
		} else {
			spriteType = SpriteType.TADPOLE;
		}

		// Calculate the next value for the velocity of the target.
		updateVelocity();
	}


	void updateVelocity() {
		reportedAcceleration = 0;
		signumDirection = Math.signum(velocity);
		switch (tadpolePhase) {
		
		case ACCELERATING : {
			if (velocity >= maxVelocity || velocity <= -maxVelocity) {
				tadpolePhase = TadpolePhase.DECELERATING;
                autopilotPhase = AutopilotPhase.DECELERATING;
			} else {
				velocity += tadpoleAccel * signumDirection;
				reportedAcceleration = tadpoleAccel * signumDirection;
			}
			break;
		}
			
		case DECELERATING : {
			if (velocity <= minVelocity && velocity >= -minVelocity) {
				tadpolePhase = TadpolePhase.CONSTANT;
                autopilotPhase = AutopilotPhase.CONSTANT;
			} else {
				velocity = velocity - tadpoleDecel * signumDirection;
				reportedAcceleration = -tadpoleDecel * signumDirection;
			}
			break;
		}
			
		case CONSTANT : {
			if (counter > cyclesAtMinimumVelocity) {
				counter = 0;
				tadpolePhase = TadpolePhase.ACCELERATING;
                autopilotPhase = AutopilotPhase.ACCELERATING;
			} else {
				counter++;
			}
			break;
		}
		}
	}

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        autopilotEvaluator = autopilotPhase;
        cyclesCounter = cyclesUntilArrival;
        float dist = 0;
        while (cyclesCounter > 0) {
            dist += calculateDistForCurrentCycle();
            autopilotEvaluator = autopilotEvaluator.moveToNextPhase();
        }
        float newAngle = (float) Helpers.resolveAngle(alpha + dist);

        float minAngle = newAngle - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
        float maxAngle = newAngle + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;

        if (angle > minAngle && angle < maxAngle) {
            return true;
        } else {
            return false;
        }
    }

    private float calculateDistForCurrentCycle() {
        float distance = 0;
        switch (autopilotEvaluator) {
            case ACCELERATING:
                int cyclesTillDecel = (int) ((maxVelocity - Math.abs(velocity)) / tadpoleAccel);
                if (cyclesCounter < cyclesTillDecel) {
                    distance = velocity * cyclesCounter + (0.5f * signumDirection * tadpoleAccel * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillDecel + (0.5f * signumDirection * tadpoleAccel * cyclesTillDecel * cyclesTillDecel);
                    cyclesCounter -= cyclesTillDecel;
                }
                break;
            case DECELERATING:
                int cyclesTillConstant = (int) ((Math.abs(velocity) - minVelocity) / tadpoleDecel);
                if (cyclesCounter < cyclesTillConstant) {
                    distance = velocity * cyclesCounter + (0.5f * signumDirection * -tadpoleDecel * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillConstant + (0.5f * signumDirection * -tadpoleDecel * cyclesTillConstant * cyclesTillConstant);
                    cyclesCounter -= cyclesTillConstant;
                }
                break;
            case CONSTANT:
                int cyclesTillAccel = cyclesAtMinimumVelocity - counter;
                if (cyclesCounter < cyclesTillAccel) {
                    distance = velocity * cyclesCounter;
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesTillAccel;
                    cyclesCounter -= cyclesTillAccel;
                }

                break;
        }
        return distance;
    }

	@Override
	public void updateConstants(int level) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void configureTargetToCurrentLevel(int level) {
		// TODO Auto-generated method stub
		
	}
	
}
