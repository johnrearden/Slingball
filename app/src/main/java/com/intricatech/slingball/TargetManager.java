package com.intricatech.slingball;

import java.util.Random;

import static com.intricatech.slingball.TargetBehaviour.EXITING;

/**
 * Created by Bolgbolg on 27/04/2015.
 */


public class TargetManager implements LevelChangeObserver {

    private final String TAG;

    // This enum represents the state of the current level.
    public enum LevelPhase {
        LEVEL_IN_PROGRESS,
        LEVEL_COMPLETE_TIDYING_UP,
        READY_FOR_NEW_LEVEL
    }
    LevelPhase levelPhase;

    boolean levelCompleteTidyingUpPersistentFlag;

    TargetSupplier targetSupplier;
    MessageBoxManager messageBoxManager;
    //OrbitSetupCatalogue orbitSetupCatalogue;
    LevelCatalogue levelCatalogue;
    private LevelConfig levelConfig;
    private LevelManager levelManager;
    private FlyingHeart flyingHeart;
    private DifficultyLevelDirector difficultyLevelDirector;
    SoundManager soundManager;
    ExplosionSequencer[] explosionSequencers;
    private int level;
    private int numberOfQuarryOrbits;
    private int numberOfBlockerOrbits;
    private int orbitsInUse;
    boolean levelComplete;
    int hitsOccuredThisCycle;

    AbstractTarget[] orbits;
    boolean[] ballIsTouching;
    boolean[] leadingEdgeCollisionDetected;
    boolean rewarderTargetExists;
    boolean rewarderTargetAllowed;
    boolean blockersAreGhostedOut;
    int rewarderTargetIndex;

    Random random;

    int maxNumberOfTargets;
    int targetsRemaining;
    double xCenterOfRotation;
    double yCenterOfRotation;
    double scoreChangeThisUpdate;
    double energyChangeThisUpdate;
    float nextTargetPosition;
    boolean nextBlockerGoesClockwise;

    int ghostingGameCyclesRemaining, ghostingSecondsRemaining;

    public TargetManager(LevelManager levelManager,
                         DifficultyLevelDirector difficultyLevelDirector,
                         SoundManager soundManager,
                         MessageBoxManager messageBoxManager,
                         FlyingHeart flyingHeart) {

        TAG = getClass().getSimpleName();

        // Register with levelManager to be updated as the level changes.
        this.levelManager = levelManager;
        this.soundManager = soundManager;
        this.messageBoxManager = messageBoxManager;
        this.flyingHeart = flyingHeart;
        levelCompleteTidyingUpPersistentFlag = false;
        this.difficultyLevelDirector = difficultyLevelDirector;
        levelManager.register(this);
        levelComplete = false;
        hitsOccuredThisCycle = 0;
        levelPhase = LevelPhase.LEVEL_IN_PROGRESS;

        maxNumberOfTargets = IntRepConsts.MAX_NUMBER_OF_ORBITS;
        explosionSequencers = new ExplosionSequencer[maxNumberOfTargets * 2];
        for (int i = 0; i < explosionSequencers.length; i++) {
            explosionSequencers[i] = new ExplosionSequencer();
        }

        //orbitSetupCatalogue = OrbitSetupCatalogue.getInstance();
        levelCatalogue = LevelCatalogue.getInstance();
        updateConstants(level);
        targetSupplier = new TargetSupplier(levelManager, difficultyLevelDirector, levelCatalogue);

        orbits = new AbstractTarget[maxNumberOfTargets];
        assert orbitsInUse <= maxNumberOfTargets;

        ballIsTouching = new boolean[maxNumberOfTargets];
        for (boolean b : ballIsTouching) {
            b = false;
        }
        leadingEdgeCollisionDetected = new boolean[maxNumberOfTargets];
        for (boolean b : leadingEdgeCollisionDetected) {
            b = false;
        }

        xCenterOfRotation = IntRepConsts.X_CENTER_OF_ROTATION;
        yCenterOfRotation = IntRepConsts.Y_CENTER_OF_ROTATION;

        scoreChangeThisUpdate = 0;
        energyChangeThisUpdate = 0;
        nextBlockerGoesClockwise = true;
        blockersAreGhostedOut = false;
    }

    double updateTargetManager(Swingball swingball,
                               ScoreBubblePool bubblePool,
                               int consecHits,
                               Fireflies fireflies) {
        scoreChangeThisUpdate = 0;
        energyChangeThisUpdate = 0;
        hitsOccuredThisCycle = 0;

        // First handle any potential update to ghosting BLOCKERS.
        if (blockersAreGhostedOut) {
            if (ghostingGameCyclesRemaining-- <= 0) {
                turnOnBlockers();
            }
            ghostingSecondsRemaining = ghostingGameCyclesRemaining / (60 * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE);
            messageBoxManager.updateGhostBlocker(ghostingSecondsRemaining);
        }


        // The levelPhase can be switched to LEVEL_COMPLETE_TIDYING_UP when the player runs out of energy, or when the actual
        // level is complete.
        switch (levelPhase) {

            case LEVEL_IN_PROGRESS : {
                for (int i = 0; i < orbitsInUse; i++) {
                    if (orbits[i] != null) {

                        // If this target is a blocker or is shielded and is being touched by the ball for the first time,
                        // reverse the direction.
                        if ((orbits[i].type == TargetType.BLOCKER || orbits[i].isShielded)
                                && ballIsTouching[i]
                                && !orbits[i].wasTouchingBall
                                && leadingEdgeCollisionDetected[i]) {
                            orbits[i].velocity = -orbits[i].velocity;
                        }
                        leadingEdgeCollisionDetected[i] = false;

                        // Update the target.
                        orbits[i].updateTarget(ballIsTouching[i], TargetBehaviour.STANDARD, 0, 0, swingball);

                        // If this target is a quarry and has been hit, delete it, and start an explosionSequence for it.
                        // If no explosionSequencer is available, no explosion appears (unfortunately).
                        if (orbits[i].taggedForDeletion) {
                            soundManager.playTargetCollision(0);
                            ballIsTouching[i] = false;
                            assignExplosionSequencer(orbits[i]);
                            double s;
                            switch(difficultyLevelDirector.getDiffLev()) {
                                case EASY: {
                                    s = orbits[i].score * 0.5f;
                                    break;
                                }
                                case NORMAL: {
                                    s = orbits[i].score * 0.7f;
                                    break;
                                }
                                case HARD: {
                                    s = orbits[i].score;
                                    break;
                                }
                                default: {
                                    s = orbits[i].score;
                                }
                            }
                            if (orbits[i].type != TargetType.REWARDER) {
                                bubblePool.activateNewScoreBubble(
                                        (int) s,
                                        orbits[i].alpha,
                                        orbits[i].orbitIndex,
                                        ScoreBubblePool.BubbleColor.GREEN);
                            }
                            if (orbits[i].type == TargetType.REWARDER) {
                                fireflies.turnOffAll();
                                TarRewarder tr = (TarRewarder) orbits[i];
                                if (tr.bonusType == TarRewarder.BonusType.EXTRA_HEART) {
                                    if (!tr.diedWithHisBootsOn) {
                                        flyingHeart.activateFlyingHeart(
                                                tr.orbitIndex,
                                                tr.alpha,
                                                tr.velocity
                                        );
                                    }
                                }
                            }
                            scoreChangeThisUpdate += s;
                            energyChangeThisUpdate += orbits[i].energyValue;
                            if (difficultyLevelDirector.getDiffLev() != DifficultyLevel.HARD) {
                                targetsRemaining--;
                            } else if (levelManager.getLevel() < difficultyLevelDirector.getLastLevel()) {
                                targetsRemaining--;
                            }
                            if (orbits[i].type != TargetType.REWARDER) {
                                hitsOccuredThisCycle++;
                            } else {
                                TarRewarder tr = (TarRewarder) orbits[i];
                                if (!tr.diedWithHisBootsOn) {
                                    hitsOccuredThisCycle++;
                                }
                            }
                            /*int totalConsecHits = hitsOccuredThisCycle + consecHits;
                            if (totalConsecHits > 1 && orbits[i].type != TargetType.REWARDER) {
                                int consecHitsBubbleValue =
                                        totalConsecHits <= 5 ? (totalConsecHits - 1) * 5 : 25;
                                bubblePool.activateNewScoreBubble(
                                        consecHitsBubbleValue,
                                        orbits[i].alpha + orbits[i].size.getAngularSize() / 2,
                                        orbits[i].orbitIndex + 3,
                                        ScoreBubblePool.BubbleColor.GREEN);
                                scoreChangeThisUpdate += consecHitsBubbleValue;
                            }*/

                            nextTargetPosition = (float)(Helpers.resolveAngle(orbits[i].alpha + Math.PI));
                            if (orbits[i].type == TargetType.REWARDER) {
                                rewarderTargetExists = false;
                            }
                            orbits[i] = null;
                        }

                    }
                    if (i < numberOfQuarryOrbits && orbits[i] == null && targetsRemaining >= numberOfQuarryOrbits) {
                        orbits[i] = getNewQuarry(i, nextTargetPosition);
                    }
                    if (i >= numberOfQuarryOrbits && orbits[i] == null) {
                        orbits[i] = getNewBlocker(i, nextTargetPosition, nextBlockerGoesClockwise);
                        nextBlockerGoesClockwise = !nextBlockerGoesClockwise;;
                    }
                }
                // If there are no targets remaining to be added, switch the levelComplete flag, and instruct the remaining
                // targets, if any, to exit the game area.
                if (targetsRemaining <= 0) {
                    levelCompleteTidyingUpPersistentFlag = true;
                    levelPhase = LevelPhase.LEVEL_COMPLETE_TIDYING_UP;
                    for (int i = 0; i < orbitsInUse; i++) {
                        if (orbits[i] != null) {
                            orbits[i].behaviour = TargetBehaviour.EXITING;
                        }
                    }
                }
                break;
            }

            case LEVEL_COMPLETE_TIDYING_UP : {
                boolean allTargetsGone = true;
                for (int i = 0; i < maxNumberOfTargets; i++) {
                    if (orbits[i] != null) {
                        allTargetsGone = false;
                        orbits[i].updateTarget(false, EXITING, 0, 0, swingball);
                        if (orbits[i].taggedForDeletion) {
                            orbits[i] = null;
                        }
                    }
                }
                if (allTargetsGone) {
                    levelPhase = LevelPhase.READY_FOR_NEW_LEVEL;
                    // remove final target.
                    for (int i = 0; i < orbitsInUse; i++) {
                        orbits[i] = null;
                    }
                }
                break;
            }
        }




        // Finally, return the change in score.
        return scoreChangeThisUpdate;
    }

    // Method called by physicsAndBGRenderer when gameOver occurs.
    void forceEndOfLevel() {
        for (int i = 0; i < maxNumberOfTargets; i++) {
            if (orbits[i] != null) {
                orbits[i].taggedForDeletion = true;
            }
        }
        levelPhase = LevelPhase.LEVEL_COMPLETE_TIDYING_UP;
    }

    // Method assigns an explosion sequencer from the pool to a target being deleted.
    void assignExplosionSequencer(AbstractTarget t) {
        int index = getFirstAvailableExplosionSequencer();
        if (index != -1) {
            explosionSequencers[index].assignExplosionSequencer(
                    (float)t.alpha,
                    t.orbitIndex,
                    t.size);
        }
    }
    // Method populates the orbits at the start of each level. called by physicsAndBGRenderer.
    void populateOrbits() {

        // Add the quarries.
        for (int i = 0; i < numberOfQuarryOrbits; i++) {
            nextTargetPosition = (float)(-Math.PI + Math.random() * Math.PI * 2);
            orbits[i] = getNewQuarry(i, nextTargetPosition);
        }

        // Add the blockers, if any.
        for (int i = numberOfQuarryOrbits; i < numberOfQuarryOrbits + numberOfBlockerOrbits; i++) {
            nextTargetPosition = (float)(-Math.PI + Math.random() * Math.PI * 2);
            orbits[i] = getNewBlocker(i, nextTargetPosition, nextBlockerGoesClockwise);
            nextBlockerGoesClockwise = !nextBlockerGoesClockwise;
        }
    }

    // Method selects, gets and returns a random AbstractTarget messageType based on the current level's weighting balance.
    private AbstractTarget getNewQuarry(int orbitIndex, float angle) {

        AbstractTarget newQuarry;
        TargetType newType = levelConfig.chooseWeightedRandomTargetType();
        newQuarry = switchOnTargetType(newType, orbitIndex);
        newQuarry.alpha = angle;
        if (newQuarry instanceof TarOscillator) {
            TarOscillator to = (TarOscillator) newQuarry;
            to.setInitialAngleAsPivot();
        }
        return newQuarry;

        }

    private AbstractTarget switchOnTargetType(TargetType type, int orbitIndex) {
        AbstractTarget newQuarry = null;
        switch(type) {
            case DARTER: {
                newQuarry = targetSupplier.getNewTarDarter(orbitIndex);
                break;
            }
            case CRUISER: {
                newQuarry = targetSupplier.getNewTarCruiser(orbitIndex);
                break;
            }
            case OSCILLATOR: {
                newQuarry = targetSupplier.getNewTarOscillator(orbitIndex);
                break;
            }
            case TADPOLE: {
                newQuarry = targetSupplier.getNewTarTadpole(orbitIndex);
                break;
            }
            case DODGER: {
                newQuarry = targetSupplier.getNewTarDodger(orbitIndex);
                break;
            }
            case FLICKER: {
                newQuarry = targetSupplier.getNewTarFlicker(orbitIndex);
                break;
            }
            case DECOYS: {
                newQuarry = targetSupplier.getNewTarDecoys(orbitIndex);
                break;
            }
            case KILLER: {
                newQuarry = targetSupplier.getNewTarKiller(orbitIndex);
                break;
            }

            case REWARDER: {
                if (!rewarderTargetExists && rewarderTargetAllowed) {
                    newQuarry = targetSupplier.getNewTarRewarder(orbitIndex);
                    rewarderTargetExists = true;
                    rewarderTargetAllowed = false;
                    rewarderTargetIndex = orbitIndex;
                } else {
                    newQuarry = switchOnTargetType(levelConfig.getAlternativeToRewarderTarget(), orbitIndex);
                }
                break;
            }
        }
        return newQuarry;

    }





    // Method returns a new TarBlocker.
    private AbstractTarget getNewBlocker(int orbitIndex, float angle, boolean nextBlockerGoesClockwise) {
        AbstractTarget newBlocker = targetSupplier.getNewTarBlocker(
                orbitIndex,
                nextBlockerGoesClockwise,
                difficultyLevelDirector.getDiffLev());
        newBlocker.alpha = angle;
        return newBlocker;
    }

    String getTargetListAsString() {
        StringBuilder sb = new StringBuilder("orbits : \n");
        for (int i = 0; i < orbits.length; i++) {
            if (orbits[i] == null) {
                sb.append(i + " : null, \n");
            } else {
                sb.append(i + " : non-null : " + orbits[i].toString() + "\n");
            }
        }
        return sb.toString();

    }
    /**
     * @return the orbits
     */
    public AbstractTarget[] getOrbits() {
        return orbits;
    }
    /**
     * @param orbits the orbits to set
     */
    public void setOrbits(AbstractTarget[] orbits) {
        this.orbits = orbits;
    }

    @Override
    public void updateConstants(int suppliedLevel) {

        level = suppliedLevel;
        levelConfig = levelCatalogue.getLevelConfig(level, difficultyLevelDirector.getDiffLev());
        numberOfQuarryOrbits = levelConfig.getNumberOfQuarryOrbits();
        targetsRemaining = levelConfig.getTotalNumberOfTargets();
        numberOfBlockerOrbits = levelConfig.getNumberOfBlockerOrbits();
        orbitsInUse = numberOfBlockerOrbits + numberOfQuarryOrbits;
    }

    public LevelPhase getLevelPhase() {
        return levelPhase;
    }

    public void setLevelPhase(LevelPhase levelPhase) {
        this.levelPhase = levelPhase;
    }

    public void setLevelCompleteTidyingUpPersistentFlag(boolean levelCompleteTidyingUpPersistentFlag) {
        this.levelCompleteTidyingUpPersistentFlag = levelCompleteTidyingUpPersistentFlag;
    }

    // Returns the index of the first available ExplosionSequencer in the array/pool, and returns
    // -1 if all the explosionSequencers are State.ACTIVE.
    private int getFirstAvailableExplosionSequencer() {
        int count = 0;
        while (count < explosionSequencers.length) {
            if (explosionSequencers[count].state == ExplosionSequencer.State.WAITING) {
                return count;
            } else {
                count++;
            }
        }
        return -1;
    }

    void turnOffBlockers() {
        blockersAreGhostedOut = true;
        messageBoxManager.messageMap.get(MessageBoxManager.MessageType.GHOST_BLOCKERS).on = true;
        ghostingGameCyclesRemaining = IntRepConsts.GHOSTING_CYCLES;
        for (AbstractTarget target : orbits) {
            if (target != null && target.type == TargetType.BLOCKER) {
                target.setGhosting(true);
            }
        }
    }

    void turnOnBlockers() {
        messageBoxManager.messageMap.get(MessageBoxManager.MessageType.GHOST_BLOCKERS).on = false;
        blockersAreGhostedOut = false;
        for (AbstractTarget target : orbits) {
            if (target != null && target.type == TargetType.BLOCKER) {
                target.setGhosting(false);
            }
        }
    }
}

