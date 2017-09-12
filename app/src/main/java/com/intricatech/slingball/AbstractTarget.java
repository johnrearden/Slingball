package com.intricatech.slingball;

import static com.intricatech.slingball.TargetBehaviour.*;
import static java.lang.Math.PI;

/**
 * Created by Bolgbolg on 15/06/2015.
 */
public abstract class AbstractTarget implements LevelChangeObserver, TargetCollisionPredictor {

	// Fields common to all targets :-
    TargetType type;
	float opacity;						// the value of the alpha component of the opaqueMaskFilter.
    float alpha;                   		// The current angle the target is at.
    LevelManager levelManager;
    float velocity;                		// Measured in radians per cycle.
    float maxVel;						// The highest possible value for the velocity of the targets.
    float acceleration;					// Measured in radians per cycle^2.
	float reportedAcceleration;			// The acceleration used to modify velocity during the last cycle. Used by swingball.autopilot.
	float avoidingAcceleration;			// The acceleration used when in AVOIDING mode.
	float swingballRange;				// The range that swingballWithinRange flags if inside.

    float trackingAccelerationMultiplier; // the ratio of the deceleration to the acceleration.
    TargetSize size;					// Enum containing hard-coded size for the arc for each target, in radians.
    TargetColor color;					// Enum containing hard-coded values for the possible color of each enum.
	SpriteType spriteType;				// Enum denoting the messageType of sprite to draw for this target.
    TargetBehaviour behaviour;			// Enum containing possible behaviours for target.
    TrackingBehaviourPhase trackingBehaviourPhase; // Enum containing possible subbehaviours while tracking.
    int orbitIndex;       				// Measured from the outside in, with 0 as the outermost orbit.
    int targetThickness;  				// The thickness of the target measured in pixels.
    boolean isShielded;         	// true is Target is a blocker, false otherwise.
	boolean invulnerable;
	boolean swingballWithinRange;		// flags whether ball is inside a range that can be set by each target.
    boolean wasTouchingBall;        	// true if Target was touching ball at last updateDifficultyDependents. This is used
                                    	// to ensure that collisions are not counted twice.
    boolean isGhost;                    // If true, will allow ball to pass straight through target unimpeded
    boolean taggedForDeletion;      	// Flag to indicate to TargetManager that the Target can be removed.
    float shieldHealth;                     	// the current shieldHealth level of the Target.
    double score;
    double energyValue;
    double xCenterOfRotation,
            yCenterOfRotation;      // The coordinates of the center of the game circle.
    float shieldHealthIncrement = IntRepConsts.HEALTH_INCREMENT;
    double distFromOrigin;
    float rangeToDeceleration = IntRepConsts.RANGE_TO_DECELERATION;
	static final float AVOIDING_DECELERATION_TO_ACCELERATION_RATIO = 0.1f;
	static float reactionDistSq = (float) Math.pow(IntRepConsts.REACTION_DIST_FOR_AVOIDANCE, 2);
	static final float maxVelMultiplier = IntRepConsts.TARGET_AVOIDING_MAX_VEL_MULTIPLIER;
	static final float CENTER_OF_GAME_AREA = IntRepConsts.DIAMETER / 2.0f;

	static final float opaqueMaskRatio = IntRepConsts.RATIO_OF_OPAQUE_MASK_TO_TARGET;

	static final float AUTOPILOT_ANGULAR_TOLERANCE = 0.25f;

	float[] decoyPositions;

    DifficultyLevel difficultyLevel;
    
    // Superclass constructor - initializes fields which depend exclusively on IntRepConsts.
    AbstractTarget() {
    	xCenterOfRotation = IntRepConsts.DIAMETER / 2;
    	yCenterOfRotation = IntRepConsts.DIAMETER / 2;
    	targetThickness = IntRepConsts.TARGET_THICKNESS;
		behaviour = TargetBehaviour.STANDARD;
		acceleration = IntRepConsts.TARGET_ACCELERATION;
		avoidingAcceleration = IntRepConsts.TARGET_AVOIDING_ACCELERATION;
		swingballWithinRange = false;
		swingballRange = 0;
    	
    	wasTouchingBall = false;
    	taggedForDeletion = false;
    	isShielded = false;
        invulnerable = false;
        isGhost = false;
    	shieldHealth = 1;
    	energyValue = 25;
    	maxVel = IntRepConsts.TARGET_MAX_VELOCITY;
    	trackingAccelerationMultiplier = (float) IntRepConsts.TRACKING_ACCELERATION_MULTIPLIER;
    	trackingBehaviourPhase = TrackingBehaviourPhase.ACTIVE;

		decoyPositions = new float[IntRepConsts.DECOY_MAX_POPULATION];
    }
    
    //Methods common to all Targets :-
    /*
    Updates the targets position based on the current velocity.
     */
    void updatePosition() {

		if (type != TargetType.OSCILLATOR) {
			alpha = (float)Helpers.resolveAngle(alpha + velocity);
		}
	}
    /*
    Each concrete subclass must implement this method - 
    */
    abstract void doTargetSpecificUpdate();
    
    void updateTarget
			(boolean inContactWithBall,
			 TargetBehaviour desiredBehaviour,
			 double angleToTrack,
			 double velocityOfTarget,
			 Swingball swingball) {
    	
    	// Call superclass method to check the effect of a collision on this target.
    	processCollisionIfAny(inContactWithBall);

		// Set the swingballWithinRange flag.
		swingballWithinRange = isSwingballWithinRange(swingball, swingballRange);

    	// Set behaviour as appropriate :

		if (behaviour != ENTERING && behaviour != EXITING) {
			if (type != TargetType.DODGER) {
				behaviour = desiredBehaviour;
			}
		}


		switch(behaviour) {
    	
    	case STANDARD : {
    		doTargetSpecificUpdate();
    		break;
    	}
    	
    	case AVOIDING : {

			// All target types can be set to TargetBehaviour.AVOIDING, but only TarDodgers retain control
			// over their own behaviour while avoiding - specifically, they switch in and out of AVOIDING
			// on a timer. For this reason, doTargetSpecificUpdate() must be invoked here for them.
			if (type == TargetType.DODGER) {
				doTargetSpecificUpdate();
			}

			float currentDistSq = getDistanceSqToBall((float)alpha, (float)swingball.xPos, (float)swingball.yPos);
			float nextDistSq;
			float newAlpha, newXPos, newYPos;

			// First, check if the ball is outside the reaction horizon. If it is, and the velocity is greater
			// than MAX_VELOCITY, reduce the velocity by increments, and break.
			if (currentDistSq > reactionDistSq) {
				if (velocity > maxVel) {
					velocity -= avoidingAcceleration * AVOIDING_DECELERATION_TO_ACCELERATION_RATIO;
				}
				if (velocity < - maxVel) {
					velocity += avoidingAcceleration * AVOIDING_DECELERATION_TO_ACCELERATION_RATIO;
				}
				break;
			}

    		// Next, check if the ball will be closer after the next cycle. If not, break.
			newAlpha = alpha + velocity;
			newXPos = (float)(swingball.xPos + Math.cos(swingball.direction) * swingball.velocity);
			newYPos = (float)(swingball.yPos + Math.sin(swingball.direction) * swingball.velocity);
			nextDistSq = getDistanceSqToBall(newAlpha, newXPos, newYPos);
			if (nextDistSq > currentDistSq) {
				//break;
			}

			// Finally, check if the target would be further away from the ball on the next cycle
			// if it was accelerated clockwise (+) or anticlockwise (-).
			float distWithClockwiseAcc, distWithAntiClockwiseAcc;
			newAlpha = alpha + velocity + avoidingAcceleration;
			distWithClockwiseAcc = getDistanceSqToBall(newAlpha, newXPos, newYPos);
			newAlpha = alpha + velocity - avoidingAcceleration;
			distWithAntiClockwiseAcc = getDistanceSqToBall(newAlpha, newXPos, newYPos);

			if (distWithClockwiseAcc > distWithAntiClockwiseAcc) {
				velocity +=  avoidingAcceleration;
			} else {
				velocity -=  avoidingAcceleration;
			}

			// Limit the velocity to the adjusted maxVel.
			float limit = maxVel * maxVelMultiplier;
			if (velocity > limit) {
				velocity = limit;
			} else if (velocity < -limit) {
				velocity = -limit;
			}

    	}
    	
    	case TRACKING : {
    		//trackToAngle2(angleToTrack, velocityOfTarget);
    		break;
    	}
    	
    	case ENTERING : {
    		boolean enteringComplete = continueEntering();
    		if (enteringComplete) {
    			behaviour = TargetBehaviour.STANDARD;
    		}
    		break;
    	}
    	
    	case EXITING : {
    		boolean exitingComplete = continueExiting();
    		if (exitingComplete) {
    			taggedForDeletion = true;
    		}
    		break;
    	}
    	}
    	
    	// Restrict the velocity to the maximum value, as long as the target is not entering or exiting.
    	if(behaviour != ENTERING && behaviour != EXITING && behaviour != AVOIDING) {
    		if (type != TargetType.DODGER
					&& type != TargetType.TADPOLE
					&& type != TargetType.DECOYS
					&& type != TargetType.OSCILLATOR
					&& type != TargetType.KILLER) {
				limitVelocityToMaxVel();
			}
    	}
    	
    	// Calculate the next position of the target.
    	updatePosition();
    }
    
    abstract void configureTargetToCurrentLevel(int level);
    
    void processCollisionIfAny(boolean inContactWithBall) {
    	
    	// Check to see if the target continues to be in contact, in order to prevent
        // the effects of collisions being applied twice.
        boolean stillInContact = wasTouchingBall && inContactWithBall;

    	
    	// If there is no collision, set wasTouchingBall to false to inform the next updateDifficultyDependents of this,
        // updatePosition and return.
        if (!inContactWithBall) {
            wasTouchingBall = false;
            return;
        }

        // If a collision is still in process since the last updateDifficultyDependents, updatePosition and return.
        if (stillInContact) {
            return;
        }

        // Otherwise, calculate the effect of the collision on the targets shieldHealth, if vulnerable, and flag the
        // beginning of the collision for the targetRenderer.
        if (isShielded) {
        	shieldHealth = shieldHealth - shieldHealthIncrement;
			if (shieldHealth <= 0 && type != TargetType.BLOCKER) {
				taggedForDeletion = true;
			}
        } else {  // target is unshielded and should be marked for deletion.
			if (type != TargetType.BLOCKER && !invulnerable) {
				taggedForDeletion = true;
			}
		}
        checkHealth();
        wasTouchingBall = true;             // Inform next updateDifficultyDependents about this collision.
        return;
    }
    
    boolean continueEntering() {
    	// Check if the target is close to -PI/2. If so, change behaviour to STANDARD.
    	double minAngle = -(PI / 16 * 9);
    	double maxAngle = -(PI / 16 * 7);
    	if (alpha >= minAngle && alpha <= maxAngle) {
    		return true;
    	}
    	return false;
    }
    
    boolean continueExiting() {
    	
    	// Accelerate in the direction of PI/2.
    	double angularDistToExitPoint = Helpers.resolveAngle((PI / 2) - alpha);
    	int directionToExitPoint = (int) Math.signum(angularDistToExitPoint);
    	velocity += acceleration * 2 * directionToExitPoint;
    	// Check if the target is close to -PI/2. If so, change behaviour to STANDARD.
    	double minAngle = PI / 16 * 7;
    	double maxAngle = PI / 16 * 9;
    	if (alpha >= minAngle && alpha <= maxAngle) {
    		return true;
    	}
    	return false;
    }
    
    void checkHealth() {
        if (shieldHealth <= 0) {
            isShielded = false;
        }
    }
    
    void limitVelocityToMaxVel() {
        if (velocity > maxVel) {
            velocity = maxVel;
        }
        if (velocity < - maxVel) {
            velocity = -maxVel;
        }
	}
    public int getOrbitIndex() {
        return orbitIndex;
    }
    
    double setDistFromOrigin() {
    	double outermostOrbit = (IntRepConsts.DIAMETER / 2)
    			- IntRepConsts.OUTER_CIRCLE_THICKNESS
    			- (IntRepConsts.TARGET_THICKNESS + IntRepConsts.GAP_BETWEEN_ORBITS) / 2;
    	return outermostOrbit - (orbitIndex * (targetThickness + IntRepConsts.GAP_BETWEEN_ORBITS));
    	
    }


    // Method takes an angle as an argument (normally a difference between 2 angles) and reduces it if necessary 
    // to below PI, thereby in effect returning the shorter distance around a circle.
    public double reduceBelowPI (double angle) {
    	if (angle < -PI) {
    		angle = angle + PI; 
    	}
    	if (angle > PI) {
    		angle = angle - PI;
    	}
    	return angle;
    }
    
    // Method returns a random angle which falls outside the area obscured by the bounce platform.
    public double getNewVisiblePosition () {
    	
        double angle = -PI + (Math.random() * PI * 2);
    	
    	return angle;
    	
    	
    	
    }

    // Methods called by TargetManager when drawing internal collision bitmap.
    public static float getStartAngle(float alpha, TargetSize size) {
        return (float)(alpha - (size.getAngularSize() / 2));
    }

    public static float getSweepAngle(TargetSize size) {
        return (float)(size.getAngularSize());
    }

	public static float getOpaqueMaskStartAngle(float alpha, TargetSize size) {
		return (float)(alpha - (size.getAngularSize() * opaqueMaskRatio * 0.5f));
	}

	public static float getOpaqueMaskSweepAngle(TargetSize size) {
		return (float)(size.getAngularSize() * opaqueMaskRatio);
	}

    public static int getAndroidColor(TargetType type) {
        int androidColor = (255 << 24) +
                (type.getTargetColor().getRedComponent() << 16) +
                (type.getTargetColor().getGreenComponent() << 8) +
                (type.getTargetColor().getBlueComponent());
        return androidColor;
    }

	float getDistanceSqToBall(
			float targetAngle,
			float xPos,
			float yPos) {

		// Get an x and y position for the target (center of).
		float radius = (float)setDistFromOrigin();
		float tarX = (float)(xCenterOfRotation + radius * Math.cos(targetAngle));
		float tarY = (float)(yCenterOfRotation + radius * Math.sin(targetAngle));
		float xDist = tarX - xPos;
		float yDist = tarY - yPos;

		return (xDist * xDist) + (yDist * yDist);
	}

	boolean isSwingballWithinRange(Swingball ball, float range) {
		boolean isWithinRange = false;
		float xPt, yPt;
		float rangeSq = range * range;
		float distSq;

		// If range == 0 (the case if a particular TargetType does not assign one) return false;
		if (range == 0) {
			return false;
		}

		// Check three points - the center and each end of the target. These can be done in turn.
		// 1st check the center :
		xPt = CENTER_OF_GAME_AREA + (float) (distFromOrigin * Math.cos(alpha));
		yPt = CENTER_OF_GAME_AREA + (float) (distFromOrigin * Math.sin(alpha));
		distSq = (float)((ball.xPos - xPt) * (ball.xPos - xPt) + (ball.yPos - yPt) * (ball.yPos - yPt));
		if (distSq < rangeSq) return true;

		// Now check the left-hand side.
		xPt = CENTER_OF_GAME_AREA + (float)(distFromOrigin * Math.cos(alpha - size.getAngularSize() / 2));
		yPt = CENTER_OF_GAME_AREA + (float)(distFromOrigin * Math.sin(alpha - size.getAngularSize() / 2));
		distSq = (float)((ball.xPos - xPt) * (ball.xPos - xPt) + (ball.yPos - yPt) * (ball.yPos - yPt));
		if (distSq < rangeSq) return true;

		// Finally check the right-hand side.
		xPt = CENTER_OF_GAME_AREA + (float)(distFromOrigin * Math.cos(alpha + size.getAngularSize() / 2));
		yPt = CENTER_OF_GAME_AREA + (float)(distFromOrigin * Math.sin(alpha + size.getAngularSize() / 2));
		distSq = (float)((ball.xPos - xPt) * (ball.xPos - xPt) + (ball.yPos - yPt) * (ball.yPos - yPt));
		if (distSq < rangeSq) return true;



		return isWithinRange;
	}

    public void setGhosting(boolean ghosting) {
        if (ghosting) {
            isGhost = true;
            opacity = IntRepConsts.GHOSTING_OPACITY;
        } else {
            isGhost = false;
            opacity = 0;
        }
    }


}
