package com.intricatech.slingball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.Log;

import com.intricatech.slingball.TargetManager.LevelPhase;

import static com.intricatech.slingball.MessageBoxManager.MessageType.AUTOPILOT;
import static com.intricatech.slingball.MessageBoxManager.MessageType.AUTOPILOT_TAKE;
import static com.intricatech.slingball.MessageBoxManager.MessageType.MISSES;
import static com.intricatech.slingball.MessageBoxManager.MessageType.SHIELD;
import static com.intricatech.slingball.MessageBoxManager.MessageType.SUDDEN_DEATH;
import static java.lang.Math.PI;

//import java.awt.image.BufferedImage;

/**
 * Created by Bolgbolg on 23/04/2015.
 */
public class Swingball implements LevelChangeObserver,
                                    DifficultyLevelObserver{
	private String TAG = "Swingball";

	LevelManager levelManager;
    DifficultyLevelDirector difficultyLevelDirector;
    PhysicsAndBGRenderer physicsAndBGRenderer;
    Vibrator vibrator;
    SoundManager soundManager;
    RewardsManager rewardsManager;
    MessageBoxManager messBoxManager;

    boolean playerFingerDown;
	
    double xPos, yPos, velocity, direction;
    float autopilotVel;
    double energy;
    double energyLostInWallCollision;
    double energyLostInJitteringWallCollision;
    long timeOfLastWallCollision;
    double energyLostInOrbit;
    boolean outOfEnergy;
    boolean isVibrationOn;
    static final float CENTER = IntRepConsts.DIAMETER / 2;
    static final float DIST_TO_OUTER_ORBIT = CENTER - IntRepConsts.OUTER_CIRCLE_THICKNESS;
    static final float DIST_TO_INNER_ORBIT = (CENTER - IntRepConsts.OUTER_CIRCLE_THICKNESS
            - IntRepConsts.MAX_NUMBER_OF_ORBITS * (IntRepConsts.TARGET_THICKNESS + IntRepConsts.GAP_BETWEEN_ORBITS));
    static final float OUTER_ORBIT_ANGULAR_OFFSET =
            (float) Math.acos(IntRepConsts.PREFERRED_ORBIT_RADIUS / DIST_TO_OUTER_ORBIT);
    static final float INNER_ORBIT_ANGULAR_OFFSET =
            (float) Math.acos(IntRepConsts.PREFERRED_ORBIT_RADIUS / DIST_TO_INNER_ORBIT);

    boolean rewardArcOn;
    static final long LAG_BETWEEN_HEAVY_ENERGY_LOSSES = IntRepConsts.LAG_BETWEEN_HEAVY_ENERGY_LOSSES;
    
    double collisionDirection;
    int numberOfCyclesInContact;
    double maxVel;
    int radius;
    double distToInsideOfCircleLessBallRadius;
    double distanceFromOrigin;
    double directionToCenter, previousDirectionToCenter;
    float projectedOuterOrbitCollisionAng, projectedInnerOrbitCollisionAng;
    float projectedOuterX, projectedOuterY, projectedInnerX, projectedInnerY;

    int hitsSinceLastOrbit;
    boolean shouldResetConsecutiveHitCounter;
    int rotationCounter;                // Counts the number of cycles until a full rotation of the ball.
    boolean ballCollidedWithBlocker;

    boolean wallIsLit;
    boolean ballCollidedWithWall;
    boolean ballCollidedWithRewarder;
    boolean ballHitSomethingLastCycle;
    float wallLitCountdown;
    static final float wallLitCountdownIncrement = 1.0f / IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
    static final float wallLitCountdownInitialValue = IntRepConsts.WALL_IS_LIT_COUNTDOWN_INITIAL_VALUE;
    float wallIsLitPosition;


    boolean multipleTouches;
    boolean readyForReversing;
    int readyForReversingCounter;
    static final int cyclesBeforeReadyForReversing = IntRepConsts.CYCLES_BEFORE_READY_FOR_REVERSING;
    int numberOfCyclesInOrbit;
    static final int CYCLES_FOR_STABLE_ORBIT = 30;

    final double GRAVCONST;
    final double POWERCONSTANT;

    Destination ballDest;
    boolean orbiting, primed;
    boolean ballTouchingEdge;
    float xCenterOfRotation, yCenterOfRotation;
    double turnRate, orbitDist;
    boolean orbitingClockwise;

    static final int NUMBER_OF_TEST_POINTS = IntRepConsts.NUMBER_OF_TEST_POINTS;
    static final double ARC_SIZE = (PI * 2) / NUMBER_OF_TEST_POINTS;
    boolean collisionDetected,
            ballTrapped,
            collidedButRebounding;
    TestPoint[] points; 						// the array for storing the position of each of the points, and the result at each point.
    TestPoint[] fixedPoints;					// array for storing the invariant position of each point around a ball at (0,0).
    int numberOfPointsTriggered,
            previousNumberOfPointsTriggered;

    PositionList positionList;
    int positionRecordCounter;
    Autopilot autopilot;
    Shield shield;
    SuddenDeath suddenDeath;

    @Override
    public void registerWithDifficultyLevelDirector() {
        difficultyLevelDirector.register(this);
    }

    @Override
    public void unregisterWithDifficultyLevelDirector() {
        difficultyLevelDirector.unregister(this);
    }

    @Override
    public void updateDifficultyDependents(DifficultyLevel level) {
        switch (level) {
            case EASY: {
                maxVel = IntRepConsts.MAXIMUM_BALL_VELOCITY;// was 1.15f
                velocity = maxVel;
                autopilotVel = (float)velocity * IntRepConsts.AUTOPILOT_VELOCITY_RATIO;
                turnRate = IntRepConsts.calculateTurnRate(maxVel);
                break;
            }
            case NORMAL: {
                maxVel = IntRepConsts.MAXIMUM_BALL_VELOCITY * 1.15f;
                velocity = maxVel;
                autopilotVel = (float)velocity * IntRepConsts.AUTOPILOT_VELOCITY_RATIO;
                turnRate = IntRepConsts.calculateTurnRate(maxVel);
                break;
            }
            case HARD: {
                maxVel = IntRepConsts.MAXIMUM_BALL_VELOCITY * 1.3;
                velocity = maxVel;
                autopilotVel = (float)velocity * IntRepConsts.AUTOPILOT_VELOCITY_RATIO;
                turnRate = IntRepConsts.calculateTurnRate(maxVel);
                break;
            }
            default: Log.d(TAG, "updateDD fallthrough in switch ");
        }
    }

    public enum CollisionPhase {
    	FIRST_COLLISION_REGISTERED,
    	NOT_COLLIDING
    }
    CollisionPhase collisionPhase;

    public Swingball(int radius,
                     double xPos,
                     double yPos,
                     LevelManager levelManager,
                     DifficultyLevelDirector difficultyLevelDirector,
                     Vibrator vibrator,
                     boolean isVibrationOn,
                     SoundManager soundManager,
                     MessageBoxManager messageBoxManager,
                     PhysicsAndBGRenderer physicsAndBGRenderer) {

    	// Register with levelManager to be updated as the level changes. So with difficulty.
    	this.levelManager = levelManager;
    	levelManager.register(this);
        this.difficultyLevelDirector = difficultyLevelDirector;
        registerWithDifficultyLevelDirector();
        this.vibrator = vibrator;
        this.isVibrationOn = isVibrationOn;
        this.soundManager = soundManager;
        this.messBoxManager = messageBoxManager;
        this.physicsAndBGRenderer = physicsAndBGRenderer;

        playerFingerDown = false;
        autopilot = Autopilot.OFF;
        autopilot.gameCyclesRemaining = 30000;
        autopilot.ballHeld = false;
        autopilot.initializeConstantArrays(this);
        shield = Shield.OFF;
        suddenDeath = SuddenDeath.OFF;
        ballHitSomethingLastCycle = false;
    	
    	this.radius = radius;
        autopilot.calculateAngularWidths(radius);
        this.xPos = xPos;
        this.yPos = yPos;
        velocity = IntRepConsts.MAXIMUM_BALL_VELOCITY;
        direction = -PI / 2;
        energy = IntRepConsts.INITIAL_ENERGY;
        energyLostInWallCollision = IntRepConsts.ENERGY_LOST_IN_WALL_COLLISION;
        energyLostInJitteringWallCollision = energyLostInWallCollision / 10;
        energyLostInOrbit = IntRepConsts.ENERGY_LOST_IN_ORBIT;
        outOfEnergy = false;
        hitsSinceLastOrbit = 0;
        rotationCounter = 0;
        shouldResetConsecutiveHitCounter = false;
        ballCollidedWithBlocker = false;
        ballTouchingEdge = false;

        GRAVCONST = IntRepConsts.GRAVCONST;
        POWERCONSTANT = IntRepConsts.POWERCONSTANT;

        orbiting = false;
        primed = false;
        numberOfCyclesInOrbit = 0;
        xCenterOfRotation = IntRepConsts.X_CENTER_OF_ROTATION;
        yCenterOfRotation = IntRepConsts.Y_CENTER_OF_ROTATION;
        distToInsideOfCircleLessBallRadius = IntRepConsts.OUTER_CIRCLE_RADIUS
        						- (IntRepConsts.OUTER_CIRCLE_THICKNESS / 2);
        distanceFromOrigin = 0;
        ballDest = new Destination(xCenterOfRotation, yCenterOfRotation);
        orbitDist = IntRepConsts.ORBIT_DISTANCE;
        ballCollidedWithWall = false;
        collisionPhase = CollisionPhase.NOT_COLLIDING;

        wallIsLit = false;
        wallIsLitPosition = 0;
        wallLitCountdown = 0;

        numberOfCyclesInContact = 0;
        points = new TestPoint[NUMBER_OF_TEST_POINTS];
        fixedPoints = new TestPoint[NUMBER_OF_TEST_POINTS];

        for (int n = 0; n < NUMBER_OF_TEST_POINTS; n++) {
            points[n] = new TestPoint();
            fixedPoints[n] = new TestPoint();
            
            // Initialise the fixedPoints array to contain a set of points circling (0,0),
            // each a distance equal to the radius of the ball from the origin.
            fixedPoints[n].xCoor = radius * (Math.cos(ARC_SIZE * n));
            fixedPoints[n].yCoor = radius * (Math.sin(ARC_SIZE * n));
        }
        numberOfPointsTriggered = 0;

        positionList = new PositionList();
        positionRecordCounter = 0;

        // Finally, update the difficultyLevelDependents.
        difficultyLevelDirector.updateObservers();

    }

    public int updatePhysics(
            Object internalCollisionMap,
            Object fixturesMap,
            boolean fingerDown,
            boolean fingerDownOverride,
            TargetManager targetManager,
            RewardsManager rewardsManager,
            int totalNumberOfMisses,
            boolean controlButton1Pressed,
            boolean controlButton2Pressed
    ){
        this.rewardsManager = rewardsManager;
        int returnValue = totalNumberOfMisses;

        // Set the orbitingClockwise flag.
        orbitingClockwise = (Math.signum(Helpers.resolveAngle(directionToCenter - direction)) < 0);

        // Calculate the angular position the ball, if released now, would impact the outer orbit at, and
        // the inner orbit, and then calculate the points that correspond to each position.
        projectedOuterOrbitCollisionAng =
                (float) Helpers.resolveAngle(directionToCenter + PI)
                + OUTER_ORBIT_ANGULAR_OFFSET * (orbitingClockwise ? -1 : 1);
        projectedInnerOrbitCollisionAng =
                (float) Helpers.resolveAngle(directionToCenter + PI)
                + INNER_ORBIT_ANGULAR_OFFSET * (orbitingClockwise ? -1 : 1);
        projectedOuterX = CENTER + DIST_TO_OUTER_ORBIT * (float) Math.cos(projectedOuterOrbitCollisionAng);
        projectedOuterY = CENTER + DIST_TO_OUTER_ORBIT * (float) Math.sin(projectedOuterOrbitCollisionAng);
        projectedInnerX = CENTER + DIST_TO_INNER_ORBIT * (float) Math.cos(projectedInnerOrbitCollisionAng);
        projectedInnerY = CENTER + DIST_TO_INNER_ORBIT * (float) Math.sin(projectedInnerOrbitCollisionAng);

        // Update the turnRate, just in case something I'll do later changes the velocity.
        //turnRate = IntRepConsts.calculateTurnRate(velocity);


        if (autopilot == Autopilot.ON) {
            autopilot.update(targetManager, this);
            autopilot.gameCyclesRemaining--;
            autopilot.secondsRemaining = autopilot.gameCyclesRemaining / (60 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE);
            messBoxManager.updateAutopilot(autopilot.secondsRemaining);
            if (autopilot.gameCyclesRemaining <= 0) {
                turnOffAutopilot(true);
            }
        }

        if (shield == Shield.ON) {
            shield.gameCyclesRemaining--;
            shield.secondsRemaining = shield.gameCyclesRemaining / (60 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE);
            messBoxManager.updateShield(shield.secondsRemaining);
            if (shield.gameCyclesRemaining <= 0) {
                turnOffShield();
            }
        }

        if (suddenDeath == SuddenDeath.ON) {
            suddenDeath.gameCyclesRemainging--;
            suddenDeath.secondsRemaining = suddenDeath.gameCyclesRemainging / (60 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE);
            messBoxManager.updateSuddenDeath(suddenDeath.secondsRemaining);
            if (suddenDeath.gameCyclesRemainging <= 0 ) {
                turnOffSuddenDeath();
            }
        }

        /**
         * If autopilot is on, then the autopilot should decide whether or not the ball is attracted.
         * If autopilot if off, then the player's touch should decide.
         */
        if (autopilot == Autopilot.ON) {
            playerFingerDown = autopilot.ballHeld;
        } else {
            if (controlButton1Pressed || controlButton2Pressed || fingerDownOverride)
            playerFingerDown = fingerDown;
        }
        ballHitSomethingLastCycle = false;

        // Start off with the shouldResetConsecutiveHitCounter set to false.
        shouldResetConsecutiveHitCounter = false;
        hitsSinceLastOrbit += targetManager.hitsOccuredThisCycle;

    	// If the player has the finger on the screen, calculate the motion
    	// of the ball and reduce the energy appropriately.
    	if (playerFingerDown) {
            setRotationalVelocity();
            if (targetManager.levelPhase == LevelPhase.LEVEL_IN_PROGRESS) {
            	energy -= energyLostInOrbit;
            }
    	} else {
            rotationCounter = 0;
            numberOfCyclesInOrbit = 0;
        }

    	// Add the energy gained from the last targetManager.updateDifficultyDependents().
    	energy += targetManager.energyChangeThisUpdate;
    	if (energy > 100) {
    		energy = 100;
    	}
        if (energy < 0) {
            energy = 0;
        }
        
        // Calculate the distance from the center of rotation.
        distanceFromOrigin = Math.hypot(xPos - xCenterOfRotation, yPos - yCenterOfRotation);
        
        // Next, check for a collision with the circle boundary.
        long timeForWallCollisionCheck = checkCollisionWithWall();
        
        // Finally, check for a collision with each target in turn.
        long timeForTargetCollisionCheck = checkCollisionWithTargets(targetManager, internalCollisionMap);
        
        setNewPosition();
        recordPosition();
        setDirectionToCenter();

        if (ballCollidedWithBlocker) {
            shouldResetConsecutiveHitCounter = true;
        }
        if (ballCollidedWithWall) {
            shouldResetConsecutiveHitCounter = true;
            physicsAndBGRenderer.thisRunOfHits = 0;
            ballCollidedWithWall = false;
            if (shield == Shield.OFF) {
                returnValue++;
                MessageBoxManager.MessageBox box = messBoxManager.messageMap.get(MISSES);
                messBoxManager.updateMisses(returnValue);
                box.on = true;
                box.fading = false;
                box.constantCountdown = box.fadeType.initCountdown;
                box.brightness = 255;
            }
            wallIsLit = true;
            wallLitCountdown = wallLitCountdownInitialValue;
            wallIsLitPosition = (float)Helpers.resolveAngle(directionToCenter + PI);

        }
        if (wallIsLit) {
            if (wallLitCountdown <= 0) {
                wallIsLit = false;
            } else {
                wallLitCountdown-= wallLitCountdownIncrement;
            }
        }
        if (energy <= 0) {
        	outOfEnergy = true;
        }
        return returnValue;
    }

    private long checkCollisionWithWall() {
        
    	// get the time at start of method.
    	long startTime = System.nanoTime();
    	
    	// Calculate the distance from the center to the outside of the ball.
        double xDist = xPos - xCenterOfRotation;
        double yDist = yPos - yCenterOfRotation;
        double distToBallCenterSQ = (xDist * xDist) + (yDist * yDist);
        distToInsideOfCircleLessBallRadius = IntRepConsts.OUTER_CIRCLE_RADIUS
        						- (IntRepConsts.OUTER_CIRCLE_THICKNESS / 2) - radius;

        // Compare the distances (squared),and set the collisionOccurring flag as appropriate.
        double distAllowedSQ = distToInsideOfCircleLessBallRadius * distToInsideOfCircleLessBallRadius;
        boolean collisionOccurring = !(distToBallCenterSQ < distAllowedSQ);
        double difference = Helpers.resolveAngle(directionToCenter - direction);

        // HACK - If the ball is beyond where it should be, return it to its outer limit at the
        // same angle to the centre.
        if (distAllowedSQ < distToBallCenterSQ) {

            double alpha = Math.atan2(yDist, xDist);
            xPos = distToInsideOfCircleLessBallRadius * Math.cos(alpha) + xCenterOfRotation;
            yPos = distToInsideOfCircleLessBallRadius * Math.sin(alpha) + yCenterOfRotation;
        }

        switch (collisionPhase) {
        
        case NOT_COLLIDING : {
        	if (collisionOccurring) {
        		collisionPhase = CollisionPhase.FIRST_COLLISION_REGISTERED;
                ballCollidedWithWall = false;
                ballHitSomethingLastCycle = true;

        		// Calculate the new direction.
                direction = Helpers.resolveAngle(direction + (2 * (difference - (PI / 2))));
        	}
        	return System.nanoTime() - startTime;
        }
        
        case FIRST_COLLISION_REGISTERED : {
        	if(!collisionOccurring) {
        		collisionPhase = CollisionPhase.NOT_COLLIDING;
                long currentTime = System.nanoTime();
                float realDir = (float)Helpers.resolveAngle(directionToCenter + PI);
                /*boolean ballHittingActiveReward =
                        realDir > PI / 2 - IntRepConsts.REWARD_TAG_ANGULAR_SIZE / 2
                        && realDir < PI / 2 + IntRepConsts.REWARD_TAG_ANGULAR_SIZE / 2
                        && rewardArcOn;*/
                boolean ballHittingActiveReward = false; // This is no longer needed.
                if (suddenDeath == SuddenDeath.ON) {
                    if (energy < 20/* || suddenDeath.collisionsOccurred > 0*/) {
                        energy = 0;
                        turnOffSuddenDeath();
                    }
                    else {
                        energy -= 50;
                        if (energy < 20) {
                            energy = 20;
                        }
                        suddenDeath.collisionsOccurred++;
                    }
                    soundManager.playCircleCollision();
                    if (isVibrationOn) {
                        vibrator.vibrate(100);
                    }

                }
                if (suddenDeath == SuddenDeath.OFF) {
                    if (currentTime - timeOfLastWallCollision > LAG_BETWEEN_HEAVY_ENERGY_LOSSES) {
                        if (!ballHittingActiveReward && shield == Shield.OFF) {
                            energy -= energyLostInWallCollision;
                            soundManager.playCircleCollision();
                            if (isVibrationOn) vibrator.vibrate(100);
                        }
                        timeOfLastWallCollision = currentTime;
                    } else {
                        if (!ballHittingActiveReward  && shield == Shield.OFF) {
                            energy -= energyLostInJitteringWallCollision;
                            soundManager.playCircleCollision();
                            if (isVibrationOn) vibrator.vibrate(100);
                        }
                    }
                    if (shield == Shield.ON) {
                        soundManager.playTadpoleShieldedImpactSound();
                    }
                }


                ballCollidedWithWall = true;
                if (shield == Shield.OFF && suddenDeath != SuddenDeath.ON) {
                    physicsAndBGRenderer.circleHighlighter.fire();
                }

            }
            return 0;
        }
        
        default : 
        	return 0;
        }



        
    }
    
    private long checkCollisionWithTargets(TargetManager targetManager, Object internalCollisionMap) {
    	// Get the time at start of method.
    	long startTime = System.nanoTime();
    	
    	// Calculate the current ball angle.
    	double currentBallAngle = Math.atan2(yPos - yCenterOfRotation, xPos - xCenterOfRotation);
    	
    	// Iterate through the targets, testing each one in turn.
    	AbstractTarget[] targets = targetManager.getOrbits();
    	for (AbstractTarget target : targets) {
    		if (target != null && !target.isGhost) {
    			/*
    			// First early escape. If ball is inside the targets distance from the origin, then skip this collision test.
    			if ((distanceFromOrigin + radius) < (target.distFromOrigin + (target.targetThickness / 2))) {
    				continue;
    			}
    			
    			// Second early escape. If the ball is more than PI/2 removed from the target, then skip this collision test.
    			// This is crude (not an accurate calculation of the possibility of collision) but fast. directionToCenter must be reversed
    			// by adding PI to it.
    			double diff = Helpers.resolveAngle(target.opacity - (Helpers.resolveAngle(directionToCenter + PI)));
    			if (Math.abs(diff) > (PI / 2)) {
    				continue;
    			}
    			*/
    			double referenceAngle = InternalCollisionMap.getReferenceAngle(target.size);

    			int blueFactor = InternalCollisionMap.getBlueFactorCode(target.size, target.orbitIndex);

    			// Rotate the ball to the correct angle for testing against this target. Rotated position stored in temporary fields
    			double rotation = Helpers.resolveAngle(referenceAngle - target.alpha);
    			double newBallAngle = Helpers.resolveAngle(currentBallAngle + rotation);
    			double testXPos = xCenterOfRotation + (Math.cos(newBallAngle) * distanceFromOrigin);
    			double testYPos = yCenterOfRotation + (Math.sin(newBallAngle) * distanceFromOrigin);

                ballTouchingEdge = false;
    			boolean doot = checkCollisionWithWorld(internalCollisionMap, testXPos, testYPos, blueFactor, rotation);
    			targetManager.ballIsTouching[target.orbitIndex] = doot;
                if (doot) {
                    ballHitSomethingLastCycle = true;
                    if (target.type == TargetType.REWARDER) {
                        TarRewarder tr = (TarRewarder) target;
                        if (tr.bonusType != TarRewarder.BonusType.EXTRA_HEART) {
                            ballCollidedWithRewarder = true;
                        } else {
                            ballCollidedWithRewarder = false;
                        }
                    }
                    if (target.type == TargetType.BLOCKER) {
                        soundManager.playMetallicCollision();
                    }
                    if (target.type == TargetType.TADPOLE
                            && target.shieldHealth > target.shieldHealthIncrement) {
                        soundManager.playTadpoleShieldedImpactSound();
                    }
                    if (target.type == TargetType.KILLER && target.invulnerable) {
                        if (shield == Shield.OFF) {
                            energy -= energyLostInWallCollision;
                            soundManager.playCircleCollision();
                            if (isVibrationOn) {
                                vibrator.vibrate(100);
                            }
                            if (energy < 0) {
                                energy = 0;
                            }
                        } else {
                            soundManager.playTadpoleShieldedImpactSound();
                        }
                    }
                }

                // If the ball is contacting the leading edge of the target, set the flag in targetManager.
                if (ballTouchingEdge) {
                    float ballDir = (float)Helpers.resolveAngle(directionToCenter + PI);
                    boolean ballOnLeft = ballDir < target.alpha
                            || ballDir > target.alpha + (PI);
                    boolean targetTravellingLeft = target.velocity < 0;
                    if ((ballOnLeft && targetTravellingLeft)||(!ballOnLeft && !targetTravellingLeft)) {
                        targetManager.leadingEdgeCollisionDetected[target.orbitIndex] = true;
                    }
    			}
    		}
    	}

        if (ballHitSomethingLastCycle) {
            physicsAndBGRenderer.fingerDownOverride = true;
        }
    	
    	return 0;
    }
    
    public boolean checkCollisionWithWorld (Object collideables, double xBallTestPos, double yBallTestPos,
    										int soughtBlueFactor, double rotation){
    	
        // Initialise local variables and reset ball variables
        if (!collisionDetected) {
            numberOfCyclesInContact = 0;
        }
        previousNumberOfPointsTriggered = numberOfPointsTriggered;
        numberOfPointsTriggered = 0;
        double sumOfXCoors = 0;
        double sumOfYCoors = 0;
        int detectedBlueParamter = 0;
        int detectedRedParamter = 0;
        int rgb = 0;
        collisionDetected = false;
        ballTrapped = false;
        collidedButRebounding = false;
        ballTouchingEdge = false;

        // Fill the testPoint array.
        for (int n = 0; n < NUMBER_OF_TEST_POINTS; n++) {
            points[n].xCoor = xBallTestPos + fixedPoints[n].xCoor;
            points[n].yCoor = yBallTestPos + fixedPoints[n].yCoor;
            int testX = (int)(points[n].xCoor + 0.5);
            int testY = (int)(points[n].yCoor + 0.5);
            rgb = Helpers.getPixel4Byte(collideables, testX, testY);
            detectedBlueParamter = rgb & 0xff;
            detectedRedParamter = rgb >> 16 & 0xff;

            if (detectedBlueParamter == soughtBlueFactor) {
                points[n].empty = false;
                collisionDetected = true;
                numberOfPointsTriggered++;
                sumOfXCoors = sumOfXCoors + points[n].xCoor;
                sumOfYCoors = sumOfYCoors + points[n].yCoor;
                if (detectedRedParamter == 255) {
                    ballTouchingEdge = true;
                }
            } else {
                points[n].empty = true;
            }


        }
        if (collisionDetected) {
            numberOfCyclesInContact++;
        }
        // If no collision has occurred, return false.
        if(!collisionDetected) {
            return false;
        }
        // Collision has occurred - calculate collisionDirection.
        double averageXCoor = sumOfXCoors / numberOfPointsTriggered;
        double averageYCoor = sumOfYCoors / numberOfPointsTriggered;
        collisionDirection = Math.atan2(averageYCoor - yBallTestPos, averageXCoor - xBallTestPos);
        
        // Apply the rotation to the balls direction.
        direction = Helpers.resolveAngle(direction + rotation);
        
        // Check to ensure ball is moving in direction of collisionDirection(+- PI/2). If not,
        // return;
        double difference = Helpers.resolveAngle(collisionDirection - direction);
        if (Math.abs(difference) > (PI / 2) ) {
            collidedButRebounding = true;
            
            // Undo the rotation.
            direction = direction - rotation;
            return true; // Don't change the ball direction.
        }


        // reflect ballDirection through collisionDirection.
        direction = Helpers.resolveAngle(direction + (2 * difference));
        // and then reverse ballDirection.
        direction = Helpers.resolveAngle(direction + PI);
        // and undo the rotation.
        direction = Helpers.resolveAngle(direction - rotation);

        // Test for ball trapped between two surfaces or fully embedded.
        if (((averageXCoor - xPos) < 0.5) && ((averageYCoor - yPos) < 0.5)) {
            ballTrapped = true;

        }

        // Finally, return.
        return true;
    }
    
    void setDirectionToCenter() {
        previousDirectionToCenter = directionToCenter;
    	directionToCenter = Math.atan2(yCenterOfRotation - yPos, xCenterOfRotation - xPos);
    }

    boolean isRotatingClockwise() {
        if (Helpers.resolveAngle(directionToCenter - previousDirectionToCenter) >= 0) {
            return true;
        } else return false;
    }

    public void setNewPosition() {
        xPos = xPos + (velocity * Math.cos(direction));
        yPos = yPos + (velocity * Math.sin(direction));
    }

    void recordPosition() {
        if (positionRecordCounter++ > IntRepConsts.GAP_BETWEEN_POSITION_RECORDINGS) {
            positionRecordCounter -= IntRepConsts.GAP_BETWEEN_POSITION_RECORDINGS;
            positionList.addCurrentPosiiton((float) xPos, (float) yPos);
        }
    }

    public void setRotationalVelocity(){
        ballDest.xDest = xCenterOfRotation;
        ballDest.yDest = yCenterOfRotation;
        //double dist = Helpers.getDistToDest(xPos, yPos, ballDest);
        double dist = Math.hypot(xPos - ballDest.xDest, yPos - ballDest.yDest);
        double xAccel, yAccel, xVel, yVel;
        double xDist = ballDest.xDest - xPos;
        double yDist = ballDest.yDest - yPos;
        xVel = velocity * Math.cos(direction);
        yVel = velocity * Math.sin(direction);

        // If the ball is in stable orbit range, update the counter.
        if (orbiting
                && distanceFromOrigin > IntRepConsts.PREFERRED_ORBIT_RADIUS * 0.9f
                && distanceFromOrigin < IntRepConsts.PREFERRED_ORBIT_RADIUS * 1.1f) {
            numberOfCyclesInOrbit++;
        }

        if (multipleTouches && readyForReversing ) {
            direction = Helpers.resolveAngle(direction + PI);
            readyForReversingCounter = cyclesBeforeReadyForReversing;
            readyForReversing = false;
        }
        if (readyForReversingCounter < 0) {
            readyForReversing = true;
        } else {
            readyForReversingCounter--;
        }

        // If outside the orbitDist, ball is subject to gravitational attraction towards the pointer position
        if (dist > orbitDist){
            orbiting = false;
            numberOfCyclesInOrbit = 0;
            xAccel = ((GRAVCONST * xDist) / Math.pow(dist, POWERCONSTANT));
            yAccel = ((GRAVCONST * yDist) / Math.pow(dist, POWERCONSTANT));
            xVel = xVel + xAccel;
            yVel = yVel + yAccel;
            velocity = Math.hypot(xVel, yVel);
            if (velocity > maxVel) {velocity = maxVel;}
            direction = Math.atan2(yVel, xVel);
            return;
        } else {
            // Ball is inside the orbitDist - turns toward the pointer position in increments of
            // (difference in angle) / turnRate
            orbiting = true;
            rotationCounter++;
            if (rotationCounter > turnRate * 5) shouldResetConsecutiveHitCounter = true;
            double alpha = Math.atan2(yDist, xDist);
            double beta = direction;
            double turn = (alpha - beta);
            turn = Helpers.resolveAngle(turn);
            turn = turn / (turnRate);

            direction = direction + turn;
            direction = Helpers.resolveAngle(direction);
        }
    }

    // Collision detection fields
    class TestPoint {
        double xCoor, yCoor;
        boolean empty;
        public TestPoint() {
            xCoor = 0;
            yCoor = 0;
            empty = true;
        }
    } //  a class to enable straightforward storage of data in an array (for execution speed)

	@Override
	public void updateConstants(int level) {
		
		// The energies lost in various collision types increase to a maximum of five times their original values.
		double levelIncrement = IntRepConsts.LEVEL_INCREMENT_FOR_ENERGY_LOSSES;
		double maxFactor = IntRepConsts.LEVEL_MAX_MULTIPLE_OF_ORIGINAL_ENERGY_LOST;
		energyLostInWallCollision = IntRepConsts.ENERGY_LOST_IN_WALL_COLLISION + (levelIncrement * energyLostInWallCollision);
		if (energyLostInWallCollision > (maxFactor * IntRepConsts.ENERGY_LOST_IN_WALL_COLLISION)) {
			energyLostInWallCollision = maxFactor * IntRepConsts.ENERGY_LOST_IN_WALL_COLLISION;
		}
		
		energyLostInOrbit = IntRepConsts.ENERGY_LOST_IN_ORBIT + (levelIncrement * energyLostInOrbit);
		if (energyLostInOrbit > (maxFactor * IntRepConsts.ENERGY_LOST_IN_ORBIT)) {
			energyLostInOrbit = maxFactor * IntRepConsts.ENERGY_LOST_IN_ORBIT;
		}
	}

    public void setMultipleTouches(boolean multipleTouches) {
        this.multipleTouches = multipleTouches;
    }

    boolean turnOnShield() {
        if (shield == Shield.ON) {
            shield.gameCyclesRemaining = IntRepConsts.SHIELD_CYCLES;
            return true;
        } else {
            shield = Shield.ON;
            shield.gameCyclesRemaining = IntRepConsts.SHIELD_CYCLES;
            messBoxManager.messageMap.get(SHIELD).on = true;
            return false;
        }
    }

    boolean turnOffShield() {
        if (shield == Shield.OFF) {
            return true;
        } else {
            shield = Shield.OFF;
            messBoxManager.messageMap.get(SHIELD).on = false;
            return false;
        }

    }

    boolean turnOnSuddenDeath() {
        if (suddenDeath == SuddenDeath.ON) {
            return true;
        } else {
            suddenDeath = SuddenDeath.ON;
            suddenDeath.collisionsOccurred = 0;
            suddenDeath.gameCyclesRemainging = IntRepConsts.SUDDEN_DEATH_CYCLES;
            messBoxManager.messageMap.get(SUDDEN_DEATH).on = true;
            return false;
        }
    }

    boolean turnOffSuddenDeath() {
        if (suddenDeath == SuddenDeath.OFF) {
            return true;
        } else {
            suddenDeath = SuddenDeath.OFF;
            messBoxManager.messageMap.get(SUDDEN_DEATH).on = false;
            return false;
        }
    }

    enum Shield {
        ON, OFF;

        int gameCyclesRemaining;
        int secondsRemaining;
    }

    enum SuddenDeath {
        ON, OFF;

        int gameCyclesRemainging;
        int secondsRemaining;
        int collisionsOccurred;
    }


    /**
     * Method switches on the Autopilot.
     *
     * @return true if the autopilot was already on, false otherwise.
     */
    boolean turnOnAutopilot() {
        if (autopilot == Autopilot.ON) {
            return true;
        } else {
            autopilot = Autopilot.ON;
            autopilot.ballHeld = true;
            velocity = autopilotVel;
            physicsAndBGRenderer.fingerDownOverride = true;
            turnRate = IntRepConsts.calculateTurnRate(velocity);
            autopilot.initializeConstantArrays(this);
            autopilot.gameCyclesRemaining = IntRepConsts.AUTOPILOT_CYCLES;
            messBoxManager.messageMap.get(AUTOPILOT).on = true;
            return false;
        }
    }

    /**
     * Method switches off the autopilot.
     *
     * @return true if the autopilot was already off, false otherwise.
     */
    boolean turnOffAutopilot(boolean showTakeOverPrompt) {
        if (autopilot == Autopilot.OFF) {
            return true;
        } else {
            autopilot = Autopilot.OFF;
            autopilot.ballHeld = false;
            soundManager.playTakeOverVoice();
            physicsAndBGRenderer.fingerDownOverride = true;
            velocity = maxVel;
            turnRate = IntRepConsts.calculateTurnRate(velocity);
            messBoxManager.messageMap.get(AUTOPILOT).on = false;
            if (showTakeOverPrompt) {
                messBoxManager.messageMap.get(AUTOPILOT_TAKE).on = true;
            }
            return false;
        }
    }

    /**
     * Enum represents a singleton instance of the autopilot and contains all methods necessary to
     * run it.
     */
    enum Autopilot {
        ON, OFF;

        static String TAG = "Swingball.Autopilot";
        boolean ballHeld;
        boolean ballInStableOrbit;
        int currentTargetSelected;
        float signumDirectionDiff;
        static final float STABLE_ORBIT_DISTANCE = (float) IntRepConsts.PREFERRED_ORBIT_RADIUS;

        static float outermostOrbitEdge = (float)(
                IntRepConsts.DIAMETER / 2
                        - IntRepConsts.OUTER_CIRCLE_THICKNESS
                        - IntRepConsts.TARGET_THICKNESS
                        - IntRepConsts.BALL_RADIUS);

        static float gapBetweenOrbits = (float)(
                IntRepConsts.TARGET_THICKNESS + IntRepConsts.GAP_BETWEEN_ORBITS);

        int gameCyclesRemaining;
        int secondsRemaining;

        float[] targetPositions;
        float[] targetVelocities;
        float[] targetAccelerations;
        float[] cyclesUntilRelease;

        // new autopilot system.
        int[] cyclesUntilArrival;
        float[] arrivalPositions;

        float[] timeToOrbit;    // The time taken by the ball travelling between release and
        // first touch at the orbit.
        float[] angularOffset;  // The angular difference between the position of the ball when
        // released and its position when it first touches a given orbit.
        boolean[] orbitOccupied;
        final int MAX_NUM = IntRepConsts.MAX_NUMBER_OF_ORBITS;
        float ballAngularWidths[];

        Paint whitePaint;


        /**
         * No argument constructor. Just initializes the arrays for holding the target data.
         */
        Autopilot() {
            ballHeld = false;
            ballInStableOrbit = false;
            currentTargetSelected = -1;

            cyclesUntilRelease = new float[MAX_NUM];
            targetPositions = new float[MAX_NUM];
            targetVelocities = new float[MAX_NUM];
            targetAccelerations = new float[MAX_NUM];
            orbitOccupied = new boolean[MAX_NUM];
            timeToOrbit = new float[MAX_NUM];
            angularOffset = new float[MAX_NUM];
            ballAngularWidths = new float[MAX_NUM];


            cyclesUntilArrival = new int [MAX_NUM];
            arrivalPositions = new float [MAX_NUM];

            whitePaint = new Paint();
            whitePaint.setColor(Color.WHITE);
            whitePaint.setStyle(Paint.Style.FILL);
        }

        void calculateAngularWidths(float radius) {
            float radialDist;
            for (int i = 0; i < MAX_NUM; i++) {
                radialDist =  outermostOrbitEdge - (i * gapBetweenOrbits);
                ballAngularWidths[i] = ((float) (radius / (2.0f * PI * radialDist)));
            }
        }


        /**
         * Method calculates the constants arccos( orbitDist / radius) and sqrt(r2 - orbitDist2),
         * which vary only with the difficulty level and remain constant throughout any one game.
         * This is an optimization to prevent the autopilot from interminably repeating the same
         * expensive calculations.
         *
         * The orbits are numbered from the outside in, as with targetMangager.orbits.
         *
         * @param ball The swingball instance referenced by physicsAndBGRenderer.
         */
        void initializeConstantArrays(Swingball ball) {
            float radius;

            for (int i = 0; i < MAX_NUM; i++) {
                radius = outermostOrbitEdge - i * gapBetweenOrbits;
                double distToTravel = Math.sqrt(radius * radius - STABLE_ORBIT_DISTANCE * STABLE_ORBIT_DISTANCE);
                timeToOrbit[i] = (float)(distToTravel /  ball.velocity);
                angularOffset[i] = (float) Math.acos(STABLE_ORBIT_DISTANCE / radius);
            }
        }

        void calculateTimeAndPositionForOrbits(Swingball ball) {

            for (int i = 0; i < MAX_NUM; i++) {
                float radialDistToOrbitEdge = outermostOrbitEdge - (i * gapBetweenOrbits);
                float distToTravel = (float) Math.sqrt(
                        (radialDistToOrbitEdge * radialDistToOrbitEdge)
                        - ((ball.xPos - ball.xCenterOfRotation) * (ball.xPos - ball.xCenterOfRotation))
                        - ((ball.yPos - ball.yCenterOfRotation) * (ball.yPos - ball.yCenterOfRotation))
                );
                int cyclesTillArrival = (int) (distToTravel / ball.velocity);
                float xArrivalPos = (float) (ball.xPos + distToTravel * Math.cos(ball.direction));
                float yArrivalPos = (float) (ball.yPos + distToTravel * Math.sin(ball.direction));
                float arrivalAngle = (float) Math.atan2(yArrivalPos - ball.yCenterOfRotation, xArrivalPos - ball.xCenterOfRotation);

                cyclesUntilArrival[i] = cyclesTillArrival;
                arrivalPositions[i] = arrivalAngle;
            }
        }

        void drawImpactLocations(Canvas c, PlayAreaInfo playAreaInfo) {

            for (int i = 0; i < MAX_NUM; i++) {
                float dist = outermostOrbitEdge - gapBetweenOrbits * i;
                float xx = dist * (float) Math.cos(arrivalPositions[i]);
                float yy = dist * (float) Math.sin(arrivalPositions[i]);
                c.drawCircle(
                        playAreaInfo.xCenterOfCircle + xx * playAreaInfo.ratioOfActualToModel,
                        playAreaInfo.yCenterOfCircle + yy * playAreaInfo.ratioOfActualToModel,
                        10,
                        whitePaint
                );
            }
        }


        /*void update(TargetManager tm, Swingball ball) {
            loadData(tm, ball);
            // If the ball collided with something last cycle, switch on ballIsHeld.
            if (ball.ballHitSomethingLastCycle) {
                ballHeld = true;
            }

            // If the ball is held, select the best available target. If it is not held, there is no
            // need to perform the calculation.
            if (ballHeld) {
                currentTargetSelected = selectBestTarget(ball, tm);
            } else {
                currentTargetSelected = -1;
            }

            // If the release time for the selected Target has been reached, let go.
            if (currentTargetSelected != -1
                    && cyclesUntilRelease[currentTargetSelected] <= 2.0f
                    && ballInStableOrbit) {
                ballHeld = false;
            }
        }*/

        void update(TargetManager tm, Swingball ball) {

            calculateTimeAndPositionForOrbits(ball);
            ballInStableOrbit = ball.numberOfCyclesInOrbit > CYCLES_FOR_STABLE_ORBIT;

            // If the ball collided with something last cycle, switch on ballIsHeld.
            if (ball.ballHitSomethingLastCycle) {
                ballHeld = true;
            }

            for (int i = MAX_NUM - 1; i >= 0 ; i--) {
                boolean targetSaysYes;
                if (tm.orbits[i] != null) {
                    targetSaysYes = tm.orbits[i].willBallHitTarget(arrivalPositions[i], cyclesUntilArrival[i], ballAngularWidths[i]);
                    if (targetSaysYes) {
                        if (tm.orbits[i].type == TargetType.BLOCKER && !tm.orbits[i].isGhost) {
                            break;
                        } else if (tm.orbits[i].type == TargetType.KILLER && tm.orbits[i].invulnerable) {
                            break;
                        }
                    }
                    if (targetSaysYes && ballInStableOrbit
                            && tm.orbits[i].type != TargetType.BLOCKER) {
                        ballHeld = false;
                        break;
                    }
                }
            }
        }
    }

    public void setRewardArcOn(boolean rewardArcOn) {
        this.rewardArcOn = rewardArcOn;
    }

    public void reverseDirection() {
        direction = Helpers.resolveAngle(direction + PI);
    }

}
