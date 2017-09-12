package com.intricatech.slingball;


public class TarDarter extends AbstractTarget {

    private static String TAG = "TarDarter";
    enum DarterPhase {
        MOVING,
        STOPPED
    }

    enum AutopilotPhase {
        STOPPED, ACCELERATING, AT_MAX_VEL, DECELERATING;

        AutopilotPhase moveToNextPhase() {
            int ord = this.ordinal();
            ord++;
            AutopilotPhase[] array = AutopilotPhase.values();
            if (ord >= array.length) {
                ord = 0;
            }
            return array[ord];
        }
    }

	// Instance fields particular to TarDarter.
	double destination;
	DarterPhase darterPhase;
	AutopilotPhase autopilotPhase, autopilotEvaluator;
    float terminalDecel;
	int minStoppedPeriod;
	int stoppedPeriodRange;
	int stoppedPeriodCountdown;
    int cyclesCounter;
    int cyclesTillDeceleration;
	
	public TarDarter (TargetSize size,
            			int orbitIndex, 
						LevelManager levelManager) {
		super();
		this.levelManager = levelManager;
		levelManager.register(this);
		this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.DARTER;
		spriteType = SpriteType.DARTER;
        acceleration = IntRepConsts.TARGET_ACCELERATION * 5;
		reportedAcceleration = 0;
        darterPhase = DarterPhase.MOVING;
        autopilotPhase = AutopilotPhase.ACCELERATING;
        setNewRandomIntendedDestination();
        score = 12.0;
		energyValue = 6.0;
        
        color = TargetColor.PURPLE;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();

		minStoppedPeriod = IntRepConsts.DARTER_MIN_STOPPED_PERIOD
			* IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
		stoppedPeriodRange = IntRepConsts.DARTER_STOPPED_PERIOD_RANGE
			* IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
	}
	
	@Override
	void doTargetSpecificUpdate() {

		reportedAcceleration = 0;
        switch (darterPhase) {
		
		case MOVING : {
			
			// If the target is already moving, then check if it is closer to the destination than the
			// RANGE_TO_DECELERATION. If it is, then slow to a chauffer stop at the destination. If not
			// accelerate in the direction of the destination (limited by maxVel).
			double diffInAngle = Helpers.resolveAngle(destination - alpha);
			double absoluteDiffInAngle = Math.abs(diffInAngle);
    		int directionOfDiffInAngle = (int) Math.signum(diffInAngle);
    		
    		if (absoluteDiffInAngle < rangeToDeceleration) {
    			terminalDecel = (float)(Math.pow(velocity, 2) / (2 * diffInAngle));
                autopilotPhase = AutopilotPhase.DECELERATING;
    			velocity = velocity - terminalDecel;
				reportedAcceleration = -terminalDecel;
    			
    			// If the target is very close to its destination, switch to STOPPED phase.
    			if (absoluteDiffInAngle < 0.02) {
    				velocity = 0;
    				darterPhase = DarterPhase.STOPPED;
                    autopilotPhase = AutopilotPhase.STOPPED;
					stoppedPeriodCountdown = (int) (minStoppedPeriod + Math.random() * stoppedPeriodRange);
    			}
    		} else {
                cyclesTillDeceleration = (int) ((absoluteDiffInAngle - rangeToDeceleration) / Math.abs(velocity));

                reportedAcceleration = acceleration * directionOfDiffInAngle;
    			velocity = velocity + reportedAcceleration;
                float absVel = Math.abs(velocity);
                if (absVel < maxVel) {
                    autopilotPhase = AutopilotPhase.ACCELERATING;
                } else if (absVel >= maxVel) {
                    autopilotPhase = AutopilotPhase.AT_MAX_VEL;
                }
    		}
    		break;
		}
		
		case STOPPED : {
			
			// If the target is stopped, decide based on the probabilityOfDestinationChange whether
			// or not to set a new destination and switch to DarterPhase.MOVING. If the new destination 
			// is inside the RANGE_TO_DECELERATION * 2, the target will not gain enough speed from its
			// current stationary position to do anything other than crawl to the next destination, as the 
			// deceleration phase will kick in immediately. (No accel at all if inside the R_T_D).
			if (stoppedPeriodCountdown-- <= 0) {
    			setNewRandomIntendedDestination();
    			darterPhase = DarterPhase.MOVING;
    		}
			break;
		}
		}
    }

	void setNewRandomIntendedDestination() {
		float start = alpha + rangeToDeceleration * 2;
		float range = (float) Math.PI * 2 - rangeToDeceleration * 4;
		destination = Helpers.resolveAngle(start + range * Math.random());
	}

	@Override
	public void updateConstants(int level) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void configureTargetToCurrentLevel(int level) {
		// TODO Auto-generated method stub
		
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
            case STOPPED:
                if (stoppedPeriodCountdown > cyclesCounter) {
                    cyclesCounter = 0;
                    distance = 0;
                } else {
                    cyclesCounter -= stoppedPeriodCountdown;
                    distance = 0;
                }
                break;
            case ACCELERATING:
                int cyclesUntilMaxvel = ((int) Math.abs(maxVel / reportedAcceleration));
                if (cyclesUntilMaxvel > cyclesCounter) {
                    distance = velocity * cyclesCounter + (0.5f * reportedAcceleration * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;

                } else {
                    cyclesCounter -= cyclesUntilMaxvel;
                    distance = velocity * cyclesUntilMaxvel + (0.5f * reportedAcceleration * cyclesUntilMaxvel * cyclesUntilMaxvel);
                }
                break;
            case AT_MAX_VEL:
                if (cyclesCounter < cyclesTillDeceleration) {
                    distance = velocity * cyclesCounter;
                    cyclesCounter = 0;
                } else {
                    cyclesCounter -= cyclesTillDeceleration;
                    distance = velocity * cyclesTillDeceleration;
                }
                break;
            case DECELERATING:
                int cyclesUntilStopped = (int) Math.abs(velocity / terminalDecel);
                if (cyclesCounter < cyclesUntilStopped) {
                    distance = velocity * cyclesCounter + (0.5f * -terminalDecel * cyclesCounter * cyclesCounter);
                    cyclesCounter = 0;
                } else {
                    distance = velocity * cyclesUntilStopped + (0.5f * -terminalDecel * cyclesUntilStopped * cyclesUntilStopped);
                    cyclesCounter -= cyclesUntilStopped;
                }
                break;
        }
        return distance;
    }
}
