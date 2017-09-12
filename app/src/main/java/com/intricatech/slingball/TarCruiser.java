package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 15/06/2015.
 *
 * Cruisers
 */

public class TarCruiser extends AbstractTarget {

    public TarCruiser (TargetSize size,
                       int orbitIndex,
                       LevelManager levelManager) {
    	
    	// Call the superclass constructor to initialize values set by IntRepConsts.
        super();
        this.levelManager = levelManager;
        levelManager.register(this);
        
        this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.CRUISER;
        spriteType = SpriteType.CRUISER;
        acceleration = IntRepConsts.TARGET_ACCELERATION;
        reportedAcceleration = 0;
        score = 10.0;
        energyValue = 5.0;

        color = TargetColor.GREEN;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();
    }
    
    void doTargetSpecificUpdate() {
    	// Standard behaviour is to continue at constant velocity.
    }
    
    public String toString() {
    	return this.getClass().toString();
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
