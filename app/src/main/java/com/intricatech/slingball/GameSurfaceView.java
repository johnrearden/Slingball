package com.intricatech.slingball;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bolgbolg on 09/01/2016.
 * <p/>
 * This class maintains two buffers, one for the PhysicsAndBGRenderer thread to prepare, and one to
 * draw on itself. These are created once, by onSurfaceChanged().
 */
public class GameSurfaceView extends SurfaceView
        implements Runnable,
        SurfaceHolder.Callback,
        Choreographer.FrameCallback{

    InterstitialAd betweenLevelsStaticAd;

    private static String TAG = "GameSurfaceView";
    SharedPreferences levelData;
    private static final String LEVEL_DATA = "LEVEL_DATA";
    private static boolean DEBUG = false;
    private static boolean DEBUG_WIDTH_RATIO = true;
    private static boolean SHOW_STATS = false;
    private static final boolean USE_SPRITES = true;
    private boolean startNewGame;
    long timeOfLastDoFrame;
    private boolean resumeLastGame;
    private boolean firstGrabDumpOccurred;

    private int firstDrawOnNewSurfaceCountdown;
    private static int NUMBER_OF_SURFACEVIEW_BUFFERS = 0;  // 3 extra for safety
    int numberOfCallbacks;
    int numberOfMissedFrames;
    int numberOfSkippedFrames;
    int numberOfNoSwaps;
    private boolean lastFrameCompleted;
    private float missedFrameRatio;
    List<Bitmap> bufferContainer;

    public static long MAX_TIME_AVAILABLE_NANOS = 16700000;
    // The maximum time available (less the lag from the Choreographer
    // callback) to generate the frame and render the background.
    public static long MIN_TIME_FOR_DRAWING_OBJECTS = 6000000;

    Choreographer choreographer;
    Resources resources;

    PlayAreaInfo playAreaInfo;
    PhysicsAndBGRenderer physicsAndBGRenderer;
    MessageBoxManager messageBoxManager;
    DifficultyLevelDirector difficultyLevelDirector;
    EffectsFactory effectsFactory;
    SurfaceHolder holder;

    Thread gameSurfaceViewThread;
    Thread physicsThread;

    Bitmap backBuffer1, backBuffer2;                // Two buffers are maintained to enable one to be
                                                    // drawn on while PhysicsAndBGRenderer is preparing
                                                    // the other.
    Canvas backBufferCanvas1, backBufferCanvas2;    // Canvases for these buffers.

    Bitmap currentBackBuffer;
    Bitmap deepBackground;                  // This is drawn only once.
    Bitmap circlePlateBackground;
    Bitmap ballimagesource, ballimage, ballHaloedImage, ballShieldedImage;
    Bitmap bumpCircle;
    Bitmap rewarderQuestionMark;
    Paint outerCirclePaint;
    Paint ballPaint;
    Paint targetPaint;
    BlurMaskFilter targetBlurMaskFilter;
    Paint textPaint;
    Paint blankCirclePaint;
    Paint playerMessagePaint;
    Paint wallLightPaint;
    Paint blackPaint;
    Paint innerShieldPaint;
    Paint projectedCollPaint;
    Paint trailPaint;
    Paint alphaPaint;
    int circleBackgroundColor;
    int outerCircleColor;
    int innerShieldColor;

    RectF ballRect;
    float projStartX, projStartY, projStopX, projStopY;
    RectF wallLightRect;

    RectF targetRect;
    TargetDrawInfo[] orbitsDrawInfo;
    SpriteKitFactory spriteKitFactory;
    HashMap<SpriteType, SpriteKit> spriteKitMap;
    PositionList ballPosList;

    volatile boolean continueRenderingSurfaceView;
    boolean surfaceAvailable;
    volatile boolean startDrawingGameObjects;
    long choreographerCallBackTime;
    long timeForFrame;
    int screenWidth, screenHeight;
    float ratio;
    int topOfCircle;
    float scaledBallXPos, scaledBallYPos, scaledBallRadius, scaledHaloOffset;
    int maxNumberOfTargets;
    static final float opaqueMaskRatio = IntRepConsts.RATIO_OF_OPAQUE_MASK_TO_TARGET;



    public GameSurfaceView (Context context) {
        super(context);
        resources = context.getResources();
        holder = getHolder();
        holder.addCallback(this);

    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);

    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        holder.addCallback(this);

    }

    public boolean doChildrenExist() {
        boolean answer = true;
        if (physicsAndBGRenderer == null) {
            answer = false;
        }
        if (effectsFactory == null) {
            answer = false;
        }
        return answer;
    }


    public void initializeGameSurfaceView(
            Context context,
            DifficultyLevelDirector difficultyLevelDirector,
            float musicVolume,
            float effectsVolume,
            boolean isVibrationOn,
            int startingLevelNumber) {

        this.betweenLevelsStaticAd = betweenLevelsStaticAd;


        firstGrabDumpOccurred = false;
        this.difficultyLevelDirector = difficultyLevelDirector;
        levelData = context.getSharedPreferences(LEVEL_DATA, Context.MODE_PRIVATE);
        resources = context.getResources();

        circlePlateBackground = BitmapFactory.decodeResource(resources, R.drawable.circle_plate_background);
        bumpCircle = BitmapFactory.decodeResource(resources, R.drawable.bumpcirclecropped);
        rewarderQuestionMark = BitmapFactory.decodeResource(resources, R.drawable.rewarder_question_mark);
        effectsFactory = new EffectsFactory(resources);

        maxNumberOfTargets = IntRepConsts.MAX_NUMBER_OF_ORBITS;

        firstDrawOnNewSurfaceCountdown = NUMBER_OF_SURFACEVIEW_BUFFERS;
        //gameSurfaceViewThread = new Thread(this);
        startNewGame = true;

        choreographer = Choreographer.getInstance();
        //choreographer.postFrameCallback(this);
        numberOfCallbacks = 0;
        numberOfMissedFrames = 0;
        timeOfLastDoFrame = 0;

        physicsAndBGRenderer = new PhysicsAndBGRenderer(this, context, musicVolume, effectsVolume, isVibrationOn, startingLevelNumber);
        physicsAndBGRenderer.checkAdsActivated();
        messageBoxManager = physicsAndBGRenderer.messageBoxManager;
        //physicsThread = new Thread(physicsAndBGRenderer);

        outerCirclePaint = new Paint();
        outerCircleColor = getResources().getColor(R.color.outer_circle);
        outerCirclePaint.setColor(outerCircleColor);
        outerCirclePaint.setStyle(Paint.Style.FILL);
        outerCirclePaint.setStrokeWidth(1);
        outerCirclePaint.setAntiAlias(true);
        targetPaint = new Paint();
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeCap(Paint.Cap.ROUND);
        ballPaint = new Paint();
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setARGB(200, 255, 0, 0);
        textPaint = new Paint();
        textPaint.setARGB(255, 255, 255, 255);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(30);
        blankCirclePaint = new Paint();
        circleBackgroundColor = resources.getColor(R.color.circle_background);
        blankCirclePaint.setColor(circleBackgroundColor);
        blankCirclePaint.setStyle(Paint.Style.FILL);
        alphaPaint = new Paint();

        playerMessagePaint = new Paint();
        playerMessagePaint.setARGB(255, 200, 200, 200);
        playerMessagePaint.setStyle(Paint.Style.STROKE);
        playerMessagePaint.setTextSize(60);
        playerMessagePaint.setTextAlign(Paint.Align.CENTER);
        wallLightPaint = new Paint();
        wallLightPaint.setStyle(Paint.Style.STROKE);
        wallLightPaint.setStrokeCap(Paint.Cap.ROUND);
        wallLightPaint.setARGB(255, 255, 255, 255);
        wallLightPaint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.INNER));
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL);

        innerShieldPaint = new Paint();
        innerShieldColor = resources.getColor(R.color.tadpolehalo);
        innerShieldPaint.setColor(innerShieldColor);
        innerShieldPaint.setAntiAlias(true);
        innerShieldPaint.setStyle(Paint.Style.STROKE);
        innerShieldPaint.setStrokeCap(Paint.Cap.ROUND);

        projectedCollPaint = new Paint();
        projectedCollPaint.setStyle(Paint.Style.STROKE);
        projectedCollPaint.setColor(Color.WHITE);
        projectedCollPaint.setAlpha(IntRepConsts.BALL_PROJECTION_ALPHA);

        trailPaint = new Paint();
        trailPaint.setStyle(Paint.Style.FILL);
        trailPaint.setColor(resources.getColor(R.color.ball_trail));

        playAreaInfo = new PlayAreaInfo();
        playAreaInfo.outermostTargetRect = new RectF();


        ballRect = new RectF();
        wallLightRect = new RectF();
        targetRect = new RectF();
        orbitsDrawInfo = new TargetDrawInfo[maxNumberOfTargets];
        for (int i = 0; i < maxNumberOfTargets; i++) {
            orbitsDrawInfo[i] = new TargetDrawInfo();
        }
        spriteKitFactory = new SpriteKitFactory(difficultyLevelDirector, resources);
        spriteKitMap = new HashMap<SpriteType, SpriteKit>();

        continueRenderingSurfaceView = false;
        surfaceAvailable = false;
        startDrawingGameObjects = false;
        timeForFrame = 0;
        bufferContainer = new ArrayList<Bitmap>();
        backBuffer1 = null;
        backBuffer2 = null;

        ballimage = effectsFactory.getBallImage();
        ballShieldedImage = effectsFactory.getBallShieldedImage();

        ballPosList = new PositionList();

        scaledBallXPos = -200.0f;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (DEBUG) {
            Log.d(TAG, "surfaceChanged() invoked");
        }
        lastFrameCompleted = true;
        screenWidth = width;
        screenHeight = height;
        firstDrawOnNewSurfaceCountdown = NUMBER_OF_SURFACEVIEW_BUFFERS;

        scaleFixedFields();
        physicsAndBGRenderer.setPlayAreaInfo(playAreaInfo);

        effectsFactory.onSurfaceChanged(width, height, playAreaInfo);
        ballimage = effectsFactory.getBallImage();
        ballShieldedImage = effectsFactory.getBallShieldedImage();
        ballHaloedImage = effectsFactory.getBallHaloedImage();

        innerShieldPaint.setStrokeWidth(playAreaInfo.scaledTargetThickness * IntRepConsts.TADPOLE_REL_INDICATOR_THICKNESS);
        projectedCollPaint.setMaskFilter(new BlurMaskFilter(playAreaInfo.scaledTargetThickness, BlurMaskFilter.Blur.INNER));
        projectedCollPaint.setStrokeWidth(playAreaInfo.scaledTargetThickness * 2);

        physicsAndBGRenderer.energyBar.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.remTarBar.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.levelIndicator.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.fireflies.onSurfaceChanged(playAreaInfo);

        physicsAndBGRenderer.scoreDisplayer.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.countdownTimer.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.scoreBubblePool.onSurfaceChanged(playAreaInfo);

        physicsAndBGRenderer.messageBoxManager.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.controlButton.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.bonusDisplayer.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.circleHighlighter.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.hitRateDisplayer.onSurfaceChanged(playAreaInfo);
        physicsAndBGRenderer.flyingHeart.onSurfaceChanged(playAreaInfo);

        circlePlateBackground = Bitmap.createScaledBitmap(
                circlePlateBackground,
                (int)(playAreaInfo.scaledDiameter - playAreaInfo.scaledOuterCircleThickness * 2),
                (int)(playAreaInfo.scaledDiameter - playAreaInfo.scaledOuterCircleThickness * 2),
                false);
        bumpCircle = Bitmap.createScaledBitmap(
                bumpCircle,
                (int) playAreaInfo.screenWidth,
                (int) playAreaInfo.screenWidth,
                false
        );
        rewarderQuestionMark = Bitmap.createScaledBitmap(
                rewarderQuestionMark,
                (int) (playAreaInfo.scaledTargetThickness * IntRepConsts.REL_SIZE_OF_REWARDER_QUESTION_MARK),
                (int) (playAreaInfo.scaledTargetThickness * IntRepConsts.REL_SIZE_OF_REWARDER_QUESTION_MARK),
                false);

        effectsFactory.createTargetExplosions(playAreaInfo);
        drawBackgroundOnBuffers();
        physicsAndBGRenderer.rewardsManager.onSurfaceChanged(playAreaInfo, backBuffer1);
        currentBackBuffer = backBuffer1;
        if (DEBUG) {
            Log.d(TAG, "currentBackBuffer == null : " + (currentBackBuffer == null));
        }
        physicsAndBGRenderer.setBackBuffer(backBuffer2);
        physicsAndBGRenderer.setBackBufferCanvas(backBufferCanvas2);
        physicsAndBGRenderer.usingBB1 = false;

        surfaceAvailable = true;

        spriteKitFactory = new SpriteKitFactory(difficultyLevelDirector, resources);
        for (SpriteType type : SpriteType.values()) {
            spriteKitMap.put(type, spriteKitFactory.getSpriteKit(playAreaInfo, type, type.getTargetType()));
        }

        startThreads();

        if (DEBUG) {
            Log.d(TAG, "surfaceChanged : physicsThread.start() invoked.");
        }
    }
    void startThreads() {

        firstGrabDumpOccurred = false;
        continueRenderingSurfaceView = true;
        gameSurfaceViewThread = new Thread(this);
        gameSurfaceViewThread.start();

        physicsAndBGRenderer.setContinueRunningPhysicsThread(true);
        physicsThread = new Thread(physicsAndBGRenderer);
        physicsThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        surfaceAvailable = false;
        choreographer.removeFrameCallback(this);
        physicsAndBGRenderer = null;
    }

    public void doFrame(long time) {
        if (DEBUG) {
            long t = System.nanoTime() - time;
            long interval = time - timeOfLastDoFrame;
            Log.d(TAG, "***********doFrame() called***********");
            Log.d(TAG, "time elapsed before notifyAll() = " + String.format("%,d", t));
            timeOfLastDoFrame = time;
        }
        choreographer.postFrameCallback(this);
        numberOfCallbacks++;
        if (!lastFrameCompleted) {
            numberOfMissedFrames++;
            missedFrameRatio = (float)numberOfMissedFrames / (float)numberOfCallbacks;
            if (DEBUG) {
                Log.d(TAG, "%%%%%%%%%%%%%%%%%%%%%% :Last frame missed %%%%%%%%%%%%%%%%!");
            }
        }
        lastFrameCompleted = false;
        if (DEBUG) {
            Log.d(TAG, "missed frames : " + numberOfMissedFrames + ", ratio = " + (missedFrameRatio * 100) + ", total frames = " + numberOfCallbacks);
        }

        setStartDrawingGameObjects(true, time);
    }

    @Override
    public void run() {
        long timeAvailable;

        outerloop :
        while (continueRenderingSurfaceView) {
            if (!holder.getSurface().isValid()) {
                continue;
            }
            while(!startDrawingGameObjects) {
                try {
                    Thread.sleep(0, 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            long startTime = 0;
            long timeUsed = System.nanoTime() - choreographerCallBackTime;
            if (DEBUG) {
                Log.d(TAG, "Time since callback = " + String.format("%,d", timeUsed));
            }

            // Check to ensure that there is sufficient time for rendering.
            timeAvailable = MAX_TIME_AVAILABLE_NANOS - (System.nanoTime() - choreographerCallBackTime);
            if (DEBUG) {
                Log.d(TAG, "time available at start of frame = " + String.format("%,d", timeAvailable));
            }
            if (timeAvailable < MIN_TIME_FOR_DRAWING_OBJECTS) {
                if (DEBUG) {
                    Log.d(TAG, "Not enough time for drawing, so skipping frame");
                }
                numberOfSkippedFrames++;
                startDrawingGameObjects = false;
                continue;
            }
            Canvas c = holder.lockCanvas();
            screenWidth = c.getWidth();
            screenHeight = c.getHeight();

            // Draw the backbuffer passed by physicsAndBGRenderer
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            c.drawBitmap(currentBackBuffer, 0, 0, null);
            if (DEBUG) {
                Log.d(TAG, "Time for drawing backBuffer = " + String.format("%,d", System.nanoTime() - startTime));
            }

            // Draw the targets.
            if (true) {
                startTime = System.nanoTime();
            }
            float incrementPerOrbit = playAreaInfo.scaledGapBetweenOrbits
                    + playAreaInfo.scaledTargetThickness;
            float cumulativeRotation = 0;
            float amountToRotate;
            for (int i = 0; i < orbitsDrawInfo.length; i++) {
                SpriteType currentType = orbitsDrawInfo[i].spriteType;
                TargetSize targetSize = orbitsDrawInfo[i].size;

                if (currentType != null && USE_SPRITES) {

                    SpriteKit spriteKit = spriteKitMap.get(currentType);
                    SpriteKit.Sprite s = spriteKit.spriteMap.get(targetSize)[i];
                    amountToRotate = (float) (Math.toDegrees((Math.PI / 2) + orbitsDrawInfo[i].alpha))
                            - cumulativeRotation;
                    cumulativeRotation += amountToRotate;
                    c.rotate(amountToRotate, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
                    float x = playAreaInfo.outermostTargetRect.left + s.xPos;
                    float y = playAreaInfo.outermostTargetRect.top + s.yPos;
                    alphaPaint.setAlpha(255 - (int)orbitsDrawInfo[i].opacity);
                    c.drawBitmap(s.spriteImage, x, y, alphaPaint);


                    // Draw the rewarder question mark if a rewarder exists.
                    if (currentType == SpriteType.REWARDER) {
                        c.drawBitmap(
                                rewarderQuestionMark,
                                x + s.spriteImage.getWidth() / 2 - rewarderQuestionMark.getWidth() / 2,
                                y + playAreaInfo.scaledTargetThickness / 2 - rewarderQuestionMark.getHeight() / 2,
                                null
                        );
                    }
                    // Draw the shield energy, if appropriate.
                    if (orbitsDrawInfo[i].spriteType == SpriteType.TADPOLE_HALOED) {
                        float tadArcSize = orbitsDrawInfo[i].size.getAngularSize() * IntRepConsts.TADPOLE_REL_INDICATOR_LENGTH;
                        targetRect.set(playAreaInfo.outermostTargetRect.left + (i * incrementPerOrbit) + playAreaInfo.drawArcOffset,
                                playAreaInfo.outermostTargetRect.top + (i * incrementPerOrbit) + playAreaInfo.drawArcOffset,
                                playAreaInfo.outermostTargetRect.right - (i * incrementPerOrbit) - playAreaInfo.drawArcOffset,
                                playAreaInfo.outermostTargetRect.bottom - (i * incrementPerOrbit) - playAreaInfo.drawArcOffset);
                        c.drawArc(
                                targetRect,
                                (float) Math.toDegrees(orbitsDrawInfo[i].alpha - tadArcSize / 2) - cumulativeRotation,
                                (float) Math.toDegrees(tadArcSize * orbitsDrawInfo[i].shieldHealth / 100),
                                false,
                                innerShieldPaint);

                    }
                }
                if (currentType != null && !USE_SPRITES) {

                    targetRect.set(playAreaInfo.outermostTargetRect.left + (i * incrementPerOrbit) + playAreaInfo.drawArcOffset,
                            playAreaInfo.outermostTargetRect.top + (i * incrementPerOrbit) + playAreaInfo.drawArcOffset,
                            playAreaInfo.outermostTargetRect.right - (i * incrementPerOrbit) - playAreaInfo.drawArcOffset,
                            playAreaInfo.outermostTargetRect.bottom - (i * incrementPerOrbit) - playAreaInfo.drawArcOffset);
                    targetPaint.setColor(AbstractTarget.getAndroidColor(orbitsDrawInfo[i].type));
                    targetPaint.setStrokeWidth(playAreaInfo.scaledTargetThickness);
                    c.drawArc(targetRect,
                            (float) Math.toDegrees(AbstractTarget.getStartAngle(orbitsDrawInfo[i].alpha,
                                    orbitsDrawInfo[i].size)),
                            (float) Math.toDegrees(AbstractTarget.getSweepAngle(orbitsDrawInfo[i].size)),
                            false,
                            targetPaint);

                }
            }
            c.rotate(-cumulativeRotation, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
            if(DEBUG) {
                Log.d(TAG, "Time for drawing targets = " + String.format("%,d", System.nanoTime() - startTime));
            }


            // Draw the fireflies, if active.
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            if (physicsAndBGRenderer.fireflies.firefliesBehaviour != Fireflies.Behaviour.OFF) {
                physicsAndBGRenderer.fireflies.displayFireflies(c);
            }
            if(DEBUG) {
                Log.d(TAG, "Time for drawing fireflies = " + String.format("%,d", System.nanoTime() - startTime));
            }


            // Draw the explosions, if any.
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            float explosionYBase = playAreaInfo.outermostTargetRect.top;
            float explosionXBase = playAreaInfo.outermostTargetRect.left;
            float explosionY;
            int explosionSize;
            cumulativeRotation = 0;
            ExplosionSequencer[] sequencers = physicsAndBGRenderer.targetManager.explosionSequencers;
            EffectsFactory.ExplosionSprite[] sprites;

            for (ExplosionSequencer es : sequencers) {
                if (es.state == ExplosionSequencer.State.ACTIVE) {
                    //Log.d(TAG, "Explosion generated ...........................");
                    explosionY = explosionYBase + playAreaInfo.scaledTargetThickness * es.orbit;
                    explosionSize = es.updateExplosionSequencer();
                    if (explosionSize == -1) break;
                    amountToRotate = (float) (Math.toDegrees((Math.PI / 2) + es.angle))
                            - cumulativeRotation;
                    c.rotate(amountToRotate, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
                    cumulativeRotation += amountToRotate;
                    sprites = effectsFactory.getExplosionSprites(es.size);
                    c.drawBitmap(sprites[explosionSize].bmap,
                            explosionXBase + sprites[explosionSize].xPos,
                            explosionY + sprites[explosionSize].yPos,
                            null);
                }
            }
            c.rotate(-cumulativeRotation, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
            if(DEBUG) {
                Log.d(TAG, "Time for drawing explosions = " + String.format("%,d", System.nanoTime() - startTime));
            }

            // Temp : draw the previous ball positions. May use bezier curve if this improves UX.
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            if (firstGrabDumpOccurred) {
                if (physicsAndBGRenderer.swingball.shield == Swingball.Shield.ON) {
                    trailPaint.setColor(resources.getColor(R.color.reward_shield));
                } else {
                    trailPaint.setColor(resources.getColor(R.color.ball_trail));
                }
                float w;
                int splitter = ballPosList.previousBallPositions.length / 3 * 2;
                float r = playAreaInfo.scaledBallRadius / 4;
                float inc = r / ballPosList.previousBallPositions.length;
                for (int i = 2; i < splitter; i +=2) {
                    w = r - (inc * i) + 1;
                    trailPaint.setAlpha(150);
                    if (ballPosList.previousBallPositions[i].occupied) {
                        c.drawCircle(
                                ballPosList.previousBallPositions[i].xPos,
                                ballPosList.previousBallPositions[i].yPos,
                                w,
                                trailPaint);
                    }
                }
                for (int i = splitter; i < ballPosList.previousBallPositions.length; i ++) {
                    w = r - (inc * i) + 1;
                    //trailPaint.setAlpha(200 / (i + 1));
                    if (ballPosList.previousBallPositions[i].occupied) {
                        c.drawCircle(
                                ballPosList.previousBallPositions[i].xPos,
                                ballPosList.previousBallPositions[i].yPos,
                                w,
                                trailPaint);
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "Time for drawing ball-tail = " + String.format("%,d", System.nanoTime() - startTime));
            }

            // Draw the ball.
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            if (firstGrabDumpOccurred && physicsAndBGRenderer.gameState != PhysicsAndBGRenderer.GameState.GAME_OVER) {
                if (physicsAndBGRenderer.swingball.shield == Swingball.Shield.ON) {
                    c.drawBitmap(ballShieldedImage, ballRect.left, ballRect.top, null);
                } else if (physicsAndBGRenderer.fingerDown) {
                    c.drawBitmap(ballHaloedImage, ballRect.left - scaledHaloOffset, ballRect.top - scaledHaloOffset, null);
                } else {
                    c.drawBitmap(ballimage, ballRect.left, ballRect.top, null);
                }
            }

            if (DEBUG) {
                Log.d(TAG, "Time for drawing ball = " + String.format("%,d", System.nanoTime() - startTime));
            }

            // DEBUG only :
            if (SHOW_STATS) {
                c.drawText(String.valueOf(numberOfMissedFrames), 400, 300, textPaint);
                c.drawText(String.valueOf(numberOfNoSwaps), 400, 330, textPaint);
            }

            physicsAndBGRenderer.scoreBubblePool.displayScoreBubbles(c);

            // Show the level summary if appropriate.
            if (physicsAndBGRenderer.gameState == PhysicsAndBGRenderer.GameState.SHOWING_LEVEL_SUMMARY) {
                //c.drawText("TAP TO CONTINUE!", 300, 300, textPaint);
                physicsAndBGRenderer.bonusDisplayer.drawDisplay(c);
            }

            // Draw all the messageBoxes currently active.
            if (DEBUG) {
                startTime = System.nanoTime();
            }
            messageBoxManager.drawTopPriorityMessageBox(c);
            if (DEBUG) {
                Log.d(TAG, "Time for drawing messageBoxes = " + String.format("%,d", System.nanoTime() - startTime));
            }

            // Draw the Flying Heart if state == FLYING
            if (physicsAndBGRenderer.flyingHeart.getFlyingHeartState() != FlyingHeart.State.OFF) {
                physicsAndBGRenderer.flyingHeart.drawHeart(c);
            }

            holder.unlockCanvasAndPost(c);
            startDrawingGameObjects = false;
            if (DEBUG) {
                Log.d(TAG, "gameSurfaceView drawing complete, canvas posted");
            }

            if (DEBUG) {
                timeAvailable = MAX_TIME_AVAILABLE_NANOS - (System.nanoTime() - choreographerCallBackTime);
                Log.d(TAG, "time left at end of cycle = " + String.format("%, d", timeAvailable));
            }
            lastFrameCompleted = true;
    }
}
    private void drawDeepBackgroundBitmap() {
        deepBackground = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        // Draw the circle.
        Canvas c = new Canvas(deepBackground);

        // Draw a blank background;
        c.drawARGB(255, 30, 30, 30);
    }

    private void drawBackgroundOnBuffers() {
        if (DEBUG) {
            Log.d(TAG, "drawBackgroundOnBuffers() invoked.");
        }

        int bufferHeight = playAreaInfo.screenHeight;
        backBuffer1 = Bitmap.createBitmap(playAreaInfo.screenWidth, bufferHeight, Bitmap.Config.RGB_565);
        backBuffer2 = Bitmap.createBitmap(playAreaInfo.screenWidth, bufferHeight, Bitmap.Config.RGB_565);
        backBufferCanvas1 = new Canvas(backBuffer1);
        backBufferCanvas2 = new Canvas(backBuffer2);
        bufferContainer.add(backBuffer1);
        bufferContainer.add(backBuffer2);
        Canvas canvas;

        // Draw the readoutBoxes on the backBuffers.
        int dimensionOfBumpCircle = (int) (playAreaInfo.scaledDiameter / IntRepConsts.SCREEN_REMAINING_AFTER_OUTER_CIRCLE);
        bumpCircle = Bitmap.createScaledBitmap(
                bumpCircle,
                dimensionOfBumpCircle,
                dimensionOfBumpCircle,
                false);

        for(Bitmap bmp : bufferContainer) {
            canvas = new Canvas(bmp);
            // Draw a blank background.
            canvas.drawARGB(255, 0, 0, 0);

            canvas.drawBitmap(
                    bumpCircle,
                    playAreaInfo.xCenterOfCircle - dimensionOfBumpCircle / 2,
                    playAreaInfo.yCenterOfCircle - dimensionOfBumpCircle / 2,
                    null
            );

            drawFancyCircle(canvas, 0);

            // Draw the scoreBox.
            physicsAndBGRenderer.scoreDisplayer.drawScoreBoxBackgrounds(canvas);

            // Draw the countDown Box.
            physicsAndBGRenderer.countdownTimer.drawInitialBackground(canvas);

            // Draw the energyBar background.
            physicsAndBGRenderer.energyBar.drawEnergyBarBackground(canvas);

            // Draw the remTarBar background.
            physicsAndBGRenderer.remTarBar.drawBackground(canvas);

            // Draw the levelIndicator background.
            physicsAndBGRenderer.levelIndicator.drawBackground(canvas);

            // Draw the highScore.
            physicsAndBGRenderer.scoreDisplayer.drawHighScore(canvas);

            // Draw the hitrate background.
            physicsAndBGRenderer.hitRateDisplayer.drawHitRateBackground(canvas);

            canvas = null;
        }

        // Release resources.
        bumpCircle.recycle();
        bumpCircle = null;
        circlePlateBackground.recycle();
        circlePlateBackground = null;
    }

    private void drawFancyCircle(Canvas canvas, float rotation) {

        canvas.rotate(rotation, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);

        canvas.drawBitmap(
                circlePlateBackground,
                playAreaInfo.xCenterOfCircle - circlePlateBackground.getWidth() / 2,
                playAreaInfo.yCenterOfCircle - circlePlateBackground.getWidth() / 2,
                null
        );

        canvas.rotate(-rotation, playAreaInfo.xCenterOfCircle, playAreaInfo.yCenterOfCircle);
    }

    private void swapBackBuffers() {
        // Don't swap unless the physicsAndBGRenderer has completed its drawing routines.
        if (!physicsAndBGRenderer.bufferReadyForGameObjects) {
            if (DEBUG) {
                Log.d(TAG, "***************no swap***************" + numberOfNoSwaps);
            }
            numberOfNoSwaps++;
            return;
        }
        if (currentBackBuffer == backBuffer1 || currentBackBuffer == null) {
            currentBackBuffer = backBuffer2;
            physicsAndBGRenderer.setBackBuffer(backBuffer1);
            physicsAndBGRenderer.setBackBufferCanvas(backBufferCanvas1);
            physicsAndBGRenderer.usingBB1 = true;
            physicsAndBGRenderer.setBufferReadyForGameObjects(false);
            return;
        } else if (currentBackBuffer == backBuffer2) {
            currentBackBuffer = backBuffer1;
            physicsAndBGRenderer.setBackBuffer(backBuffer2);
            physicsAndBGRenderer.setBackBufferCanvas(backBufferCanvas2);
            physicsAndBGRenderer.usingBB1 = false;
            physicsAndBGRenderer.setBufferReadyForGameObjects(false);
            return;
        }
    }

    public void setStartDrawingGameObjects(boolean go, long time) {
        choreographerCallBackTime = time;
        physicsAndBGRenderer.setChoreographerCallBackTime(time);

        // Grab the draw information from physicsAndBGRenderer's previous cycle. If it failed to
        // complete, the previous one is still available.
        setDrawInformation(physicsAndBGRenderer.grabDump());

        // Notify both threads to start running. Swap the backbuffers before starting the physicsAndBGRenderer thread.
        synchronized (this) {
            swapBackBuffers();
            startDrawingGameObjects = true;
        }
        synchronized (physicsAndBGRenderer) {
            physicsAndBGRenderer.setStartGeneratingNextFrame(true);
        }
        timeForFrame = MAX_TIME_AVAILABLE_NANOS - (System.nanoTime() - choreographerCallBackTime);
        if (DEBUG) {
            Log.d(TAG, String.format("setDrawingGameObjects time available for frame = " + "%,d", timeForFrame));
        }
    }

    public void setDrawInformation(GraphicsFieldDump dump) {

        if (dump != null) {
            scaledBallXPos = dump.ballXPos - IntRepConsts.DIAMETER / 2;
            scaledBallYPos = dump.ballYPos - IntRepConsts.DIAMETER / 2;
            scaledBallRadius = dump.ballRadius;

            float halfDiam = IntRepConsts.DIAMETER / 2;
            float rat = playAreaInfo.ratioOfActualToModel;
            projStartX = playAreaInfo.xCenterOfCircle + (dump.projStartX - halfDiam) * rat;
            projStartY = playAreaInfo.yCenterOfCircle + (dump.projStartY - halfDiam) * rat;
            projStopX = playAreaInfo.xCenterOfCircle + (dump.projStopX - halfDiam) * rat;
            projStopY = playAreaInfo.yCenterOfCircle + (dump.projStopY - halfDiam) * rat;

            for (int i = 0; i < maxNumberOfTargets; i++) {
                if (dump.typesOfTargets[i] == null) {
                    orbitsDrawInfo[i].type = null;
                    orbitsDrawInfo[i].spriteType = null;
                } else {
                    orbitsDrawInfo[i].alpha = (float) dump.anglesOfTargets[i];
                    orbitsDrawInfo[i].type = dump.typesOfTargets[i];
                    orbitsDrawInfo[i].size = dump.sizesOfTargets[i];
                    orbitsDrawInfo[i].opacity = dump.opacitiesOfTargets[i];
                    orbitsDrawInfo[i].spriteType = dump.spriteTypesOfTargets[i];
                    orbitsDrawInfo[i].shieldHealth = dump.shieldEnergies[i];
                    if (orbitsDrawInfo[i].type == TargetType.DECOYS) {
                        for (int j = 0; j < IntRepConsts.DECOY_MAX_POPULATION; j++) {
                            orbitsDrawInfo[i].decoyPositions[j] = dump.decoyPositionsByOrbit[i][j];
                        }
                    }
                }
            }

            scaleDumpFields();
        } else {
            for (int i = 0; i < maxNumberOfTargets; i++) {
                orbitsDrawInfo[i].type = null;
            }
        }
        float xx, yy;
        for (int i = 0; i < ballPosList.previousBallPositions.length; i++) {
            xx = dump.ballPositionList.previousBallPositions[i].xPos;
            yy = dump.ballPositionList.previousBallPositions[i].yPos;
            xx = (xx - IntRepConsts.X_CENTER_OF_ROTATION) * playAreaInfo.ratioOfActualToModel
                + playAreaInfo.xCenterOfCircle;
            yy = (yy - IntRepConsts.Y_CENTER_OF_ROTATION) * playAreaInfo.ratioOfActualToModel
                + playAreaInfo.yCenterOfCircle;
            ballPosList.previousBallPositions[i].assignValues(xx, yy);
        }

        firstGrabDumpOccurred = true;
    }

    void scaleImages() {
        ballimage = Bitmap.createScaledBitmap(ballimagesource,
                (int) (playAreaInfo.scaledBallRadius * 2),
                (int) (playAreaInfo.scaledBallRadius * 2),
                false);
    }

    float calculateWidthRatio() {
        /*
        If the width to height ratio allows for the minimum height for the top and bottom panels
        while using the full width of the screen for the circle, then escape early to this option.
         */
        float minPanelHeight = (float) screenHeight * IntRepConsts.MINIMUM_TOPBOTTOM_PANELS_HEIGHT_RATIO;
        if (screenHeight - screenWidth > minPanelHeight) {
            if (DEBUG_WIDTH_RATIO) {
                Log.d(TAG, "calculateWidthRatio() : can indeed use full width for circle diameter ... ratio is 1.0f");
            }
            return IntRepConsts.SCREEN_REMAINING_AFTER_OUTER_CIRCLE;
        }

        /*
        Reduce the width to match the height available for the circle (after deducting the minPanelHeight
        from the total height) and return the ratio.
         */

        float heightRemainingForCircle = screenHeight - minPanelHeight;
        float reducedRatio = heightRemainingForCircle / screenWidth * IntRepConsts.SCREEN_REMAINING_AFTER_OUTER_CIRCLE;
        if (DEBUG_WIDTH_RATIO) {
            Log.d(TAG, "calculateWidthRatio() : not enought height, ratio reduced to " + reducedRatio + "f");
        }

        return reducedRatio;
    }

    void scaleFixedFields() {

        float widthRatio = calculateWidthRatio();
        float spaceAvailableForTopAndBottomPanel;
        float minPanelHeight = (float) screenHeight * IntRepConsts.MINIMUM_TOPBOTTOM_PANELS_HEIGHT_RATIO;

        if (screenHeight - screenWidth > minPanelHeight) {
            spaceAvailableForTopAndBottomPanel = screenHeight - screenWidth;
        } else {
            spaceAvailableForTopAndBottomPanel = IntRepConsts.MINIMUM_TOPBOTTOM_PANELS_HEIGHT_RATIO * screenHeight;
            Log.d(TAG, "spaceAvailableForTopAndBottomPanel == minimum available == " + spaceAvailableForTopAndBottomPanel);
        }

        playAreaInfo.circleWidthRatio = widthRatio;
        playAreaInfo.screenWidth = screenWidth;
        playAreaInfo.screenHeight = screenHeight;

        ratio = (float) (screenWidth * playAreaInfo.circleWidthRatio) / (float) IntRepConsts.DIAMETER;
        playAreaInfo.ratioOfActualToModel = ratio;

        scaleDumpFields();

        playAreaInfo.topPanelHeight = (int)(spaceAvailableForTopAndBottomPanel * IntRepConsts.RATIO_OF_TOP_PANEL_TO_AVAILABLE_SPACE);

        topOfCircle = playAreaInfo.topPanelHeight;

        playAreaInfo.scaledDiameter = (int) ((IntRepConsts.DIAMETER * ratio) + 0.5);

        playAreaInfo.xCenterOfCircle = screenWidth / 2;
        playAreaInfo.yCenterOfCircle = topOfCircle + playAreaInfo.scaledDiameter / 2;


        playAreaInfo.scaledBallRadius = IntRepConsts.BALL_RADIUS * ratio;
        playAreaInfo.scaledBallShadowDiameter = IntRepConsts.BALL_SHADOW_DIAMETER * ratio;
        playAreaInfo.scaledOuterCircleThickness = (int) ((IntRepConsts.OUTER_CIRCLE_THICKNESS * ratio) + 0.5);
        playAreaInfo.scaledTargetThickness = (int) ((IntRepConsts.TARGET_THICKNESS * ratio) + 0.5);
        playAreaInfo.scaledGapBetweenOrbits = (int) ((IntRepConsts.GAP_BETWEEN_ORBITS * ratio) + 0.5);
        playAreaInfo.effectiveDiameter =
                playAreaInfo.scaledDiameter
                - (2 * playAreaInfo.scaledOuterCircleThickness)
                - (2 * playAreaInfo.scaledGapBetweenOrbits);
        playAreaInfo.drawArcOffset = playAreaInfo.scaledTargetThickness / 2;
        // set the size of the outermostTargetRect.

        float left = playAreaInfo.xCenterOfCircle - playAreaInfo.effectiveDiameter / 2;
        float right = playAreaInfo.xCenterOfCircle + playAreaInfo.effectiveDiameter / 2;
        float top = playAreaInfo.yCenterOfCircle - playAreaInfo.effectiveDiameter / 2;
        float bottom = playAreaInfo.yCenterOfCircle + playAreaInfo.effectiveDiameter / 2;

        playAreaInfo.outermostTargetRect.set(left, top, right, bottom);

        outerCirclePaint.setStrokeWidth(playAreaInfo.scaledOuterCircleThickness);
        targetPaint.setStrokeWidth(playAreaInfo.scaledTargetThickness);
        targetBlurMaskFilter = new BlurMaskFilter(playAreaInfo.drawArcOffset, BlurMaskFilter.Blur.INNER);

        float halfScaledOuterCircleThickness = playAreaInfo.scaledOuterCircleThickness / 2;
        wallLightRect.set(
                playAreaInfo.xCenterOfCircle - (playAreaInfo.scaledDiameter / 2) + halfScaledOuterCircleThickness,
                playAreaInfo.yCenterOfCircle - (playAreaInfo.scaledDiameter / 2) + halfScaledOuterCircleThickness,
                playAreaInfo.xCenterOfCircle + (playAreaInfo.scaledDiameter / 2) - halfScaledOuterCircleThickness,
                playAreaInfo.yCenterOfCircle + (playAreaInfo.scaledDiameter / 2) - halfScaledOuterCircleThickness
        );
        wallLightPaint.setStrokeWidth(playAreaInfo.scaledOuterCircleThickness);
    }

    void scaleDumpFields() {
        scaledBallXPos = playAreaInfo.xCenterOfCircle + scaledBallXPos * ratio;
        scaledBallYPos = playAreaInfo.yCenterOfCircle + scaledBallYPos * ratio;
        scaledBallRadius = (scaledBallRadius * ratio) + 0.5f;
        ballRect.set(scaledBallXPos - scaledBallRadius,
                scaledBallYPos - scaledBallRadius,
                scaledBallXPos + scaledBallRadius,
                scaledBallYPos + scaledBallRadius);
        scaledHaloOffset = scaledBallRadius * 0.5f;
    }


    public void pause() {
        if (DEBUG) {
            Log.d("**********onPause : ", "Invoked*************");
        }
        choreographer.removeFrameCallback(this);
        continueRenderingSurfaceView = false;
        physicsAndBGRenderer.setContinueRunningPhysicsThread(false);
        //physicsAndBGRenderer.pauseAllSound();
        //physicsAndBGRenderer.killAndReleaseAudio();
        physicsAndBGRenderer.saveHighScore();
        if (!physicsAndBGRenderer.gameOver) {
            physicsAndBGRenderer.saveLevelData();
        }


        while (true) {
            try {
                setStartDrawingGameObjects(true, 0);  // Necessary in case thread is waiting.
                gameSurfaceViewThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (DEBUG) {
                Log.d("**********onPause : ", "gameSurfaceThread joined.************");
            }
            try {
                physicsAndBGRenderer.setStartGeneratingNextFrame(true); // Necessary in case thread is waiting.
                physicsThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                physicsAndBGRenderer.gameSurfaceView = null;
            }

            if (DEBUG) {
                Log.d("**********onPause : ", "physicsThread joined.************");
            }

            //recycleBackbuffers();

            break;
        }
    }

    public void resume(
            Context context,
            DifficultyLevelDirector difficultyLevelDirector,
            float musicVolume,
            float effectsVolume,
            boolean isVibrationOn,
            boolean resumeAfterGameOver) {
        if (!doChildrenExist()) {
            initializeGameSurfaceView(
                    context,
                    difficultyLevelDirector,
                    musicVolume,
                    effectsVolume,
                    isVibrationOn,
                    levelData.getInt("LEVEL_NUMBER", 1)
            );
            Log.d(TAG, "Children do not exist");
        } else {
            Log.d(TAG, "Children do in fact exist");
        }
        choreographer.postFrameCallback(this);
        //physicsAndBGRenderer.setStartNewGame(true);
        physicsAndBGRenderer.resumingGame = resumeLastGame;
        physicsAndBGRenderer.loadLevelData(this.getClass());
        firstDrawOnNewSurfaceCountdown = NUMBER_OF_SURFACEVIEW_BUFFERS;
        if (!physicsAndBGRenderer.continueGameAfterAd) {
            physicsAndBGRenderer.gameState = PhysicsAndBGRenderer.GameState.PREP_FOR_NEXT_LEVEL;
        } else {
            physicsAndBGRenderer.gameState = PhysicsAndBGRenderer.GameState.PREP_FOR_NEXT_LEVEL;
        }
        //physicsAndBGRenderer.resumeAllSound();

        //test
        if (surfaceAvailable) startThreads();



    }

    public void listChildObjects() {
        Log.d(TAG, "physicsAndBGRenderer : " + physicsAndBGRenderer.toString());
        Log.d(TAG, "physicsAndBGRenderer.messageBoxManager : " + physicsAndBGRenderer.messageBoxManager.toString());
        Log.d(TAG, "physicsAndBGRenderer.soundManager : " + physicsAndBGRenderer.soundManager.toString());
        Log.d(TAG, "physicsAndBGRenderer.swingball : " + physicsAndBGRenderer.swingball.toString());
    }

    public void setResumeLastGame(boolean b) {
        this.resumeLastGame = b;
    }

    void recycleBackbuffers() {
        backBuffer1.recycle();
        backBuffer2.recycle();
        backBuffer1 = null;
        backBuffer2 = null;
    }

}
