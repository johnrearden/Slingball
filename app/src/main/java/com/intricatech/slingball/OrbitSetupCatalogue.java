package com.intricatech.slingball;

import java.util.HashMap;
import java.util.Map;

public class OrbitSetupCatalogue {
	
	// Singleton instance.
	private static OrbitSetupCatalogue orbitSetupCatalogue = new OrbitSetupCatalogue();
	
	// A map to associate the set of defined orbitDetail objects with their respective levels.
	private Map<Integer, OrbitDetail> orbitDetailMapHard;
	private Map<Integer, OrbitDetail> orbitDetailMapEasy;
	private Map<Integer, OrbitDetail> orbitDetailMapNormal;

	// The total permissible number of targets in any one orbit.
	static final int MAX_TARGETS_PER_ORBIT = IntRepConsts.MAX_TARGETS_PER_LEVEL;
    static final int maxLevelHard = IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
    static int maxLevelEasy = IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY;
	static final int maxLevelNormal = IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL;
		
	// Method returns the appropriate OrbitDetail for the level given as a parameter.
	public OrbitDetail getOrbitDetail(int level, DifficultyLevel difficulty) {
		// If the level is greater than the maximum defined, return the maximum defined instead.

		switch (difficulty) {
			case NORMAL: {
				if (level > maxLevelNormal) {
					return orbitDetailMapNormal.get(maxLevelNormal);
				} else {
					return orbitDetailMapNormal.get(level);
				}
			}
			case HARD: {
				if (level > maxLevelHard) {
					return orbitDetailMapHard.get(maxLevelHard);
				} else {
					return orbitDetailMapHard.get(level);
				}
			}
			
			case EASY: {
				if (level > maxLevelEasy) {
					return orbitDetailMapEasy.get(maxLevelHard);
				} else {
					return orbitDetailMapEasy.get(level);
				}
			}
			default : {
				return null;
			}
		}
	}
	
	// Private constructor.
	private OrbitSetupCatalogue() {
		orbitDetailMapHard = new HashMap<Integer, OrbitDetail>();
		orbitDetailMapHard.put(0, new OrbitDetail(10, 3, 0, 0.8, 0, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapHard.put(1, new OrbitDetail(15, 2, 0, 0, 0.8, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapHard.put(2, new OrbitDetail(15, 3, 1, 0.4, 0.4, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapHard.put(3, new OrbitDetail(15, 3, 1, 0.1, 0.1, 0, 0.6, 0, 0, 0, 0.2));
		orbitDetailMapHard.put(4, new OrbitDetail(15, 3, 1, 0.2, 0.3, 0, 0.3, 0, 0, 0, 0.2));
		orbitDetailMapHard.put(5, new OrbitDetail(15, 3, 1, 0, 0, 0, 0, 0, 0.8, 0, 0.2));
		orbitDetailMapHard.put(6, new OrbitDetail(20, 3, 1, 0.2, 0.2, 0, 0.2, 0, 0.25, 0, 0.15));
		orbitDetailMapHard.put(7, new OrbitDetail(20, 3, 1, 0, 0, 0.75, 0, 0, 0, 0, 0.25));
		orbitDetailMapHard.put(8, new OrbitDetail(20, 3, 1, 0.2, 0, 0.2, 0.2, 0, 0.2, 0, 0.2));
		orbitDetailMapHard.put(9, new OrbitDetail(20, 3, 1, 0, 0., 0, 0, 0.8, 0, 0, 0.2));
		orbitDetailMapHard.put(10, new OrbitDetail(20, 2, 2, 0, 0, 0.3, 0, 0.25, 0.2, 0.1, 0.15));

		orbitDetailMapHard.put(10, new OrbitDetail(3, 2, 0, 1.0, 0, 0, 0, 0, 0, 0, 0));

		orbitDetailMapHard.put(11, new OrbitDetail(20, 2, 2, 0.3, 0.15, 0.1, 0.10, 0, 0.1, 0.1, 0.15));
		orbitDetailMapHard.put(12, new OrbitDetail(20, 2, 2, 0.15, 0.20, 0.15, 0.10, 0, 0.15, 0.1, 0.15));
		orbitDetailMapHard.put(13, new OrbitDetail(20, 2, 2, 0.1, 0.1, 0.1, 0.1, 0.1, 0.15, 0.1, 0.25));
		orbitDetailMapHard.put(14, new OrbitDetail(20, 2, 2, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2));
		orbitDetailMapHard.put(15, new OrbitDetail(21, 2, 2, 0.1, 0.1, 0.15, 0.15, 0.15, 0.1, 0.05, 0.2));
		orbitDetailMapHard.put(16, new OrbitDetail(22, 2, 2, 0.1, 0.1, 0.15, 0.15, 0.15, 0.1, 0.05, 0.2));
		orbitDetailMapHard.put(17, new OrbitDetail(23, 2, 2, 0.1, 0.1, 0.15, 0.15, 0.15, 0.1, 0.05, 0.2));
		orbitDetailMapHard.put(18, new OrbitDetail(24, 2, 2, 0.1, 0.1, 0.15, 0.15, 0.15, 0.1, 0.05, 0.2));
		orbitDetailMapHard.put(19, new OrbitDetail(25, 2, 2, 0.1, 0.1, 0.15, 0.15, 0.15, 0.1, 0.05, 0.2));

		orbitDetailMapNormal = new HashMap<>();
/*
		orbitDetailMapNormal.put(0, new OrbitDetail(10, 1, 0, 0, 0, 0, 0, 0, 0, 1.0, 0));
*/
		orbitDetailMapNormal.put(0, new OrbitDetail(10, 2, 0, 0.8, 0, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(1, new OrbitDetail(11, 2, 0, 0, 0.8, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(2, new OrbitDetail(12, 2, 0, 0.4, 0.4, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(3, new OrbitDetail(13, 2, 1, 0, 0, 0, 0.8, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(4, new OrbitDetail(14, 2, 1, 0.2, 0.3, 0, 0.3, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(5, new OrbitDetail(15, 2, 1, 0, 0, 0, 0, 0, 0.8, 0, 0.2));
		orbitDetailMapNormal.put(6, new OrbitDetail(16, 2, 1, 0.2, 0.2, 0, 0.2, 0, 0.2, 0, 0.2));
		orbitDetailMapNormal.put(7, new OrbitDetail(17, 2, 1, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
		orbitDetailMapNormal.put(8, new OrbitDetail(18, 2, 1, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
		orbitDetailMapNormal.put(9, new OrbitDetail(19, 2, 1, 0.1, 0.1, 0.2, 0.1, 0.1, 0.2, 0, 0.2));
		orbitDetailMapNormal.put(10, new OrbitDetail(20, 2, 1, 0, 0, 0.3, 0, 0, 0.5, 0, 0.2));
		orbitDetailMapNormal.put(11, new OrbitDetail(21, 2, 1, 0.2, 0.2, 0.2, 0.2, 0, 0, 0, 0.2));
		orbitDetailMapNormal.put(12, new OrbitDetail(22, 2, 2, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
		orbitDetailMapNormal.put(13, new OrbitDetail(23, 2, 2, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
		orbitDetailMapNormal.put(14, new OrbitDetail(24, 2, 2, 0.1, 0.1, 0.2, 0.1, 0.1, 0.2, 0, 0.2));
		
		
		orbitDetailMapEasy = new HashMap<Integer, OrbitDetail>();
		orbitDetailMapEasy.put(0, new OrbitDetail(10, 4, 0, 0.8, 0, 0, 0, 0, 0, 0, 0.2));
/*
		orbitDetailMapEasy.put(0, new OrbitDetail(10, 1, 0, 0, 0, 0, 0, 0, 0, 1.0, 0));
*/
		orbitDetailMapEasy.put(1, new OrbitDetail(11, 4, 0, 0, 0.8, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapEasy.put(2, new OrbitDetail(12, 4, 0, 0.4, 0.4, 0, 0, 0, 0, 0, 0.2));
		orbitDetailMapEasy.put(3, new OrbitDetail(13, 3, 1, 0, 0, 0, 0.8, 0, 0, 0, 0.2));
		orbitDetailMapEasy.put(4, new OrbitDetail(14, 3, 1, 0.2, 0.3, 0, 0.3, 0, 0, 0, 0.2));
		orbitDetailMapEasy.put(5, new OrbitDetail(15, 3, 1, 0, 0, 0, 0, 0, 0.8, 0, 0.2));
		orbitDetailMapEasy.put(6, new OrbitDetail(16, 3, 1, 0.2, 0.2, 0, 0.2, 0, 0.2, 0, 0.2));
		orbitDetailMapEasy.put(7, new OrbitDetail(17, 3, 1, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
		orbitDetailMapEasy.put(8, new OrbitDetail(18, 3, 1, 0.1, 0.1, 0, 0.3, 0, 0.3, 0, 0.2));
        orbitDetailMapEasy.put(9, new OrbitDetail(25, 3, 1, 0.1, 0.1, 0.2, 0.1, 0.1, 0.2, 0, 0.2));

	}


	
	// Accessor method for instance.
	public static OrbitSetupCatalogue getInstance() {
		return orbitSetupCatalogue;
	}
	
	// This class encapsulates the fields in TargetManager that are covariant with the current level
	class OrbitDetail {
		private int totalNumberOfTargets;
		private int numberOfQuarryOrbits;
		private int numberOfBlockerOrbits;
		private double cruiserProbability;
		private double darterProbability;
		private double tadpoleProbability;
		private double oscillatorProbability;
		private double dodgerProbability;
		private double flickerProbability;
		private double decoyProbability;
		private double rewarderProbability;
		
		OrbitDetail(int totalNumberOfTargets,
					int numberOfQuarryOrbits, 
				    int numberOfBlockerOrbits,
				    double cruiserProbability,
				    double darterProbability,
				    double tadpoleProbability,
				    double oscillatorProbability,
					double dodgerProbability,
					double flickerProbability,
					double decoyProbability,
					double rewarderProbability) {
			this.totalNumberOfTargets = totalNumberOfTargets;
			this.numberOfQuarryOrbits = numberOfQuarryOrbits;
			this.numberOfBlockerOrbits = numberOfBlockerOrbits;
			this.cruiserProbability = cruiserProbability;
			this.darterProbability = darterProbability;
			this.tadpoleProbability = tadpoleProbability;
			this.oscillatorProbability = oscillatorProbability;
			this.dodgerProbability = dodgerProbability;
			this.flickerProbability = flickerProbability;
			this.decoyProbability = decoyProbability;
			this.rewarderProbability = rewarderProbability;
		}

		public int getNumberOfQuarryOrbits() {
			return numberOfQuarryOrbits;
		}

		public int getNumberOfBlockerOrbits() {
			return numberOfBlockerOrbits;
		}

		public double getCruiserProbability() {
			return cruiserProbability;
		}

		public double getDarterProbability() {
			return darterProbability;
		}

		public double getTadpoleProbability() {
			return tadpoleProbability;
		}

		public double getOscillatorProbability() {
			return oscillatorProbability;
		}

		public double getDodgerProbability() { return dodgerProbability;}

		public double getFlickerProbability() { return flickerProbability;}

		public double getDecoyProbability() {return decoyProbability;}

		public double getRewarderProbability() {return  rewarderProbability;}

		public int getTotalNumberOfTargets() {
			return totalNumberOfTargets;
		}
	}
	
	
}
