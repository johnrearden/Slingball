package com.intricatech.slingball;

import static java.lang.Math.PI;


/**
 * Created by Bolgbolg on 14/06/2015.
 */
public class TargetSupplier
        implements LevelChangeObserver,
                   DifficultyLevelObserver {

    static final String TAG = "TargetSupplier";
    private LevelManager levelManager;
    private DifficultyLevelDirector difficultyLevelDirector;
    private DifficultyLevel difficultyLevel;
    //private OrbitSetupCatalogue orbitSetupCatalogue;
    private LevelCatalogue levelCatalogue;

    private static final float HARD_BASE_POSSIBILITY_OF_TINY_TARGET = 0.25f;
    private static final float HARD_BASE_POSSIBILITY_OF_SMALL_TARGET = 0.5f;
    private static final float HARD_POSSIBILITY_INCREMENT_PER_LEVEL = 0.02f;
    

    private int targetsSupplied;
    private int level;
    double maxVelocityOfTargets;

    TargetSupplier(
            LevelManager levelManager,
            DifficultyLevelDirector difficultyLevelDirector,
            LevelCatalogue levelCatalogue) {
        this.levelManager = levelManager;
    	levelManager.register(this);
        this.difficultyLevelDirector = difficultyLevelDirector;
        registerWithDifficultyLevelDirector();
        this.levelCatalogue = levelCatalogue;

    	targetsSupplied = 0;
    	maxVelocityOfTargets = IntRepConsts.TARGET_MAX_VELOCITY;
    }

    // Method returns a new TarCruiser object, with fields initialized to the appropriate values for the current level.
    TarCruiser getNewTarCruiser(int orbitIndex) {
    	
    	TarCruiser tc;
    	
    	// Create and configure the new TarCruiser.
    	tc = new TarCruiser(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
    	configureTarget(tc);
    	
    	// Finally, return the new TarCruiser.
    	return tc;
    }
    
 // Method returns a new TarDarter object, with fields initialized to the appropriate values for the current level.
    TarDarter getNewTarDarter(int orbitIndex) {
    	
    	TarDarter td;
    	
    	// Create and configure the new TarDarter.
    	td = new TarDarter(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
    	configureTarget(td);
    	
    	// Finally, return the new TarDarter.
    	return td;
    }
    
 // Method returns a new TarTadpole object, with fields initialized to the appropriate values for the current level.
    TarTadpole getNewTarTadpole(int orbitIndex) {
    	
    	TarTadpole tt;
    	
    	// Create and configure the new TarTadpole.
    	tt = new TarTadpole(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
    	configureTarget(tt);
    	
    	// Finally, return the new TarTadpole.
    	return tt;
    }
    
 // Method returns a new TarOscillator object, with fields initialized to the appropriate values for the current level.
    TarOscillator getNewTarOscillator(int orbitIndex) {
    	
    	TarOscillator to;
    	
    	// Create and configure the new TarOscillator.
    	to = new TarOscillator(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
    	configureTarget(to);
    	to.configMe(levelManager.getLevel(), difficultyLevel);
    	// Finally, return the new TarOscillator.
    	return to;
    }
    
 // Method returns a new TarBlocker object, with fields initialized to the appropriate values for the current level.
    TarBlocker getNewTarBlocker(int orbitIndex, boolean nextBlockerGoesClockwise, DifficultyLevel diffLev) {
    	
    	TarBlocker tb;
    	
    	// Create and configure the new TarBlocker.
    	tb = new TarBlocker(calculateBlockerSize(difficultyLevel), orbitIndex, levelManager, diffLev);
    	configureBlocker(tb, nextBlockerGoesClockwise);
    	
    	// Finally, return the new TarBlocker.
    	return tb;
    }

    TarDodger getNewTarDodger(int orbitIndex) {

        TarDodger td;

        td = new TarDodger(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
        configureTarget(td);

        // Override the configureTarget to set the maxVel to between 0.4 and 0.6 times the normal maxVel.
        td.maxVel = (float)(maxVelocityOfTargets * 0.6 + (maxVelocityOfTargets * Math.random() * 0.2));

        // Finally, return the new TarBlocker.
        return td;
    }

    TarFlicker getNewTarFlicker(int orbitIndex) {

        TarFlicker tf;

        tf = new TarFlicker(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
        configureTarget(tf);

        // Override the configureTarget to set the maxVel to between 0.4 and 0.6 times the normal maxVel.
        tf.maxVel = (float)(maxVelocityOfTargets * 0.6 + (maxVelocityOfTargets * Math.random() * 0.2));

        // Finally, return the new TarBlocker.
        return tf;
    }

    TarDecoys getNewTarDecoys(int orbitIndex) {

        TarDecoys tarDecoys;

        tarDecoys = new TarDecoys(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
        configureTarget(tarDecoys);
        tarDecoys.setDifficultyLevel(difficultyLevel);

        // Override the configureTarget to set the maxVel to between 0.4 and 0.6 times the normal maxVel.
        tarDecoys.maxVel = (float)(maxVelocityOfTargets * 0.6 + (maxVelocityOfTargets * Math.random() * 0.2));

        // Finally, return the new TarBlocker.
        return tarDecoys;
    }

    TarKiller getNewTarKiller(int orbitIndex) {

        TarKiller tarKiller;

        tarKiller = new TarKiller(calculateTargetSize(difficultyLevel), orbitIndex, levelManager);
        configureTarget(tarKiller);
        tarKiller.configMe(difficultyLevel);
        tarKiller.setDifficultyLevel(difficultyLevel);
        tarKiller.velocity = 0;

        // Override the configureTarget to set the maxVel to between 0.4 and 0.6 times the normal maxVel.
        tarKiller.maxVel = (float)(maxVelocityOfTargets * IntRepConsts.KILLER_MAXVEL_MULTIPLIER);

        // Finally, return the new TarKiller.
        return tarKiller;
    }

    TarRewarder getNewTarRewarder(int orbitIndex) {

        TarRewarder tarRewarder;

        tarRewarder = new TarRewarder(
                calculateRewarderSize(difficultyLevel),
                orbitIndex,
                levelManager,
                difficultyLevel);
        configureTarget(tarRewarder);

        // Override the configureTarget to set the maxVel to between 0.4 and 0.6 times the normal maxVel.
        tarRewarder.maxVel = (float)maxVelocityOfTargets * 0.3f;

        // Finally, return the new TarRewarder.
        return tarRewarder;
    }
    
	@Override
	public void updateConstants(int level) {
		this.level = level;
        boolean hard = false;
        if (difficultyLevel == DifficultyLevel.HARD) {
            hard = true;
        }
		maxVelocityOfTargets =
                IntRepConsts.TARGET_MAX_VELOCITY
                        + (hard ? IntRepConsts.TARGET_VELOCITY_ADDON_FOR_HARD_DIFFICULTY : 0)
                        + (IntRepConsts.TARGET_VELOCITY_INCREMENT_PER_LEVEL * level);
		if (maxVelocityOfTargets > IntRepConsts.LIMIT_OF_TARGET_MAX_VELOCITY) {
			maxVelocityOfTargets = IntRepConsts.LIMIT_OF_TARGET_MAX_VELOCITY;
		}
        final float chanceTiny = HARD_BASE_POSSIBILITY_OF_TINY_TARGET + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL);
        final float chanceSmall = HARD_BASE_POSSIBILITY_OF_SMALL_TARGET + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL);
        final float chanceMedium = 1 - (chanceTiny + chanceSmall);
    }
	
	TargetSize calculateTargetSize(DifficultyLevel difficultyLevel) {
    	TargetSize size = null;
    	switch (difficultyLevel) {
            case EASY: {
                double random = Math.random();
                if (random < HARD_BASE_POSSIBILITY_OF_TINY_TARGET
                        + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)) {
                    size = TargetSize.SMALL;
                } else if (random < HARD_BASE_POSSIBILITY_OF_SMALL_TARGET
                        + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)){
                    size = TargetSize.MEDIUM;
                } else {
                    size = TargetSize.LARGE;
                }
                break;
            }
            case NORMAL: {
                double random = Math.random();
                if (random < HARD_BASE_POSSIBILITY_OF_TINY_TARGET
                        + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)) {
                    size = TargetSize.SMALL;
                } else if (random < HARD_BASE_POSSIBILITY_OF_SMALL_TARGET
                        + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)){
                    size = TargetSize.MEDIUM;
                } else {
                    size = TargetSize.LARGE;
                }
                break;
            }
            case HARD: {
                double random = Math.random();
                if (random < HARD_BASE_POSSIBILITY_OF_TINY_TARGET
                             + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)) {
                    size = TargetSize.SMALL;
                } else if (random < HARD_BASE_POSSIBILITY_OF_SMALL_TARGET
                                    + (level * HARD_POSSIBILITY_INCREMENT_PER_LEVEL)){
                    size = TargetSize.SMALL;
                } else {
                    size = TargetSize.MEDIUM;
                }
                break;
            }
            default : {
                size = TargetSize.LARGE;
            }
        }
    	return size;
	}

    TargetSize calculateBlockerSize(DifficultyLevel difficultyLevel) {
        TargetSize size = null;
        int numberOfBlockers = levelCatalogue
                .getLevelConfig(level, difficultyLevelDirector.getDiffLev())
                .getNumberOfBlockerOrbits();
        switch (difficultyLevel) {
            default :
            case EASY: {
                size = TargetSize.MEDIUM;
                break;
            }
            case NORMAL: {
                size = TargetSize.LARGE;
                break;
            }
            case HARD: {
                switch (numberOfBlockers) {
                    case 1 : {
                        size = TargetSize.LARGE;
                        break;
                    }
                    case 2 : {
                        size = TargetSize.LARGE;
                        break;
                    }
                    case 3 : {
                        size = TargetSize.MEDIUM;
                        break;
                    }
                    case 4 : {
                        size = TargetSize.SMALL;
                        break;
                    }
                    default : {
                        size = TargetSize.SMALL;
                    }
                }
                break;
            }

        }
        return size;
    }

    static TargetSize calculateRewarderSize(DifficultyLevel difficultyLevel) {
        TargetSize size = null;
        switch (difficultyLevel) {
            case EASY: {
                size = TargetSize.LARGE;
                break;
            }
            case NORMAL: {
                size = TargetSize.LARGE;
                break;
            }
            case HARD: {
                size = TargetSize.MEDIUM;
                break;
            }
            default : {
                size = TargetSize.SMALL;
            }
        }
        return size;
    }
	
	// method sets the starting angle for any AbstractTarget to PI / 2, and chooses a random direction for the velocity.
	void configureTarget(AbstractTarget t) {
		t.alpha = (float)(-PI + Math.random() * PI * 2);

        if (t.type != TargetType.OSCILLATOR) {
            t.maxVel = (float)(maxVelocityOfTargets - (Math.random() * 0.2 * maxVelocityOfTargets));
            int direction = Math.random() < 0.5 ? -1 : 1;
            t.velocity = t.maxVel * direction;
        } else {
            t.maxVel = (float)maxVelocityOfTargets;
            t.velocity = 0.0f;
        }

	}

    // method sets the starting angle for any AbstractTarget to PI / 2, and chooses a random direction for the velocity.
    void configureBlocker(AbstractTarget t, boolean nextBlockerGoesClockwise) {
        t.alpha = (float)(-PI + Math.random() * PI * 2);

        // Override the configureTarget to set the maxVel to between 0.2 and 0.5 times the normal maxVel.
        t.maxVel = (float)(maxVelocityOfTargets * 0.2 + (maxVelocityOfTargets * Math.random() * 0.3));
        int direction = nextBlockerGoesClockwise ? 1 : -1;
        t.velocity = t.maxVel * direction;

    }

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
        difficultyLevel = level;
    }
}
