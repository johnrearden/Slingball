package com.intricatech.slingball;

public class TarOscillator extends AbstractTarget {

    static final String TAG = "TarOscillator";
    static final float PI = (float) Math.PI;

	// Instance fields particular to TarOscillator
	private double pivotAngle;
    private float accelerationModifier;

    private int period;
    private float amplitude;
    private int cycles;

	public TarOscillator(TargetSize size,
            			 int orbitIndex,
            			 LevelManager levelManager) {
		// Call the superclass constructor to initialize values set by IntRepConsts.
        super();
        this.levelManager = levelManager;
        levelManager.register(this);
        
        this.size = size;
        this.orbitIndex = orbitIndex;
        type = TargetType.OSCILLATOR;
		spriteType = SpriteType.OSCILLATOR;
        acceleration = IntRepConsts.TARGET_ACCELERATION; //*.05f
		reportedAcceleration = 0;

        cycles = 0;

        color = TargetColor.RED;
        isShielded = false;
        wasTouchingBall = false;
        taggedForDeletion = false;
        distFromOrigin = setDistFromOrigin();
        score = 15.0;
		energyValue = 7.0;
	}
	
	void doTargetSpecificUpdate() {
		
		/*float distToPivotAngle = (float) Helpers.resolveAngle(pivotAngle - alpha);
		reduceBelowPI(distToPivotAngle);
        Log.d(TAG, "distToPivotAngle == " + distToPivotAngle);

		// Set acceleration proportional to dist.
        acceleration = distToPivotAngle / IntRepConsts.OSCILLATOR_DIST_TO_ACC_RATIO * accelerationModifier;
		
		// Accelerate the target as appropriate
		reportedAcceleration = acceleration*//* * directionOfDiff*//*;
		velocity = velocity + reportedAcceleration;*/

        if (++cycles > period) {
            cycles = 0;
        }

        alpha = (float) (pivotAngle + amplitude * Math.sin(2 * PI * ((float)cycles / period)));

    }
	
	/*void chooseNewPivotAngle(int level, DifficultyLevel diffLevel) {
        int maxNumberOfLevels;
        switch (diffLevel) {
            case EASY:
                maxNumberOfLevels = IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY;
                accelerationModifier = 0.6f;
                break;
            case NORMAL:
                maxNumberOfLevels = IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL;
                accelerationModifier = 0.8f;
                break;
            case HARD:
                maxNumberOfLevels = IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
                accelerationModifier = 1.0f;
                break;
            default:
                maxNumberOfLevels = IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
        }
        float baseDistToPivot;
        switch (diffLevel) {
            case EASY:
                baseDistToPivot = PI * 0.2f;
                break;
            case NORMAL:
                baseDistToPivot = PI * 0.32f;
                break;
            default:
            case HARD:
                baseDistToPivot = PI * 0.45f;
                break;
        }
        float offset = PI * 0.45f + (baseDistToPivot * level / maxNumberOfLevels);
		pivotAngle = (float) Helpers.resolveAngle(alpha + offset);
	}*/

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
        int futureTime = (cycles + cyclesUntilArrival) % period;
        float futureAngPos = (float) (pivotAngle + amplitude * Math.sin(2 * PI * ((float)futureTime / period)));

        float minAngle = futureAngPos - size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;
        float maxAngle = futureAngPos + size.getAngularSize() * AUTOPILOT_ANGULAR_TOLERANCE;

        if (angle > minAngle && angle < maxAngle) {
            return true;
        } else {
            return false;
        }
    }

    void setInitialAngleAsPivot() {
        pivotAngle = alpha;
    }

    void configMe(int level, DifficultyLevel difficultyLevel) {

        float progressRatio = 0;
        switch (difficultyLevel) {
            case EASY:
                amplitude = PI * 0.37f;
                progressRatio = level / IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY;
                period = (int) ((3.5f - 0.5f * progressRatio)
                        * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE * 60);
                break;
            case NORMAL:
                amplitude = PI * 0.43f;
                progressRatio = level / IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL;
                period = (int) ((3.5f - 0.5f * progressRatio)
                        * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE * 60);
                break;
            case HARD:
                amplitude = PI * 0.5f;
                progressRatio = level / IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
                period = (int) ((3.0f - 0.6f * progressRatio)
                        * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE * 60);
                break;
        }
    }
}
