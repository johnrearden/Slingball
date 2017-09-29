package com.intricatech.slingball;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import static com.intricatech.slingball.MessageBoxManager.MessageType.AUTOPILOT;
import static com.intricatech.slingball.MessageBoxManager.MessageType.NEW_LEVEL_BOX;
import static com.intricatech.slingball.MessageBoxManager.MessageType.SHIELD;

/**
 * Created by Bolgbolg on 05/01/2016.
 */
public class PhysicsAndBGRenderer implements
        Runnable,
        DifficultyLevelObserver,
        MediaPlayer.OnPreparedListener {

    GameActivity currentActivity;
    GameSurfaceView gameSurfaceView;

    int pointerCount;

    public static final boolean DEBUG = false;

    boolean continueGameAfterAd;
    boolean shouldDisplayAd;
    boolean adsActivated;

    //temp - for screenshots.
    boolean freezeOnNextFrame;

    public static String TAG = "PhysicsAndBGRenderer";
    SharedPreferences permanentPlayerData, levelData, audioPrefs, adData;
    SharedPreferences.Editor permanentPlayerDataEditor, levelDataEditor, audioPrefsEditor;
    private static final String PERMANENT_PLAYER_DATA = "PERMANENT_PLAYER_DATA";
    private static final String LEVEL_DATA = "LEVEL_DATA";
    static final String AUDIO_PREFERENCES = "AUDIO";
    static final String AD_DATA = "AD_DATA";
    static final String SHOW_ADS = "SHOW_ADS";
    String EASY_COMPLETED;
    String NORMAL_COMPLETED;
    String HARD_COMPLETED;
    static String HARD_LEVEL_AVAILABLE_TAG;
    String LIVES_REMAINING_TAG;
    String levelNumberString, ballEnergyString, currentRewardString, gameScoreString, difficultyString,
        timeRemainingString, targetsRemainingString, resumeAllowedString;
    float lastSavedTime;
    int lastGameTargetsRemainging;

    private Resources resources;
    SoundManager soundManager;
    boolean musicPrepared;

    Vibrator vibrator;
    ScoreDisplayer scoreDisplayer;
    CountdownTimer countdownTimer;
    MessageBoxManager messageBoxManager;
    BonusDisplayer bonusDisplayer;
    private float timeForEachTarget;
    private float scoreValueOfTimeLeft;
    MessageBoxManager.MessageType gameOverMessageType;
    HitRateDisplayer hitRateDisplayer;

    public CallbackStats callbackStats;
    public int physicsUpdateMisses;
    private boolean startNewGame;
    boolean resumingGame;
    boolean gameOver;
    private int startingLevelNumber;
    private boolean interLevelDisplayIsComplete;
    boolean lastLevelMessageComplete;
    boolean shouldShowDiffLevCompleteAndFinish;
    float timeRemainingAfterLevel;
    int bestRunOfHits;
    int thisRunOfHits;
    int numberOfStars;
    int numberOfMisses;
    int totalNumberOfStars;
    float averageNumberOfStars;
    int livesRemaining;
    float levelHitRate;
    private int numberOfLevelsStarted;

    boolean redrawLevelIndicator;
    int frameCounter;

    @Override
    public void onPrepared(MediaPlayer mp) {
        musicPrepared = true;
    }

    public enum GameState {
        LEVEL_RUNNING,
        PREP_FOR_NEXT_LEVEL,
        PAUSING_FOR_INSTRUCTIONS,
        SHOWING_LEVEL_SUMMARY,
        GAME_OVER,
        RESTARTING_AFTER_GAME_OVER
    }

    enum GameOverCause {
        OUT_OF_TIME,
        OUT_OF_ENERGY
    }
    GameOverCause gameOverCause;
    public GameState gameState;

    private int xCenterOfRotation, yCenterOfRotation;   // The center of rotation of the internal bitmap
    private GraphicsFieldDump graphicsFieldDump;        // The fields needed to draw the gameScreen
    LevelManager levelManager;
    LevelCatalogue levelCatalogue;
    Swingball swingball;
    TargetManager targetManager;
    Fireflies fireflies;
    FlyingHeart flyingHeart;
    EnergyBar energyBar;
    RemainingTarBar remTarBar;
    LevelIndicator levelIndicator;
    RewardsManager rewardsManager;
    ScoreBubblePool scoreBubblePool;
    ControlButton controlButton;
    TapToContinueMonitor gameOverTTCMonitor;
    CircleHighlighter circleHighlighter;

    private int numberOfOrbits;
    DifficultyLevelDirector difficultyLevelDirector;
    InternalCollisionMap internalCollisionMap;

    private PlayAreaInfo playAreaInfo;                      // Pointer to GameActivity's playAreaInfo. Read only.


    private Bitmap currentBackBuffer;               // Pointer to the current buffer being used.
    boolean usingBB1;
    private Canvas currentBackBufferCanvas;         // Pointer to the current canvas for this buffer.
    Bitmap[] scoreGlyphs;
    Bitmap digitsSource;
    Canvas canvas;                                  // Canvas for drawing on this bitmap.
    Canvas backBuffer1Canvas, backBuffer2Canvas;
    Paint textPaint, textPaint2;
    Paint energyPaint;
    Paint cleanDirtyAreaPaint;

    boolean fingerDown;                     // The only user interaction -
                                                    // whether or not the user is pressing the gravity button.
    boolean fingerDownOverride;
    private float[] touchX, touchY;
    private int cyclesSinceLastFingerDown;
    private int reverseAllowedCountdown;
    private static final int REVERSE_ALLOWED_COUNTDOWN_INIT_VAL = (int) (60.0f * IntRepConsts.TIME_FOR_REVERSING_COUNTDOWN);

    private volatile boolean continueRunningPhysicsThread;   // True while Activity is in active phase of lifecycle.

    private volatile boolean startGeneratingNextFrame;       // run() waits for a trigger to start each frame - trigger
                                                    // is provided to GameActivity by Choreographer.

    int numberOfConsecutiveHits;                    // Counts the number of successive hits
                                                    // without a full rotation of the ball.
    float gameScore;
    float gameScoreAtStartOfCurrentLevel;
    boolean shouldRestartAfterGameOver;
    int highScore;

    private int numberOfUpdates;

    private long choreographerCallBackTime;
    private long timeLeftForFrame;
    public static long MAX_TIME_AVAILABLE_NANOS = 16700000;
                                                    // The maximum time available (less the lag from the Choreographer
                                                    // callback) to generate the frame and render the background.
    public static long TIME_NEEDED_FOR_PHYSICS_NANOS = 2000000;
    public static long TIME_NEEDED_FOR_RENDERING_NANOS = 500000;
    boolean bufferReadyForGameObjects;

    public void setPlayAreaInfo(PlayAreaInfo pai) {
        this.playAreaInfo = pai;
    }

    private Paint circleGlowPaint;
    private BlurMaskFilter circleGlowBlurFilter;

    String TOTAL_NMBR_STARS;

    private RectF controlButton1Bounds, controlButton2Bounds;
    private boolean controlButton1Pressed, controlButton2Pressed;

    public PhysicsAndBGRenderer(GameSurfaceView gsv,
                                Context context,
                                float musicVolume,
                                float effectsVolume,
                                boolean isVibrationOn,
                                int startingLevelNumber) {

        currentActivity = (GameActivity) context;
        soundManager = currentActivity.soundManager;
        levelCatalogue = LevelCatalogue.getInstance();
        //this.gameSurfaceView = gsv;

        this.startingLevelNumber = startingLevelNumber;
        levelData = context.getSharedPreferences(LEVEL_DATA, Context.MODE_PRIVATE);
        levelDataEditor = levelData.edit();
        adData = context.getSharedPreferences(AD_DATA, Context.MODE_PRIVATE);
        permanentPlayerData = context.getSharedPreferences(PERMANENT_PLAYER_DATA, Context.MODE_PRIVATE);
        permanentPlayerDataEditor = permanentPlayerData.edit();
        audioPrefs = context.getSharedPreferences(AUDIO_PREFERENCES, Context.MODE_PRIVATE);
        audioPrefsEditor = audioPrefs.edit();
        LIVES_REMAINING_TAG = context.getResources().getString(R.string.lives_remaining_tag);
        HARD_LEVEL_AVAILABLE_TAG = context.getResources().getString(R.string._hard_level_available);
        EASY_COMPLETED = "EASY_COMPLETED";
        NORMAL_COMPLETED = "NORMAL_COMPLETED";
        HARD_COMPLETED = "HARD_COMPLETED";
        livesRemaining = permanentPlayerData.getInt(LIVES_REMAINING_TAG,
                IntRepConsts.DEFAULT_NUMBER_OF_LIVES);
        adsActivated = adData.getBoolean(SHOW_ADS, true);

        if (DEBUG) {
            callbackStats = new CallbackStats();
        }
        frameCounter = 0;

        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        playAreaInfo = gsv.playAreaInfo;
        resources = gsv.resources;

        TOTAL_NMBR_STARS = resources.getString(R.string.total_number_of_stars);

        levelNumberString = resources.getString(R.string.level_data_level_number);
        ballEnergyString = resources.getString(R.string.level_data_ball_energy);
        currentRewardString = resources.getString(R.string.level_data_current_reward);
        gameScoreString = resources.getString(R.string.level_data_game_score);
        difficultyString = resources.getString(R.string.difficulty_string);
        timeRemainingString = "CLOCK_TIME";
        targetsRemainingString = "NUMBER_OF_TARGETS";
        resumeAllowedString = "RESUME_ALLOWED";

        digitsSource = BitmapFactory.decodeResource(resources, R.drawable.digitsinprogress);
        Log.d(TAG, "digitsSource size = " + String.valueOf(digitsSource.getWidth() * digitsSource.getHeight() * 4));
        difficultyLevelDirector = gsv.difficultyLevelDirector;
        registerWithDifficultyLevelDirector();
        startNewGame = true;
        usingBB1 = true;

        xCenterOfRotation = IntRepConsts.X_CENTER_OF_ROTATION;
        yCenterOfRotation = IntRepConsts.Y_CENTER_OF_ROTATION;
        numberOfUpdates = IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;

        levelManager = new LevelManager(startingLevelNumber);

        //soundManager = new SoundManager(context, this, musicVolume, effectsVolume);
        messageBoxManager = new MessageBoxManager(resources, levelManager);

        gameState = GameState.PREP_FOR_NEXT_LEVEL;

        interLevelDisplayIsComplete = false;
        lastLevelMessageComplete = false;
        gameOver = false;
        countdownTimer = new CountdownTimer(resources);
        timeRemainingAfterLevel = 0;
        scoreBubblePool = new ScoreBubblePool(digitsSource);
        bonusDisplayer = new BonusDisplayer(resources);
        circleHighlighter = new CircleHighlighter(resources);

        swingball = new Swingball(
                IntRepConsts.BALL_RADIUS,
                xCenterOfRotation,
                yCenterOfRotation,
                levelManager,
                difficultyLevelDirector,
                vibrator,
                isVibrationOn,
                soundManager,
                messageBoxManager,
                this);

        gameScore = 0;
        highScore = loadHighScore();
        scoreDisplayer = new ScoreDisplayer(digitsSource, resources, highScore);
        flyingHeart = new FlyingHeart(resources, scoreDisplayer, this);
        targetManager = new TargetManager(
                levelManager, difficultyLevelDirector, soundManager, messageBoxManager, flyingHeart);
        targetManager.levelPhase = TargetManager.LevelPhase.READY_FOR_NEW_LEVEL;

        rewardsManager = new RewardsManager(
                this,
                resources,
                swingball,
                targetManager,
                countdownTimer,
                soundManager,
                levelManager,
                difficultyLevelDirector);

        controlButton = new ControlButton(resources, this);
        fireflies = new Fireflies(playAreaInfo, targetManager, resources);
        hitRateDisplayer = new HitRateDisplayer(resources);

        energyBar = new EnergyBar(resources);
        remTarBar = new RemainingTarBar(levelManager, difficultyLevelDirector ,resources);
        levelIndicator = new LevelIndicator(resources, startingLevelNumber, digitsSource);

        redrawLevelIndicator = false;

        numberOfOrbits = targetManager.maxNumberOfTargets;

        internalCollisionMap = new InternalCollisionMap();
        graphicsFieldDump = new GraphicsFieldDump(numberOfOrbits);
        scoreGlyphs = new Bitmap[scoreDisplayer.getNumberOfGlyphs()];

        canvas = null;

        textPaint = new Paint();
        textPaint.setARGB(255, 255, 255, 255);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint2 = new Paint();
        textPaint2.setARGB(255, 0, 255, 0);
        textPaint2.setTextSize(25);

        energyPaint = new Paint();
        energyPaint.setARGB(255, 255, 255, 255);

        cleanDirtyAreaPaint = new Paint();
        cleanDirtyAreaPaint.setARGB(255, 0, 0, 0);
        cleanDirtyAreaPaint.setStyle(Paint.Style.FILL);

        circleGlowPaint = new Paint();
        circleGlowPaint.setARGB(255, 100, 100, 255);
        circleGlowPaint.setStyle(Paint.Style.STROKE);

        bufferReadyForGameObjects = false;

        fingerDown = false;
        fingerDownOverride = true;
        cyclesSinceLastFingerDown = 0;
        reverseAllowedCountdown = REVERSE_ALLOWED_COUNTDOWN_INIT_VAL;
        touchX = new float[5];
        touchY = new float[5];
        numberOfConsecutiveHits = 0;
        gameScore = 0;
        totalNumberOfStars = 0;
        averageNumberOfStars = 0.0f;
        numberOfLevelsStarted = 0;
        setContinueRunningPhysicsThread(false);
        difficultyLevelDirector.updateObservers();

        controlButton1Bounds = new RectF();
        controlButton2Bounds = new RectF();
        controlButton1Pressed = false;
        controlButton2Pressed = false;
    }

    class CallbackStats {
        int numberOfCallbacks;
        float averageCallbackPeriod;
        float totalPeriod;

        long currentCallbackPeriod;
        long currentCallbackTime;
        long previousCallbackTime;

        int numberOfOutliers;

        void logCallbackTime(long time) {
            currentCallbackTime = time;
            numberOfCallbacks++;
            currentCallbackPeriod = currentCallbackTime - previousCallbackTime;
            totalPeriod += currentCallbackPeriod;
            averageCallbackPeriod = totalPeriod / numberOfCallbacks;

            if (Math.abs(averageCallbackPeriod - currentCallbackPeriod) > (averageCallbackPeriod) * 0.1) {
                numberOfOutliers++;
            }

            previousCallbackTime = currentCallbackTime;
        }
    }

    public void run() {

        outerloop :
        while (continueRunningPhysicsThread) {
            while (!startGeneratingNextFrame) {
                try {
                    Thread.sleep(0, 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d(getClass().getSimpleName(), "waiting in while(!startGeneratingNextFrame");
            }
            startGeneratingNextFrame = false;
            frameCounter++;
            if (DEBUG) {
                Log.d(TAG, "-------------Start of frame");
            }
            // If the activity has been restarted from the launch screen (after a gameOver event),
            // all values need to be re-initialized.
            if(startNewGame) {
                levelManager.setLevel(startingLevelNumber);
                messageBoxManager.messageMap.get(MessageBoxManager.MessageType.NEW_LEVEL_BOX).on = true;
                startNewGame = false;
            }
            if (DEBUG) {
                callbackStats.logCallbackTime(choreographerCallBackTime);
            }

            // Begin physics update, as long as there is time. If there is insufficient time, GameSurfaceView
            // will just use the same values for a second frame.
            long timeUsedSoFar, start;
            if (DEBUG) {
                timeUsedSoFar = System.nanoTime() - choreographerCallBackTime;
                timeLeftForFrame = MAX_TIME_AVAILABLE_NANOS - timeUsedSoFar;
                Log.d(TAG, "Time since choreographer callback = " + String.format("%,d", timeUsedSoFar));
                Log.d(TAG, "Time left before physics update started = " + String.format("%,d", timeLeftForFrame));
            }

            if (DEBUG && timeLeftForFrame < TIME_NEEDED_FOR_PHYSICS_NANOS) {
                if (DEBUG) {
                    Log.d(TAG, "Not enough time for physics update");
                }
                physicsUpdateMisses++;
                continue;
            }

            if (musicPrepared && !soundManager.mediaPlayer1.isPlaying()) {
                soundManager.playMusic();
            }
            switch (gameState) {

                case LEVEL_RUNNING: {

                    // Start the timer if it is not already running.
                    if (!countdownTimer.running) {
                        countdownTimer.start();
                        countdownTimer.update();
                    }

                    // If Autopilot has ended, leaving the fingerDownOverride on, check if the finger is down
                    // If so, release the override.
                    if (fingerDown) {
                        fingerDownOverride = false;
                    }
                    // Check if all targets have been destroyed. If so, prepare for the next level.
                    if (targetManager.levelCompleteTidyingUpPersistentFlag) {
                        soundManager.stopTimerAlert();
                        levelHitRate = hitRateDisplayer.currentHitrate * 10.0f;
                        hitRateDisplayer.resetHitrateCounter();
                        if (swingball.autopilot == Swingball.Autopilot.ON) {
                            swingball.turnOffAutopilot(false);
                            messageBoxManager.messageMap.get(AUTOPILOT).on = false;
                        }
                        if (swingball.shield == Swingball.Shield.ON) {
                            swingball.turnOffShield();
                            messageBoxManager.messageMap.get(SHIELD).on = false;
                        }
                        if (swingball.suddenDeath == Swingball.SuddenDeath.ON) {
                            swingball.turnOffSuddenDeath();
                        }
                        if (targetManager.blockersAreGhostedOut) {
                            targetManager.turnOnBlockers();
                        }
                        targetManager.levelCompleteTidyingUpPersistentFlag = false;
                        interLevelDisplayIsComplete = false;
                        fingerDownOverride = true;

                        timeRemainingAfterLevel = countdownTimer.getDisplayedTime();
                        countdownTimer.stop();
                        soundManager.stopTimerAlert();
                        countdownTimer.startFadingOut();

                        checkIfIsLastLevel();

                        levelIndicator.showBlank();
                        levelManager.setLevel(levelManager.getLevel() + 1);

                        shouldDisplayAd = false;
                        checkAdsActivated();

                        if (numberOfLevelsStarted % IntRepConsts.FREQUENCY_OF_ADS == 0
                                && adsActivated && !shouldShowDiffLevCompleteAndFinish) {
                            shouldDisplayAd = true;
                        }

                        messageBoxManager.killAllMessages();

                        gameState = GameState.SHOWING_LEVEL_SUMMARY;
                        numberOfStars = 0;
                        if (timeRemainingAfterLevel > 5.0f) {
                            numberOfStars++;
                        }
                        if (numberOfMisses == 0) {
                            numberOfStars += 3;
                            soundManager.playYoureOnFire();
                        } else if (numberOfMisses < 20) {
                            numberOfStars += 2;
                        } else if (numberOfMisses < 10) {
                            numberOfStars++;
                        }
                        if (levelHitRate > 3) {
                            numberOfStars++;
                        }
                        if (numberOfStars > 5) {
                            numberOfStars = 5;

                        }
                        totalNumberOfStars += numberOfStars;
                        averageNumberOfStars = (float) totalNumberOfStars / levelManager.getLevel();
                        int bonus = calculateLevelBonus(timeRemainingAfterLevel, numberOfMisses, bestRunOfHits);
                        bonusDisplayer.configureDisplayAndMakeVisible(
                                levelHitRate,
                                bestRunOfHits,
                                timeRemainingAfterLevel,
                                numberOfMisses,
                                numberOfStars,
                                bonus);
                        gameScore += bonus;
                        numberOfMisses = 0;
                        numberOfStars = 0;
                        bestRunOfHits = 0;

                        break;
                    }

                    // Show level20 message if appropriate.
                    if (
                            messageBoxManager.messageMap.get(MessageBoxManager.MessageType.NEW_LEVEL_BOX).on == false
                            && levelManager.getLevel() == difficultyLevelDirector.getLastLevel()
                            && !lastLevelMessageComplete
                            && difficultyLevelDirector.getDiffLev() == DifficultyLevel.HARD) {
                        messageBoxManager.messageMap.get(MessageBoxManager.MessageType.LAST_LEVEL).on = true;
                        lastLevelMessageComplete = true;
                    }

                    // If the time remaining is less than countdownTimer's threshold, sound the alarm. Otherwise,
                    // call soundManager.stopTimeAlert(). It's easiest to turn it off here in the event that a time
                    // bonus takes the time back up over the threshold, and documentation says it has no effect if
                    // the clip is not playing, so happy days.
                    if (countdownTimer.getDisplayedTime() < CountdownTimer.RED_THRESHOLD) {
                        soundManager.playTimerAlert();
                    } else soundManager.stopTimerAlert(); // This is causing all the blue log calls?

                    // Check if timeRemaining is zero. If so, switch to gameOver.
                    if (countdownTimer.getDisplayedTime() <= 0) {
                        if (DEBUG) {
                            Log.d(TAG, "getDisplayedTime() == 0, switching to gameOver");
                        }
                        gameState = GameState.GAME_OVER;
                        gameOverCause = GameOverCause.OUT_OF_TIME;
                        saveHighScore();
                        soundManager.stopTimerAlert();
                        soundManager.playSoClose();
                        soundManager.playEvilLaugh();
                        countdownTimer.stop();
                        gameOverTTCMonitor = new TapToContinueMonitor();
                        break;
                    }

                    // Check if swingball.energy is zero. If so, switch to gameOver.
                    if (swingball.outOfEnergy == true) {
                        if (DEBUG) {
                            Log.d(TAG, "energy == 0, switching to gameOver");
                        }
                        gameState = GameState.GAME_OVER;
                        gameOverCause = GameOverCause.OUT_OF_ENERGY;
                        saveHighScore();
                        soundManager.stopTimerAlert();
                        soundManager.playSoClose();
                        soundManager.playEvilLaugh();
                        countdownTimer.stop();
                        numberOfConsecutiveHits = 0;
                        gameOverTTCMonitor = new TapToContinueMonitor();
                        break;
                    }
                    break;
                }

                case SHOWING_LEVEL_SUMMARY: {
                    if (bonusDisplayer.displayState == BonusDisplayer.DisplayState.READY_FOR_AD) {
                        if (shouldDisplayAd) {
                            continueGameAfterAd = true;
                            gameScoreAtStartOfCurrentLevel = gameScore;
                            displayAd(); // This causes gameActivity to pause.
                        } else {
                            continueGameAfterAd = false;
                            bonusDisplayer.displayState = BonusDisplayer.DisplayState.DISAPPEARING;
                        }
                    }
                    if (bonusDisplayer.displayState == BonusDisplayer.DisplayState.OFF) {
                            gameState = GameState.PREP_FOR_NEXT_LEVEL;
                    }
                    if (bonusDisplayer.displayState == BonusDisplayer.DisplayState.DISAPPEARING) {
                        if (shouldShowDiffLevCompleteAndFinish) {
                            currentActivity.displayDiffLevCompleteFragment(difficultyLevelDirector.getDiffLev());
                        }
                    }
                    bonusDisplayer.updateDisplay(fingerDown);
                    break;
                }

                case PREP_FOR_NEXT_LEVEL: {
                    // If the targetManager phase is READY_FOR_NEW_LEVEL, start a new level.
                    if (targetManager.levelPhase != TargetManager.LevelPhase.LEVEL_COMPLETE_TIDYING_UP
                        && interLevelDisplayIsComplete) {
                        countdownTimer.fadingOut = CountdownTimer.FadingOut.OFF;
                        countdownTimer.alphaPaintValue = 255;

                        fireflies.turnOffAll();
                        continueGameAfterAd = false;

                        if (resumingGame) {
                            if (levelManager.getLevel() < difficultyLevelDirector.getLastLevel()
                                    && difficultyLevelDirector.getDiffLev() != DifficultyLevel.HARD) {
                                countdownTimer.setTime(lastSavedTime);
                            } else {
                                countdownTimer.setTime(60.0f);
                            }
                            targetManager.targetsRemaining = lastGameTargetsRemainging;
                            resumingGame = false;
                        } else {
                            if (levelManager.getLevel() < difficultyLevelDirector.getLastLevel()
                                    && difficultyLevelDirector.getDiffLev() != DifficultyLevel.HARD) {
                                countdownTimer.setTime(calculateTimeForLevel(levelManager.getLevel()));
                            } else {
                                countdownTimer.setTime(60.0f);
                            }
                        }
                        averageNumberOfStars = (float) totalNumberOfStars / levelManager.getLevel();

                        countdownTimer.start();
                        countdownTimer.update();

                        levelIndicator.updateLevelIndicator(levelManager.getLevel() + 1);
                        redrawLevelIndicator = true;
                        targetManager.setLevelPhase(TargetManager.LevelPhase.LEVEL_IN_PROGRESS);
                        targetManager.populateOrbits();
                        remTarBar.updateRemainingTarBar(targetManager.targetsRemaining);
                        numberOfConsecutiveHits = 0;
                        hitRateDisplayer.resetHitrateCounter();
                        gameState = GameState.LEVEL_RUNNING;
                        numberOfLevelsStarted++;
                        //fingerDownOverride = false;
                        messageBoxManager.messageMap.get(MessageBoxManager.MessageType.NEW_LEVEL_BOX).on = true;
                        gameScoreAtStartOfCurrentLevel = gameScore;

                        break;
                    }
                    // Otherwise, check that the countdown has completed running to zero and that the next level
                    // information has been displayed. If this is done, continue with the next level.
                    if (/*countdownTimer.runningToZero == CountdownTimer.RunningToZero.COMPLETE*/
                            countdownTimer.fadingOut == CountdownTimer.FadingOut.COMPLETE
                            && messageBoxManager.messageMap.get(MessageBoxManager.MessageType.TIME_BONUS_BOX).on == false
                            && messageBoxManager.messageMap.get(MessageBoxManager.MessageType.GET_READY).on == false
                            && messageBoxManager.messageMap.get(MessageBoxManager.MessageType.NEW_LEVEL_BOX).on == false
                            ) {
                        countdownTimer.fadingOut = CountdownTimer.FadingOut.OFF;
                        interLevelDisplayIsComplete = true;
                    }
                    break;
                }
                case GAME_OVER: {
                    fingerDownOverride = true;

                    countdownTimer.stop();
                    numberOfMisses = 0;
                    messageBoxManager.messageMap.get(MessageBoxManager.MessageType.GAME_OVER_TIME_UP).on = false;
                    messageBoxManager.messageMap.get(MessageBoxManager.MessageType.GAME_OVER_OUT_OF_ENERGY).on = false;
                    //resetLevelData();
                    targetManager.forceEndOfLevel();
                    gameOver = true;
                    gameState = GameState.RESTARTING_AFTER_GAME_OVER;
                    saveLevelDataForRestartAfterShop();
                    currentActivity.showGameOverDialog(1, livesRemaining, gameOverCause);
                    break;
                }

                case RESTARTING_AFTER_GAME_OVER : {
                    if (shouldRestartAfterGameOver) {
                        gameScore = gameScoreAtStartOfCurrentLevel;
                        scoreDisplayer.setScoreWithoutScrolling((int) gameScore);
                        swingball.energy = IntRepConsts.INITIAL_ENERGY;
                        swingball.outOfEnergy = false;
                        rewardsManager.cancelAllRewards();
                        messageBoxManager.messageMap.get(NEW_LEVEL_BOX).on = true;
                        fireflies.turnOffAll();
                        levelManager.setLevel(levelManager.getLevel());
                        shouldRestartAfterGameOver = false;
                        //queryIfShouldDisplayAdAfterGameOverResume();
                        //Log.d(TAG, "progression beyond call to queryIfShouldDisplayAdAfterGameOverResume()");
                        gameState = GameState.PREP_FOR_NEXT_LEVEL;
                    }
                    break;
                }
            }

            for (int i = 0; i < numberOfUpdates; i++) {

                // Call the updateDifficultyDependents methods for the TargetManager and the Swingball.
                gameScore += targetManager.updateTargetManager(
                        swingball,
                        scoreBubblePool,
                        numberOfConsecutiveHits,
                        fireflies);
                // if the time limit since the last fingerDown has been exceeded, turn on the override. This is
                // to prevent the player letting the ball destroy all the targets on its own on easier levels.
                if (!fingerDown) {
                    cyclesSinceLastFingerDown++;
                } else {
                    cyclesSinceLastFingerDown = 0;
                }
                if (cyclesSinceLastFingerDown > IntRepConsts.TIME_LIMIT_FOR_NO_FINGER_DOWN
                        * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE) {
                    fingerDownOverride = true;
                    cyclesSinceLastFingerDown = 0;
                    }

                boolean playerWantsToReverse = checkWhichButtonsPressed();
                reverseAllowedCountdown--;
                if(playerWantsToReverse && reverseAllowedCountdown < 0) {
                    reverseAllowedCountdown = REVERSE_ALLOWED_COUNTDOWN_INIT_VAL * IntRepConsts.NUMBER_OF_UPDATES_PER_CYCLE;
                    swingball.reverseDirection();
                }
                boolean atLeastOneButtonPressed = controlButton1Pressed || controlButton2Pressed;

                // if the fingerDownOverride is still on from the start of the level, and the player is
                // touching the screen, turn off the override to release the ball to the player's control.
                if (atLeastOneButtonPressed
                        && gameState != GameState.PREP_FOR_NEXT_LEVEL
                        && gameState != GameState.SHOWING_LEVEL_SUMMARY
                        && gameState != GameState.GAME_OVER
                        && gameState != GameState.RESTARTING_AFTER_GAME_OVER) {
                    fingerDownOverride = false;
                }

                fireflies.updateFireflies();
                numberOfConsecutiveHits += targetManager.hitsOccuredThisCycle;
                hitRateDisplayer.registerHits(targetManager.hitsOccuredThisCycle);
                /*if (numberOfConsecutiveHits == 3 && swingball.autopilot == Swingball.Autopilot.OFF) {
                    messageBoxManager.messageMap.get(MessageBoxManager.MessageType.THREE_IN_A_ROW).on = true;
                    gameScore += IntRepConsts.SCORE_VALUE_OF_3_IN_A_ROW;
                    numberOfConsecutiveHits = 0;
                }*/
                if (targetManager.hitsOccuredThisCycle > 0) {
                    remTarBar.updateRemainingTarBar(targetManager.targetsRemaining);
                    thisRunOfHits += targetManager.hitsOccuredThisCycle;
                    if (thisRunOfHits > bestRunOfHits) {
                        bestRunOfHits = thisRunOfHits;
                    }
                }

                boolean fingerDownParam;
                if (fingerDownOverride) {
                    fingerDownParam = true;
                } else {
                    fingerDownParam = fingerDown;
                }
                if (gameState != GameState.GAME_OVER) {
                    numberOfMisses = swingball.updatePhysics(internalCollisionMap.getCollisionMap(),
                            internalCollisionMap.getFixturesMap(),
                            fingerDownParam,
                            fingerDownOverride,
                            targetManager,
                            rewardsManager,
                            numberOfMisses,
                            controlButton1Pressed,
                            controlButton2Pressed);
                }
                if (swingball.shouldResetConsecutiveHitCounter) {
                    numberOfConsecutiveHits = 0;
                }
            }

            // Update the messageBoxManager.
            messageBoxManager.update();

            // Update the flyingHeart.
            flyingHeart.update();

            // Update the scoreBubblePool (advance the countdowns and alpha decrements).
            scoreBubblePool.updateScoreBubbles();

            // Update the rewardsManager. Returns true if there are two pointer touches and
            // neither of them are on an active reward.
            rewardsManager.update(fingerDown, touchX, touchY);

            // Update the hitrateDisplayer.
            hitRateDisplayer.updateHitRate(countdownTimer.getTimeElapsed());

            // Update the controlButton.
            boolean param;
            if (swingball.autopilot == Swingball.Autopilot.OFF) {
                param = fingerDown;
            } else {
                param = true;
            }
            controlButton.update(param);

            // Processing of movable game objects complete - update graphicsFieldDump in case
            // we run out of time this cycle.
            loadGraphicsFieldDump();

            // Now start rendering the background and readouts, if there is time. If not,
            // return from run() method (allowing GameSurfaceView to draw the same bitmap again).
            if (DEBUG) {
                Log.d(TAG, "Time taken by physics update = " +
                        String.format("%,d", (System.nanoTime() - start)));
            }
            timeLeftForFrame = MAX_TIME_AVAILABLE_NANOS - (System.nanoTime() - choreographerCallBackTime);
            if (DEBUG) {
                Log.d(TAG, "time left after physics update = " + String.format("%,d", timeLeftForFrame));
            }
            if (timeLeftForFrame < TIME_NEEDED_FOR_RENDERING_NANOS) {
                if (DEBUG) {
                    Log.d(TAG, "Not enough time for drawing background");
                }
                continue;
            }

            if (DEBUG) {
                Log.d(TAG, "run() : physics update completed.");
            }

            // Get a canvas for the currentBackBuffer.
            //canvas = new Canvas(currentBackBuffer);  //****************************************MEMORY LEAK****************

            // Draw the one-time objects first.
            levelIndicator.redrawIfNecessary(canvas, usingBB1);

            // Draw the rewardsManager.
            rewardsManager.displayUpdatedTags(canvas, usingBB1);

            // Update and draw the energyBar.
            energyBar.update(swingball.energy, swingball);
            energyBar.drawToCanvas(canvas);

            // Draw the score.
            scoreGlyphs = scoreDisplayer.update((int)gameScore);
            float divider = scoreDisplayer.getDivider();
            float x = scoreDisplayer.getxPos();
            float y = scoreDisplayer.getyPos();
            float xSize = scoreDisplayer.getxSize();
            float rightPos = x + xSize;
            float glyphWidth = scoreDisplayer.getGlyphWidth();
            float xPosition;
            for (int i = 0; i < scoreDisplayer.getNumberOfGlyphs(); i++) {
                xPosition = rightPos - ((i + 1) * glyphWidth + i * divider);
                canvas.drawBitmap(scoreGlyphs[i], xPosition, y, null);
            }

            // Draw the star display.
            scoreDisplayer.updateAndDrawHeartAndStarDisplay(canvas, averageNumberOfStars, livesRemaining, usingBB1);

            // Update and draw the countdownTimer.
            countdownTimer.update();
            countdownTimer.draw(canvas);

            // Update and draw the remainingTargetBar.
            if (remTarBar.redrawCountdown > 0) {
                remTarBar.drawRemainingTarBar(canvas);
            }

            // Draw the controlButton.
            controlButton.drawButtons(canvas);

            // Draw the hitrate.
            hitRateDisplayer.drawHitRate(canvas);

            // Temp - draw the circleGlow on collision with wall.
            if (swingball.suddenDeath == Swingball.SuddenDeath.ON
                    && circleHighlighter.state != CircleHighlighter.State.SUDDEN_DEATH_ON) {
                circleHighlighter.turnOnSuddenDeath();
            } else if (swingball.suddenDeath == Swingball.SuddenDeath.OFF
                    && circleHighlighter.state == CircleHighlighter.State.SUDDEN_DEATH_ON) {
                circleHighlighter.turnOffSuddenDeath();
            }

            circleHighlighter.update();
            circleHighlighter.draw(canvas, usingBB1);

            if (freezeOnNextFrame) {
                while(true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Drawing here is complete.
            setBufferReadyForGameObjects(true);

            // Log the time remaining at end of frame.
            timeLeftForFrame = MAX_TIME_AVAILABLE_NANOS - (System.nanoTime() - choreographerCallBackTime);
            if (DEBUG) {
                Log.d(TAG, "Time left at end of physics/BGRender = " + String.format("%,d", timeLeftForFrame) +
                        " ---------------------------------------- ");
            }
        }
    }

    private void checkIfIsLastLevel() {
        int currentLev = levelManager.getLevel();
        DifficultyLevel difficultyLevel = difficultyLevelDirector.getDiffLev();
        switch(difficultyLevel) {
            case EASY:
                if (currentLev == IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY) {
                    permanentPlayerDataEditor.putBoolean(EASY_COMPLETED, true);
                    permanentPlayerDataEditor.commit();
                    shouldShowDiffLevCompleteAndFinish = true;
                }
                break;
            case NORMAL:
                if (currentLev == IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL) {
                    permanentPlayerDataEditor.putBoolean(NORMAL_COMPLETED, true);
                    permanentPlayerDataEditor.commit();
                    shouldShowDiffLevCompleteAndFinish = true;
                    audioPrefsEditor.putBoolean(HARD_LEVEL_AVAILABLE_TAG, true);
                    audioPrefsEditor.commit();
                }
                break;
            case HARD:
                if (currentLev == IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD) {
                    permanentPlayerDataEditor.putBoolean(HARD_COMPLETED, true);
                    permanentPlayerDataEditor.commit();
                    shouldShowDiffLevCompleteAndFinish = true;
                }
                break;
        }



    }

    private void queryIfShouldDisplayAdAfterGameOverResume() {
        shouldDisplayAd = false;
        checkAdsActivated();
        if ((numberOfLevelsStarted) % IntRepConsts.FREQUENCY_OF_ADS == 0 && adsActivated) {
            shouldDisplayAd = true;
        }
        if (shouldDisplayAd) {
            continueGameAfterAd = true;
            gameScoreAtStartOfCurrentLevel = gameScore;
            displayAd(); // This causes gameActivity to pause.
            gameState = GameState.PREP_FOR_NEXT_LEVEL;
        } else {
            continueGameAfterAd = false;
        }

    }

    private synchronized void loadGraphicsFieldDump(){
        graphicsFieldDump.ballXPos = (float)swingball.xPos;
        graphicsFieldDump.ballYPos = (float)swingball.yPos;
        graphicsFieldDump.ballRadius = swingball.radius;
        graphicsFieldDump.energy = (float)swingball.energy;
        graphicsFieldDump.projStartX = swingball.projectedInnerX;
        graphicsFieldDump.projStartY = swingball.projectedInnerY;
        graphicsFieldDump.projStopX = swingball.projectedOuterX;
        graphicsFieldDump.projStopY = swingball.projectedOuterY;

        graphicsFieldDump.gameScore = (int)gameScore;
        graphicsFieldDump.timeRemaining = countdownTimer.getDisplayedTime();
        for (int i = 0; i < numberOfOrbits; i ++) {
            if (targetManager.orbits[i] == null) {
                graphicsFieldDump.typesOfTargets[i] = null;
            } else {
                graphicsFieldDump.anglesOfTargets[i] = (float) (targetManager.orbits[i].alpha);
                graphicsFieldDump.typesOfTargets[i] = targetManager.orbits[i].type;
                graphicsFieldDump.sizesOfTargets[i] = targetManager.orbits[i].size;
                graphicsFieldDump.opacitiesOfTargets[i] = targetManager.orbits[i].opacity;
                graphicsFieldDump.spriteTypesOfTargets[i] = targetManager.orbits[i].spriteType;
                graphicsFieldDump.shieldEnergies[i] = targetManager.orbits[i].shieldHealth;
                if (targetManager.orbits[i].type == TargetType.DECOYS) {
                    for (int j = 0; j < graphicsFieldDump.decoyPositionsByOrbit[i].length; j++) {
                        graphicsFieldDump.decoyPositionsByOrbit[i][j] = targetManager.orbits[i].decoyPositions[j];
                    }
                }
            }
        }
        graphicsFieldDump.ballPositionList.copyPositionListFrom(swingball.positionList);
    }

    private float calculateTimeForLevel(int level) {
        float time;
        LevelConfig levelConfig = LevelCatalogue.getInstance().getLevelConfig(level, difficultyLevelDirector.getDiffLev());
        int nextLevelNumberOfTargets = levelConfig.getTotalNumberOfTargets();
        time = nextLevelNumberOfTargets * timeForEachTarget;
            if (time > IntRepConsts.MAXIMUM_TIME_ALLOWED_IN_SECONDS) {
                time = IntRepConsts.MAXIMUM_TIME_ALLOWED_IN_SECONDS;
            }

        // Add time for DECOYS, DODGERS AND TADPOLES.
        float extraTime = 0;
        extraTime += levelConfig.getProbabilityOfTarget(TargetType.DECOYS) * IntRepConsts.EXTRA_TIME_FOR_DECOYS;
        extraTime += levelConfig.getProbabilityOfTarget(TargetType.DODGER) * IntRepConsts.EXTRA_TIME_FOR_DODGERS;
        extraTime += levelConfig.getProbabilityOfTarget(TargetType.TADPOLE) * IntRepConsts.EXTRA_TIME_FOR_TADPOLES;
        extraTime += levelConfig.getProbabilityOfTarget(TargetType.KILLER) * IntRepConsts.EXTRA_TIME_FOR_KILLERS;

        return extraTime + time;
    }

    public synchronized GraphicsFieldDump grabDump() {
        return graphicsFieldDump;
    }

    public void setContinueRunningPhysicsThread(boolean continueRunningPhysicsThread) {
        this.continueRunningPhysicsThread = continueRunningPhysicsThread;
    }
    public void setStartGeneratingNextFrame(boolean startGeneratingNextFrame) {
        this.startGeneratingNextFrame = startGeneratingNextFrame;
    }
    public void setBackBuffer(Bitmap buffer) {
        currentBackBuffer = buffer;
    }
    public void setBackBufferCanvas(Canvas c) {
        canvas = c;
    }

    public void setCurrentBackBufferCanvas(Canvas canvas) {currentBackBufferCanvas = canvas;}

    public void setFingerDown(boolean fingerDown) {
        this.fingerDown = fingerDown;
    }

    public void setChoreographerCallBackTime(long choreographerCallBackTime) {
        this.choreographerCallBackTime = choreographerCallBackTime;
    }
    public void setBufferReadyForGameObjects(boolean isReady) {
        bufferReadyForGameObjects = isReady;
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
        timeForEachTarget = IntRepConsts.TIME_ALLOWED_FOR_EACH_TARGET;
        switch (level) {
            case EASY:{
                timeForEachTarget = IntRepConsts.TIME_ALLOWED_FOR_EACH_TARGET;
                scoreValueOfTimeLeft = IntRepConsts.SCORE_VALUE_OF_TIME_LEFT * 0.4f;
                break;
            }
            case NORMAL: {
                timeForEachTarget = IntRepConsts.TIME_ALLOWED_FOR_EACH_TARGET * 0.87f;
                scoreValueOfTimeLeft = IntRepConsts.SCORE_VALUE_OF_TIME_LEFT * 0.8f;
                break;
            }
            case HARD:{
                timeForEachTarget = IntRepConsts.TIME_ALLOWED_FOR_EACH_TARGET * 0.75f;
                scoreValueOfTimeLeft = IntRepConsts.SCORE_VALUE_OF_TIME_LEFT * 2.0f;
                break;
            }
        }
    }

    float calculateScoreValueOfTimeLeft(float timeLeft) {
        return scoreValueOfTimeLeft * timeLeft;
    }

    void pauseAllSound() {
        soundManager.pauseAll();
    }
    void killAndReleaseAudio() {
        soundManager.stopAndRelease();
    }

    void resumeAllSound() {
        soundManager.resumeAll();
    }

    void saveHighScore() {

        if ((int) gameScore > highScore) {
            highScore = (int) gameScore;
            permanentPlayerDataEditor.putInt(PERMANENT_PLAYER_DATA, highScore);
            permanentPlayerDataEditor.commit();
        }
        /*permanentPlayerDataEditor.putInt(PERMANENT_PLAYER_DATA, 0);
        permanentPlayerDataEditor.commit();*/
    }

    int loadHighScore() {
        return permanentPlayerData.getInt(PERMANENT_PLAYER_DATA, 0);
    }

    void saveLevelData() {
        levelDataEditor.putInt(levelNumberString, levelManager.getLevel() + 1);
        levelDataEditor.putFloat(ballEnergyString, (float) (swingball.energy));
        levelDataEditor.putInt(currentRewardString, rewardsManager.currentReward);
        levelDataEditor.putFloat(gameScoreString, gameScore);
        levelDataEditor.putInt(
                difficultyString,
                difficultyLevelDirector.getDiffLev().ordinal());
        float timeRemainingToSave;
        if (gameState == GameState.SHOWING_LEVEL_SUMMARY) {
            timeRemainingToSave = calculateTimeForLevel(levelManager.getLevel());
        } else {
            timeRemainingToSave = countdownTimer.getTime();
        }
        levelDataEditor.putFloat(timeRemainingString, timeRemainingToSave);
        levelDataEditor.putInt(targetsRemainingString, targetManager.targetsRemaining);
        levelDataEditor.putBoolean(resumeAllowedString, true);
        levelDataEditor.putInt(TOTAL_NMBR_STARS, totalNumberOfStars);
        levelDataEditor.commit();
    }

    void saveLevelDataForRestartAfterShop() {
        levelDataEditor.putInt(levelNumberString, levelManager.getLevel() + 1);
        levelDataEditor.putFloat(ballEnergyString, (float) (IntRepConsts.INITIAL_ENERGY));
        levelDataEditor.putInt(currentRewardString, -1);
        levelDataEditor.putFloat(gameScoreString, gameScoreAtStartOfCurrentLevel);
        levelDataEditor.putInt(
                difficultyString,
                difficultyLevelDirector.getDiffLev().ordinal());
        levelDataEditor.putFloat(timeRemainingString, calculateTimeForLevel(levelManager.getLevel()));

        /*OrbitSetupCatalogue.OrbitDetail orbitDetail = targetManager.orbitSetupCatalogue.getOrbitDetail(
                levelManager.getLevel() + 1,
                difficultyLevelDirector.getDiffLev());*/
        LevelConfig levelConfig = LevelCatalogue
                .getInstance()
                .getLevelConfig(levelManager.getLevel() + 1, difficultyLevelDirector.getDiffLev());
        levelDataEditor.putInt(targetsRemainingString, levelConfig.getTotalNumberOfTargets());
        levelDataEditor.putBoolean(resumeAllowedString, true);

        levelDataEditor.putInt(TOTAL_NMBR_STARS, totalNumberOfStars);
        levelDataEditor.commit();
    }

    void resetLevelData() {
        levelDataEditor.putInt(levelNumberString, IntRepConsts.STARTING_LEVEL);
        levelDataEditor.putFloat(ballEnergyString, 100.0f);
        levelDataEditor.putInt(currentRewardString, -1);
        levelDataEditor.putFloat(gameScoreString, 0);
        levelDataEditor.putFloat(timeRemainingString, 0);
        levelDataEditor.putBoolean(resumeAllowedString, false);
        levelDataEditor.putInt(TOTAL_NMBR_STARS, 0);
        levelDataEditor.commit();
    }

    void loadLevelData(Class c) {

        float savedEnergy = levelData.getFloat(ballEnergyString, 100);
        swingball.energy = savedEnergy;

        int rewardChoice = levelData.getInt(currentRewardString, -1);
        rewardsManager.currentReward = rewardChoice;
        rewardsManager.initializeNewReward(rewardChoice);

        gameScore = levelData.getFloat(gameScoreString, 0);
        scoreDisplayer.setScoreWithoutScrolling((int) gameScore - 1);

        startingLevelNumber = levelData.getInt(levelNumberString, 1);
        startingLevelNumber--;

        lastSavedTime = levelData.getFloat(timeRemainingString, 0);

        lastGameTargetsRemainging = levelData.getInt(targetsRemainingString, 1);
        targetManager.targetsRemaining = lastGameTargetsRemainging;

        totalNumberOfStars = levelData.getInt(TOTAL_NMBR_STARS, 0);
        averageNumberOfStars = totalNumberOfStars == 0 ? 0 : (float)totalNumberOfStars / (levelManager.getLevel() + 1);
    }


    void displayAd() {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                currentActivity.adManager.displayBetweenLevelsAd();
            }
        });
        continueGameAfterAd = true;
    }

    int calculateLevelBonus(float timeRemainingAfterLevel, int numberOfMisses, int bestRunOfHits) {
        int b = (int) (calculateScoreValueOfTimeLeft(timeRemainingAfterLevel));
        b += bestRunOfHits * 10;
        b -= numberOfMisses * 3;
        return b < 0 ? 0 : b;
    }

    void adjustNumberOfLives(int deltaLives) {
        livesRemaining += deltaLives;
        if (livesRemaining < 0) {
            livesRemaining = 0;
        }
        permanentPlayerDataEditor.putInt(LIVES_REMAINING_TAG, livesRemaining);
        permanentPlayerDataEditor.commit();
    }

    public void setTouchX(float[] x) {
        for (int i = 0; i < x.length; i++) {
            touchX[i] = x[i];
        }
    }

    public float[] getTouchX() {
        return touchX;
    }

    public void setTouchY(float[] y) {
       for (int i = 0; i < y.length; i++) {
           touchY[i] = y[i];
       }
    }

    public float[] getTouchY() {
        return touchY;
    }

    void clearTouchArrays() {
        for (int i = 0; i < touchX.length; i++) {
            touchX[i] = 0;
            touchY[i] = 0;
        }
    }

    void checkAdsActivated() {
        boolean prefExists = adData.contains(SHOW_ADS);
        int gamesPlayed = permanentPlayerData.getInt("NUMBER_GAMES_PLAYED", -1);
        if (gamesPlayed <= IntRepConsts.NUMBER_OF_GAMES_WITHOUT_ADS) {
            adsActivated = false;
        } else {
            adsActivated = adData.getBoolean(SHOW_ADS, true);
        }
    }

    void setControlButton1Bounds(float left, float top, float right, float bottom) {
        controlButton1Bounds.set(left, top, right, bottom);
    }

    void setControlButton2Bounds(float left, float top, float right, float bottom) {
        controlButton2Bounds.set(left, top, right, bottom);
    }
    boolean checkWhichButtonsPressed() {
        boolean shouldClearTouchArrays = false;
        controlButton1Pressed = false;
        controlButton2Pressed = false;
        shouldClearTouchArrays = rewardsManager.checkIfRewardButtonPressed(touchX, touchY);
        for (int i = 0; i < touchX.length; i++) {
            if (controlButton1Bounds.contains(touchX[i], touchY[i])) {
                controlButton1Pressed = true;
                shouldClearTouchArrays = true;
            }
            if (controlButton2Bounds.contains(touchX[i], touchY[i])) {
                controlButton2Pressed = true;
                shouldClearTouchArrays = true;
            }
        }
        if (shouldClearTouchArrays) {
            clearTouchArrays();
        }
        if (controlButton1Pressed && controlButton2Pressed) {
            return true;
        } else return false;
    }
}
