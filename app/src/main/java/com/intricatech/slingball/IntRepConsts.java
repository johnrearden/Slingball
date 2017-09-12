package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 27/06/2015.
 */

import static java.lang.Math.PI;

public class IntRepConsts {

    static final boolean IS_RELEASE_VERSION = false;

    static final int STARTING_LEVEL = 1;
    static final int FREQUENCY_OF_ADS = 4;
    static final int DEFAULT_NUMBER_OF_LIVES = 5;

    static final int NUMBER_OF_GAMES_WITHOUT_ADS = 3;
    static final int NUMBER_OF_GAMES_WITH_AUTO_INSTRUCTIONS_SHOW = 3;

    // These fields control the positioning of the main elements of the SurfaceView.
    static final float RATIO_OF_TOP_PANEL_TO_AVAILABLE_SPACE = 0.7f;// 0.7f
    static final float MINIMUM_TOPBOTTOM_PANELS_HEIGHT_RATIO = 0.4f; // 0.4f
    static final float SCREEN_REMAINING_AFTER_OUTER_CIRCLE = 353.0f / 386.0f;

    // These fields control the relative sizes of the elements in the internal representation.
    static final int DIAMETER = 400;
    static final int OUTER_CIRCLE_THICKNESS = 5;
    static final int X_CENTER_OF_ROTATION = (int)(DIAMETER / 2);
    static final int Y_CENTER_OF_ROTATION = X_CENTER_OF_ROTATION;
    static final int OUTER_CIRCLE_RADIUS = (int)((DIAMETER / 2) - (OUTER_CIRCLE_THICKNESS / 2));
    static final int TARGET_THICKNESS = 13; // 14 is nominal

	// fields relating to the collisionMap and fixturesMap.
    static final int FIXTURES_BLUE_FACTOR = 250;
    static final int BACKGROUND_BLUE_FACTOR = 255;
    
    // TIMING FIELDS : (6 is standard)
    static final int NUMBER_OF_UPDATES_PER_CYCLE = 1;

    // TARGETMANAGER FIELDS
    static final int MAX_NUMBER_OF_ORBITS = 5; // Hard max is 20 due to size of blueFactor 8bit. 5 is nominal
    static final int GAP_BETWEEN_ORBITS = 0;
    static final int HIGHEST_DEFINED_LEVEL_HARD = 29;
    static final int HIGHEST_DEFINED_LEVEL_EASY = 9;
    static final int HIGHEST_DEFINED_LEVEL_NORMAL = 19;

    // PHYSICS&BGRENDERER FIELDS:
    static final float TIME_ALLOWED_FOR_EACH_TARGET = 3.5f; // true value is 3.5
    static final float SCORE_VALUE_OF_TIME_LEFT = 1.0f;
    static final float SCORE_VALUE_OF_3_IN_A_ROW = 50.0f;

    // SWINGBALL FIELDS :
    static final float RATIO_OF_HALOED_IMAGE_TO_PLAIN_IMAGE = 1.5f;
    static final float INITIAL_ENERGY = 100.0f;
    static final int NUMBER_OF_TEST_POINTS = 32;
    static final double ORBIT_DISTANCE = 200;
    static final double PREFERRED_ORBIT_RADIUS = 76; // 76
    static final double GRAVCONST = 0.5;
	static final double POWERCONSTANT = 1.6;
    static final int BALL_RADIUS = ((int) ((3 + TARGET_THICKNESS * (MAX_NUMBER_OF_ORBITS - 1.1f)) / 2));
    static final float BALL_SHADOW_DIAMETER = 2 * BALL_RADIUS * 1.1f;
    static final int BALL_PROJECTION_ALPHA = 80;
    static final int NUMBER_OF_PREVIOUS_POSITIONS = 40; // 60
    static final int GAP_BETWEEN_POSITION_RECORDINGS = 2;

    static final double MAXIMUM_BALL_VELOCITY = 1.25;
    static final double ENERGY_LOST_IN_WALL_COLLISION = 10.0;  // was 10.
    static final double ENERGY_LOST_IN_ORBIT = 0.001;
    static final double LEVEL_INCREMENT_FOR_ENERGY_LOSSES = 0.1f;
    static final double LEVEL_MAX_MULTIPLE_OF_ORIGINAL_ENERGY_LOST = 5.0;
    static final int CYCLES_BEFORE_READY_FOR_REVERSING = 360;
    static final long LAG_BETWEEN_HEAVY_ENERGY_LOSSES = 500000000;
    static final int WALL_IS_LIT_COUNTDOWN_INITIAL_VALUE = 10;
    static final int AUTOPILOT_CYCLES = 11 * NUMBER_OF_UPDATES_PER_CYCLE * 60;
    static final int SHIELD_CYCLES = 11 * NUMBER_OF_UPDATES_PER_CYCLE * 60;
    static final int SUDDEN_DEATH_CYCLES = 6 * NUMBER_OF_UPDATES_PER_CYCLE * 60;
    static final float AUTOPILOT_VELOCITY_RATIO = 1.2f;// STANDARD = 1.2f;
    static final int TIME_LIMIT_FOR_NO_FINGER_DOWN = 40;  // was 60
    static final float TIME_FOR_REVERSING_COUNTDOWN = 0.2f;
    static final float ENERGY_THRESHOLD_FOR_NO_POWERUP_REWARD = 60.0f;
    
    // ABSTRACTTARGET FIELDS :
    static final boolean ANTI_ALIASED = true;
    static final int HEALTH_INCREMENT = 1;
    static final float RANGE_TO_DECELERATION = (float)PI / 4;
    static final float TARGET_MAX_VELOCITY = 0.003f; // 0.003f is nominal
    static final float TARGET_VELOCITY_ADDON_FOR_HARD_DIFFICULTY = 0.002f;
    static final float TARGET_VELOCITY_INCREMENT_PER_LEVEL = 0.0004f;
    static final double LIMIT_OF_TARGET_MAX_VELOCITY = TARGET_MAX_VELOCITY
            + TARGET_VELOCITY_INCREMENT_PER_LEVEL * HIGHEST_DEFINED_LEVEL_HARD;
    static final float TARGET_ACCELERATION = TARGET_MAX_VELOCITY / 100; // standard is 200.
    static final double TRACKING_ACCELERATION_MULTIPLIER = 100;
    static final float RATIO_OF_OPAQUE_MASK_TO_TARGET = 1.0f;
    static final int MAX_TARGETS_PER_LEVEL = 25;

    static final float TARGET_AVOIDING_ACCELERATION = TARGET_ACCELERATION * 80;
    static final float TARGET_AVOIDING_MAX_VEL_MULTIPLIER = 8.0f;
    static final float REACTION_DIST_FOR_AVOIDANCE = DIAMETER * 0.18f; // was 0.2

    static final int FLICKER_MIN_PERIOD = 60;
    static final int FLICKER_PERIOD_RANGE = 120;
    static final int FLICKER_DURATION = 40;
    static final float DARKNESS_INCREMENT = 4.0f;
    static final int FLICKER_NUMBER_OF_SPRITES = 3;
    static final int FLICKER_SPRITE_PERSISTENCE = 1;
    static final int FLICKER_NUMBER_OF_SPARK_POINTS = 8;
    static final float FLICKER_BLANK_ARC_SIZE = 0.95f;

    static final int DARTER_MIN_STOPPED_PERIOD = 30;
    static final int DARTER_STOPPED_PERIOD_RANGE = 30;

    static final int DECOY_MAX_POPULATION = 4;

    static final float TADPOLE_REL_INDICATOR_THICKNESS = 0.5f;
    static final float TADPOLE_REL_INDICATOR_LENGTH = 0.85f;

    static final float KILLER_MAXVEL_MULTIPLIER = 3.0f;

    static final float EXTRA_TIME_FOR_DECOYS = 3.0f;
    static final float EXTRA_TIME_FOR_DODGERS = 0.7f;
    static final float EXTRA_TIME_FOR_TADPOLES = 0.4f;
    static final float EXTRA_TIME_FOR_KILLERS = 1.5f;

    static final float REL_SIZE_OF_REWARDER_QUESTION_MARK = 1.5f;
    static final float GHOSTING_OPACITY = 210.0f;
    static final int GHOSTING_CYCLES = 11 * NUMBER_OF_UPDATES_PER_CYCLE * 60;
    // FIREFLY FIELDS :
    static final int FIREFLY_POPULATION = 10;
    static final float FIREFLY_CORE_DIAMETER = 4;
    static final float FIREFLY_OUTER_DIAMETER = 8;
    static final float FIREFLY_INITIAL_VELOCITY = 0.5f;
    static final int FIREFLY_LOOSE_COUNTDOWN_INITIAL_VALUE = NUMBER_OF_UPDATES_PER_CYCLE * 20;
    static final int FIREFLY_FADING_COUNTDOWN_INITIAL_VALUE = NUMBER_OF_UPDATES_PER_CYCLE * 20;

    // READOUT PANEL CONFIGURATION :

    static final float CURVED_DISPLAYS_OFFSET = 0.02f;
    // Score readout : y dimension sizes are a proportion of the available readout panel, not the screen.
    static final int SCORE_NUMBER_OF_DIGITS = 5;
    static final float ROUNDRECT_RADIUS = 0.02f;
    static final float SCORE_XPOS = 0.7f;
    static final float SCORE_YPOS = 0.3f;
    static final float SCORE_XSIZE = 0.25f;
    static final float SCORE_YSIZE = 0.2f;
    static final float SCORE_BORDER_THICKNESS = 0.012f;
    static final float SCORE_DIVIDER_THICKNESS = 0.003f;
    static final float HIGHSCORE_YPOS = 0.1f;
    static final float HIGHSCORE_RELATIVE_SIZE = 0.6f;
    static final float HEART_STAR_HEIGHT_RATIO = 0.7f;
    static final float HEART_AND_STAR_DISPLAY_YPOS = 0.55f;
    static final float HEART_AND_STAR_NUMBER_REL_SIZE = 0.9f;
    static final float HEART_AND_STAR_HOR_SQUEEZE = 0.7f;
    static final float HEART_AND_STAR_REL_Y_GAP = 0.2f;

    // Countdown readout : y dimension sizes are a proportion of the available readout panel, not the screen.
    static final float COUNTDOWN_XPOS = 0.15f;
    static final float COUNTDOWN_YPOS = 0.15f;
    static final float COUNTDOWN_XSIZE = 0.27f;
    static final float COUNTDOWN_YSIZE = 0.20f;
    static final float COUNTDOWN_BORDER_THICKNESS = SCORE_BORDER_THICKNESS;
    static final float COUNTDOWN_HALO_BLUR_THICKNESS = 0.01f;
    static final float COUNTDOWN_DIVIDER_THICKNESS = 0.003f;
    static final int MAXIMUM_TIME_ALLOWED_IN_SECONDS = 120;
    static final float COUNTDOWN_INTERNAL_BORDER = 0.03f;   // The relative size of the border around a digit
    static final float COUNTDOWN_FADING_OUT_INITIAL_VALUE = 255.0f;
    static final float COUNTDOWN_FADING_OUT_DECREMENT = 2.0f;

    // EnergyBar fields :
    static final float ENERGY_BAR_START_ANGLE = (float)(-PI * 26 / 32); // -PI * 26 / 32
    static final float ENERGY_BAR_FINISH_ANGLE = (float)(-PI * 21 / 32); // -PI * 21 / 32
    static final float ENERGY_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER = 0.035f;//0.04f
    static final float ENERGY_BAR_INCREMENT_PER_CYCLE = 1.0f;
    static final float ENERGY_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT = 1.18f;//1.13f
    static final float ENERGY_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS = 2.1f; //1.6f
    static final float ENERGY_BAT_THICKNESS_OF_BLACK_REL_TO_OWN_THICKNESS = 1.4f;
    static final float ENERGY_BAR_RELATIVE_THICKNESS_OF_BLANK = 1.2f;

    // RemainingTargetBar fields :
    static final float REM_TAR_BAR_START_ANGLE = (float)(-PI * 17 / 32);  // -PI * 17 / 32
    static final float REM_TAR_BAR_FINISH_ANGLE = (float)(-PI * 3 / 16);  // -PI * 3 / 16
    static final float REM_TAR_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER = ENERGY_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER;
    static final float REM_TAR_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT = ENERGY_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT;
    static final float REM_TAR_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS = ENERGY_BAR_THICKNESS_OF_BACKGROUND_RELATIVE_TO_OWN_THICKNESS;

    // HitRate fields.

    static final float HITRATE_BAR_START_ANGLE = (float) (PI * 26 / 32); // 23/32
    static final float HITRATE_BAR_FINISH_ANGLE = (float) (PI * 21 / 32); // 15/32
    static final float HITRATE_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER = ENERGY_BAR_THICKNESS_RELATIVE_TO_CIRCLE_DIAMETER * 0.6f;
    static final float HITRATE_BAR_RATIO_OF_ARCRECT_TO_OUTERMOST_TARGET_RECT = 1.14f; // 1.45f
    static final float HITRATE_HIGHEST_POSSIBLE_HITRATE = 1.0f;
    static final float HITRATE_BAR_RELATIVE_THICKNESS_OF_BLANK = ENERGY_BAR_RELATIVE_THICKNESS_OF_BLANK;

    // LevelIndicator fields :
    static final float LEVEL_INDICATOR_ANGLE = ((float) (-PI * 19 / 32));
    static final float LEVEL_INDICATOR_RADIUS_RELATIVE_TO_CIRCLE = 1.35f; // 1.16f
    static final float LEVEL_INDICATOR_DIAMETER_RATIO_TO_CIRCLE = 0.17f; // 0.12f
    static final float LEVEL_INDICATOR_RATIO_OF_GLYPHHEIGHT_TO_OVERALL_HEIGHT = 0.75f;

    // Score bubble pool
    static final int SCORE_BUBBLE_POOL_SIZE = 8;
    static final int SCORE_BUBBLE_ALPHA_DECREMENT = 8;
    static final float SCORE_BUBBLE_DIAMETER_TO_CIRCLE_RATIO = 0.11f;
    static final int SCORE_BUBBLE_DISPLAY_FRAMES = 15;

    // Reward Display.
    static final float REWARD_TAG_THICKNESS_RATIO = 0.14f; //0.11f
    static final float REWARD_TAG_INNER_THICKNESS_RATIO = 0.5f;
    static final float REWARD_TAG_GAP_TO_CIRCLE_RATIO = 1.15f; // was 1.05
    static final float REWARD_TAG_ANGULAR_SIZE = 0.73f; // was .67
    static final int REWARD_EXTRA_TIME = 10;
    static final int REWARD_EXTRA_TIME_LEVEL_20 = 20;

    // Flying Heart.
    static final float FLYING_HEART_ACCELERATION = 0.2f;
    static final float FLYING_HEART_MAX_VEL = 20.0f;
    static final float FLYING_HEART_BASE_PROBABILITY = 0.05f; // 0.05f
    static final float FLYING_HEART_DIRECTION_INCREMENT = 0.07f;


    // Other stuff.
    static final String RUBY = "AmNL3XcZsF8TuYbooTLbDlEA+GIRiBZiDx9wi0wFnnlu";
    static final String LISA = "z7paiaHB8Sn6Nin/44+lZ8MpK9yb9Cnv8t9M+pdma6xk";
    static final String JESSE = "sZWIH4uSlW4UvFKAjWhP+uScSWkGZPZr0ZQIDAQAB";
    static final int SIZE_OF_TOUCH_ARRAY = 5;

    // Static utility methods.
    static double calculateTurnRate(double maxVel) {
        return PI * PREFERRED_ORBIT_RADIUS / (2 * maxVel);
    }
}
