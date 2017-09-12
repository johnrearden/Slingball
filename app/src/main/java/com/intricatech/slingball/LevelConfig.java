package com.intricatech.slingball;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intricatech.slingball.RewardsManager.RewardType;

/**
 * Created by Bolgbolg on 12/07/2017.
 */
public class LevelConfig {

    private String TAG;
    private boolean DEBUG = false;

    /**
     * Hashmaps which holds the targetTypes and their weightings.
     */
    private final Map<TargetType, ProbabilityRange> targetTypesRangeMap;
    private final Map<TargetType, Float> targetTypeProbabilityMap;

    /**
     * Hashmap which holds the RewardTypes and their weightings.
     */
    private final Map<RewardType, ProbabilityRange> rewardTypesMap;


    /**
     * The total number of targets (including rewarders but not blockers) which comprise this
     * level.
     */
    private final int totalNumberOfTargets;


    /**
     * The maximum number of Quarries (non-blockers) that can appear at any one time.
     */
    private final int numberOfQuarryOrbits;


    /**
     * The numberOfBlockers that can appear at any one time.
     */
    private final int numberOfBlockerOrbits;

    /**
     * Cached total of weightings, used for calculating probabilities.
     */
    private float totalOfTargetWeightings;


    /**
     * Cached total of weightings, used for calculating probabilities.
     */
    private float totalOfRewardWeightings;


    /**
     * Public constructor. Takes a list each of TargetElements and RewardElements, and populates
     * the respective maps.
     *
     * @param totalNumberOfTargets The total number of targets that comprise this level.
     * @param numberOfBlockerOrbits The number of blockers that can appear simultaneously.
     * @param numberOfQuarryOrbits The max number of quarries than can appear simultaneously.
     * @param targetElementsList A list of valid TargetElement objects. Restriction : members with
     *                           weightings < 0 will not be added to the map.
     * @param rewardElementList A list of valid RewardElement objects. Restriction : members with
     *                          weightings < 0 will not be added to the map.
     */
    public LevelConfig(
            int totalNumberOfTargets,
            int numberOfQuarryOrbits,
            int numberOfBlockerOrbits,
            List<TargetElement> targetElementsList,
            List<RewardElement> rewardElementList) {

        TAG = getClass().getSimpleName();

        this.totalNumberOfTargets = totalNumberOfTargets;
        this.numberOfQuarryOrbits = numberOfQuarryOrbits;
        this.numberOfBlockerOrbits = numberOfBlockerOrbits;

        totalOfTargetWeightings = 0;
        totalOfRewardWeightings = 0;

        targetTypesRangeMap = new HashMap<>();
        targetTypeProbabilityMap = new HashMap<>();
        for (TargetElement te : targetElementsList) {
            if (te.weighting > 0) {
                ProbabilityRange pr = new ProbabilityRange(
                        totalOfTargetWeightings,
                        totalOfTargetWeightings + te.weighting);
                targetTypesRangeMap.put(te.targetType, pr);
                targetTypeProbabilityMap.put(te.targetType, te.weighting);
                if (DEBUG) {
                    Log.d(TAG, te.targetType + pr.toString());
                }
                totalOfTargetWeightings += te.weighting;
            }
        }

        rewardTypesMap = new HashMap<>();
        for (RewardElement re : rewardElementList) {
            if (re.weighting > 0) {
                ProbabilityRange pr = new ProbabilityRange(
                        totalOfRewardWeightings,
                        totalOfRewardWeightings + re.weighting);
                rewardTypesMap.put(re.rewardType, pr);
                if (DEBUG) {
                    Log.d(TAG, re.rewardType + pr.toString());
                }
                totalOfRewardWeightings += re.weighting;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "totalOfTargetWeightings == " + totalOfTargetWeightings);
            Log.d(TAG, "totalOfRewardWeightings == " + totalOfRewardWeightings);
        }
    }

    /**
     * Method generates a random number between 0 and the totalTargetWeighting, and chooses
     * a TargetType based on that.
     *
     * @return The TargetType chosen by weighted random selection.
     */
    public TargetType chooseWeightedRandomTargetType() {
        float randomFloat = (float) (Math.random() * totalOfTargetWeightings);
        for (TargetType t : targetTypesRangeMap.keySet()) {
            ProbabilityRange pr = targetTypesRangeMap.get(t);
            if (randomFloat >= pr.lowerBound && randomFloat <= pr.upperBound) {
                return t;
            }
        }
        return null;
    }

    /**
     * Method generates a random number between 0 and the totalRewardWeighting, and chooses
     * a RewardType based on that.
     *
     * @return The RewardType chosen by weighted random selection.
     */
    public RewardType chooseWeightedRandomRewardType() {
        float randomFloat = (float) (Math.random() * totalOfRewardWeightings);
        for (RewardType r : rewardTypesMap.keySet()) {
            ProbabilityRange pr = rewardTypesMap.get(r);
            if (randomFloat >= pr.lowerBound && randomFloat <= pr.upperBound) {
                return r;
            }
        }
        return null;
    }

    /**
     * Method will return a TargetType other than REWARDER (used in the event of a REWARDER
     * already existing). If no such type exists in the map for this level, a CRUISER is returned
     * by default
     *
     * @return The alternative TargetType.
     */
    public TargetType getAlternativeToRewarderTarget() {
        for (TargetType t : targetTypesRangeMap.keySet()) {
            if (t != TargetType.REWARDER) {
                return t;
            }
        }
        return TargetType.CRUISER;
    }

    /**
     * Class encapsulates a TargetElement - a targetType and associated weighting.
     */
    static class TargetElement {
        final TargetType targetType;
        final float weighting;

        TargetElement(TargetType t, float w) {
            this.targetType = t;
            this.weighting = w;
        }
    }

    /**
     * Return the probability assigned to a given Target.
     *
     * @param tt The TargetType whose probability is requested.
     * @return float : Probability
     */
    public float getProbabilityOfTarget(TargetType tt) {
        float f;
        if (targetTypeProbabilityMap.containsKey(tt)) {
            f = targetTypeProbabilityMap.get(tt);
        } else {
            f = 0;
        }
        return f;
    }

    /**
     * Class encapsulates a RewardElement - a rewardType and associated weighting.
     */
    static class RewardElement {
        final RewardType rewardType;
        final float weighting;

        RewardElement(RewardType r, float w) {
            this.rewardType = r;
            this.weighting = w;
        }
    }

    /**
     * Simple class to encapsulate a range of probability, for use in a Map.
     */
    private class ProbabilityRange {
        final float lowerBound, upperBound;

        ProbabilityRange(float lower, float upper) {
            this.lowerBound = lower;
            this.upperBound = upper;
        }

        public String toString() {
            return "(" + lowerBound + "," + upperBound + ")";
        }
    }

    public int getTotalNumberOfTargets() {
        return totalNumberOfTargets;
    }

    public int getNumberOfQuarryOrbits() {
        return numberOfQuarryOrbits;
    }

    public int getNumberOfBlockerOrbits() {
        return numberOfBlockerOrbits;
    }
}
