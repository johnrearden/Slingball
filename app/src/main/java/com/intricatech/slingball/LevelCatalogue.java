package com.intricatech.slingball;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.intricatech.slingball.LevelConfig.RewardElement;
import static com.intricatech.slingball.LevelConfig.TargetElement;

/**
 * Created by Bolgbolg on 12/07/2017.
 */
public class LevelCatalogue {

    private static LevelCatalogue instance = new LevelCatalogue();

    public static LevelCatalogue getInstance() {
        return instance;
    }

    private Map<Integer, LevelConfig> levelConfigHard, levelConfigNormal, levelConfigEasy;

    static final int MAX_TARGETS_PER_ORBIT = IntRepConsts.MAX_TARGETS_PER_LEVEL;
    static final int MAX_LEVEL_HARD = IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
    static final int MAX_LEVEL_EASY = IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY;
    static final int MAX_LEVEL_NORMAL = IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL;

    private LevelCatalogue() {
        /**
         * ****************************************************************************************
         * **********************************   NORMAL   ******************************************
         * ****************************************************************************************
         */
        levelConfigEasy = new HashMap<>();
        levelConfigEasy.put(0, new LevelConfig(10, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f)
                ))
        ));

        levelConfigEasy.put(1, new LevelConfig(11, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1),
                        new TargetElement(TargetType.REWARDER, 0.2f)

                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(2, new LevelConfig(12, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)

                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(3, new LevelConfig(13, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)

                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(4, new LevelConfig(14, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 2.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)

                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(5, new LevelConfig(15, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(6, new LevelConfig(16, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(7, new LevelConfig(17, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 2.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(8, new LevelConfig(18, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigEasy.put(9, new LevelConfig(20, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 2.0f),
                        new TargetElement(TargetType.DECOYS, 2.0f),
                        new TargetElement(TargetType.REWARDER, 2.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 4.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)

                ))
        ));

        /**
         * ****************************************************************************************
         * **********************************   NORMAL   ******************************************
         * ****************************************************************************************
         */
        levelConfigNormal = new HashMap<>();
        levelConfigNormal.put(0, new LevelConfig(15, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(1, new LevelConfig(15, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                                new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                                new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                                new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                                new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(2, new LevelConfig(10, 3, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(3, new LevelConfig(15, 2, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DARTER, 0.5f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(4, new LevelConfig(15, 2, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),

                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(5, new LevelConfig(15, 2, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(6, new LevelConfig(20, 2, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(7, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))

        ));
        levelConfigNormal.put(8, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(9, new LevelConfig(25, 1, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(10, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(11, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.6f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(12, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 0.5f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(13, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(14, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(15, new LevelConfig(25, 2, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(16, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(17, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(18, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.4f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigNormal.put(19, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 2.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 5.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));

        /**
         * ****************************************************************************************
         * **********************************   HARD   ******************************************
         * ****************************************************************************************
         */
        levelConfigHard = new HashMap<>();
        levelConfigHard.put(0, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(1, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(2, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(3, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(4, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(5, new LevelConfig(25, 1, 4,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(6, new LevelConfig(15, 2, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(7, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(8, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(9, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.15f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(10, new LevelConfig(25, 2, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(11, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(12, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 3.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 3.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(13, new LevelConfig(25, 1, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.4f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f)
                ))
        ));
        levelConfigHard.put(14, new LevelConfig(25, 5, 0,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.1f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(15, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(16, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(17, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.KILLER, 2.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.AUTOPILOT, 1.0f),
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(18, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(19, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 0.5f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 1.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 1.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(20, new LevelConfig(25, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.5f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(21, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(22, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.3f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(23, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(24, new LevelConfig(25, 3, 2,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(25, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SUDDEN_DEATH, 1.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(26, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.DARTER, 1.0f),
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.TADPOLE, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 2.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(27, new LevelConfig(25, 1, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f)
                ))
        ));
        levelConfigHard.put(28, new LevelConfig(25, 2, 3,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.CRUISER, 1.0f),
                        new TargetElement(TargetType.REWARDER, 0.2f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.POWER_UP, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f),
                        new RewardElement(RewardsManager.RewardType.SHIELD, 1.0f)
                ))
        ));
        levelConfigHard.put(29, new LevelConfig(25, 3, 1,
                new ArrayList<>(Arrays.asList(
                        new TargetElement(TargetType.OSCILLATOR, 1.0f),
                        new TargetElement(TargetType.FLICKER, 1.0f),
                        new TargetElement(TargetType.DODGER, 1.0f),
                        new TargetElement(TargetType.KILLER, 1.0f),
                        new TargetElement(TargetType.DECOYS, 1.0f),
                        new TargetElement(TargetType.REWARDER, 1.0f)
                )),
                new ArrayList<>(Arrays.asList(
                        new RewardElement(RewardsManager.RewardType.NO_BLOCKERS, 2.0f),
                        new RewardElement(RewardsManager.RewardType.EXTRA_TIME, 2.0f)
                ))
        ));
    }

    public LevelConfig getLevelConfig(int level, DifficultyLevel diffLevel) {
        switch (diffLevel) {
            case NORMAL: {
                if (level > MAX_LEVEL_NORMAL) {
                    return levelConfigNormal.get(MAX_LEVEL_NORMAL);
                } else {
                    return levelConfigNormal.get(level);
                }
            }
            case HARD: {
                if (level > MAX_LEVEL_HARD) {
                    return levelConfigHard.get(MAX_LEVEL_HARD);
                } else {
                    return levelConfigHard.get(level);
                }
            }

            case EASY: {
                if (level > MAX_LEVEL_EASY) {
                    return levelConfigEasy.get(MAX_LEVEL_EASY);
                } else {
                    return levelConfigEasy.get(level);
                }
            }
            default : {
                return null;
            }
        }
    }


}
