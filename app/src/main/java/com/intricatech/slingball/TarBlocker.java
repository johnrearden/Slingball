package com.intricatech.slingball;

public class TarBlocker extends AbstractTarget{



	enum BrightnessLevel {
		MAX(1),
		HIGH(180),
		REDUCING(180),
		LOW(30),
		INCREASING(30);

		int period;

		BrightnessLevel(int period) {
			this.period = period;
		}
	}
	BrightnessLevel brightnessLevel;



	static final int ALPHA_LOW_BOUND = 0;
	static final int ALPHA_HIGH_BOUND = 150;

	int cycles;     // the total number of cycles played so far.
	int alphaRange;
    private float probabilityOfExtraHeart;

	public TarBlocker(TargetSize size,
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
        type = TargetType.BLOCKER;
		spriteType = SpriteType.BLOCKER;
		acceleration = IntRepConsts.TARGET_ACCELERATION;
		reportedAcceleration = 0;
		score = 10.0;
		brightnessLevel = BrightnessLevel.HIGH;
		cycles = 0;
		opacity = 0.0f;
		alphaRange = ALPHA_HIGH_BOUND - ALPHA_LOW_BOUND;

		color = TargetColor.GRAY;
		color = TargetColor.YELLOW;
		isShielded = true;
		wasTouchingBall = false;
		taggedForDeletion = false;
		distFromOrigin = setDistFromOrigin();


	}

    @Override
    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth) {
        float futureAngle = (float) Helpers.resolveAngle(alpha + velocity * cyclesUntilArrival);

        float minAngle = futureAngle - size.getAngularSize() * 1.5f - ballAngularWidth;
        float maxAngle = futureAngle + size.getAngularSize() * 1.5f + ballAngularWidth;

        if (angle > minAngle && angle < maxAngle) {
            return true;
        } else {
            return false;
        }
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
	void doTargetSpecificUpdate() {
	}
}
