package com.intricatech.slingball;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Map;


public class GameActivity extends FragmentActivity
        implements View.OnTouchListener,
        DifficultyLevelObserver,
        OnGameOverDialogFinishedListener{


    public static String TAG = "GameActivity";
    private DiffLevCompleteFragment diffLevCompleteFragment;
    private static final boolean DEBUG = false;
    AdManager adManager;
    SoundManager soundManager;

    DifficultyLevel level;
    boolean startNewGame;

    SharedPreferences audioPrefs, highscoreData, levelData, adDataPrefs, permanentPlayerData;
    String TOTAL_NMBR_STARS;
    String MUSIC_VOLUME;
    String EFFECTS_VOLUME;
    String VIBRATION;
    String SHOULD_PLAY_VOICES;
    private String PREVIOUS_LEVEL;
    private static final String AUDIO_PREFERENCES = "AUDIO";
    private static final String HIGH_SCORE_DATA = "HIGH_SCORES";
    private static final String LEVEL_DATA = "LEVEL_DATA";
    static final String SHOW_ADS = "SHOW_ADS";
    static final String AD_DATA = "AD_DATA";

    private static final int SIZE_OF_TOUCH_ARRAY = IntRepConsts.SIZE_OF_TOUCH_ARRAY;

    float musicVolume, effectsVolume;
    boolean isVibrationOn;
    boolean shouldPlayVoices;
    boolean resumeLastGame;
    boolean resumeAfterGameOver;
    boolean showAds;
    private int numberGamesPlayed;

    GameSurfaceView gameSurfaceView;
    DifficultyLevelDirector difficultyLevelDirector;
    SpriteKitFactory spriteKitFactory;
    Map<SpriteType, SpriteKit> spriteKitMap;
    Resources resources;

    private float[] touchX;
    private float[] touchY;
    private int pointerCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        resources = getResources();

        adManager = new AdManager();

        TOTAL_NMBR_STARS = resources.getString(R.string.total_number_of_stars);
        PREVIOUS_LEVEL = getResources().getString(R.string.previous_level_tag);
        MUSIC_VOLUME = getResources().getString(R.string.music_volume_tag);
        EFFECTS_VOLUME = getResources().getString(R.string.effects_volume_tag);
        VIBRATION = getResources().getString(R.string.vibration_tag);
        SHOULD_PLAY_VOICES = "SHOULD_PLAY_VOICES";

        // Establish SharedPreferences for audio, high score and game state persistent storage.
        audioPrefs = getSharedPreferences(AUDIO_PREFERENCES, MODE_PRIVATE);
        highscoreData = getSharedPreferences(HIGH_SCORE_DATA, MODE_PRIVATE);
        levelData = getSharedPreferences(LEVEL_DATA, MODE_PRIVATE);
        adDataPrefs = getSharedPreferences(AD_DATA, MODE_PRIVATE);
        permanentPlayerData = getSharedPreferences("PERMANENT_PLAYER_DATA", MODE_PRIVATE);

        Intent intent = getIntent();
        resumeLastGame = (boolean) intent.getBooleanExtra("RESUME_LAST_GAME", false);

        // Replacements
        level = DifficultyLevel.values()[audioPrefs.getInt(PREVIOUS_LEVEL, 0)];
        startNewGame = true;
        effectsVolume = audioPrefs.getFloat(EFFECTS_VOLUME, 70);
        musicVolume = audioPrefs.getFloat(MUSIC_VOLUME, 70);
        isVibrationOn = audioPrefs.getBoolean(VIBRATION, true);
        shouldPlayVoices = audioPrefs.getBoolean(SHOULD_PLAY_VOICES, true);
        showAds = adDataPrefs.getBoolean(SHOW_ADS, true);
        numberGamesPlayed = permanentPlayerData.getInt("NUMBER_GAMES_PLAYED", 0);


        soundManager = new SoundManager(
                this,
                musicVolume,
                effectsVolume,
                shouldPlayVoices
                );

        difficultyLevelDirector = new DifficultyLevelDirector();
        int startingLevelNumber;
        if (!resumeLastGame) {
            difficultyLevelDirector.updateDifficultyLevel(level);
            startingLevelNumber = IntRepConsts.STARTING_LEVEL - 1;
        } else {
            int diffLev = levelData.getInt(resources.getString(R.string.difficulty_string), 0);
            difficultyLevelDirector.updateDifficultyLevel(DifficultyLevel.values()[diffLev]);
            startingLevelNumber = levelData.getInt(resources.getString(R.string.level_data_level_number), 1);
            startingLevelNumber--;
        }

        setContentView(R.layout.activity_game);
        gameSurfaceView = (GameSurfaceView) findViewById(R.id.game_surfaceview);
        gameSurfaceView.initializeGameSurfaceView(
                this,
                difficultyLevelDirector,
                musicVolume,
                effectsVolume,
                isVibrationOn,
                startingLevelNumber
        );
        gameSurfaceView.setOnTouchListener(this);
        gameSurfaceView.setResumeLastGame(resumeLastGame);

        if (!resumeLastGame) {
            gameSurfaceView.physicsAndBGRenderer.resetLevelData();
        } else {
            //gameSurfaceView.physicsAndBGRenderer.loadLevelData(this.getClass());
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onPause() invoked");
        }
        gameSurfaceView.pause();
        soundManager.pauseAll();
        if (!gameSurfaceView.physicsAndBGRenderer.continueGameAfterAd) {
            gameSurfaceView = null;
            finish();
        }


    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onResume() invoked");
        }

        gameSurfaceView.resume(this, difficultyLevelDirector, musicVolume, effectsVolume, isVibrationOn, resumeAfterGameOver);
        soundManager.resumeAll();
    }

    protected void onStop() {
        super.onStop();
        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onStop() invoked");
        }
        soundManager.stopAndRelease();
        adManager.removeListeners();
        //adManager.nullOutBetweenLevelsStaticAd();
    }

    protected void onStart() {
        super.onStart();
        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onStart() invoked");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onDestroy() invoked");
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent me) {
        if (gameSurfaceView != null && gameSurfaceView.physicsAndBGRenderer != null) {

            switch (me.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    touchX = new float[SIZE_OF_TOUCH_ARRAY];
                    touchY = new float[SIZE_OF_TOUCH_ARRAY];
                    pointerCount = me.getPointerCount();
                    gameSurfaceView.physicsAndBGRenderer.setFingerDown(true);
                    for (int i = 0; i < pointerCount; i++) {
                        touchX[i] = me.getX(i);
                        touchY[i] = me.getY(i);
                    }
                    gameSurfaceView.physicsAndBGRenderer.setTouchX(touchX);
                    gameSurfaceView.physicsAndBGRenderer.setTouchY(touchY);
                    //showStats(me);
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    gameSurfaceView.physicsAndBGRenderer.setFingerDown(true);
                    touchX = new float[SIZE_OF_TOUCH_ARRAY];
                    touchY = new float[SIZE_OF_TOUCH_ARRAY];
                    pointerCount = me.getPointerCount();
                    for (int i = 0; i < pointerCount; i++) {
                        touchX[i] = me.getX(i);
                        touchY[i] = me.getY(i);
                    }
                    gameSurfaceView.physicsAndBGRenderer.setTouchX(touchX);
                    gameSurfaceView.physicsAndBGRenderer.setTouchY(touchY);
                    //showStats(me);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    gameSurfaceView.physicsAndBGRenderer.setFingerDown(false);
                    touchX = new float[SIZE_OF_TOUCH_ARRAY];
                    touchY = new float[SIZE_OF_TOUCH_ARRAY];
                    pointerCount = me.getPointerCount();
                    for (int i = 0; i < pointerCount; i++) {
                        touchX[i] = me.getX(i);
                        touchY[i] = me.getY(i);
                    }
                    touchX[pointerCount] = 0.0f;
                    touchY[pointerCount] = 0.0f;
                    //gameSurfaceView.physicsAndBGRenderer.clearTouchArrays();
                    gameSurfaceView.physicsAndBGRenderer.setTouchX(touchX);
                    gameSurfaceView.physicsAndBGRenderer.setTouchY(touchY);
                    //showStats(me);
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP: {
                    touchX = new float[SIZE_OF_TOUCH_ARRAY];
                    touchY = new float[SIZE_OF_TOUCH_ARRAY];
                    pointerCount = me.getPointerCount();
                    for (int i = pointerCount; i < pointerCount; i++) {
                        touchX[i] = me.getX(i);
                        touchY[i] = me.getY(i);
                    }
                    touchX[pointerCount] = 0.0f;
                    touchY[pointerCount] = 0.0f;
                    //gameSurfaceView.physicsAndBGRenderer.clearTouchArrays();
                    gameSurfaceView.physicsAndBGRenderer.setTouchX(touchX);
                    gameSurfaceView.physicsAndBGRenderer.setTouchY(touchY);
                    //showStats(me);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    break;
                }
            }

        }

        return true;
    }

    private void showStats(MotionEvent me) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pointerCount; i++) {
            sb.append(me.getX(i) + ", ");
        }
        Log.d(TAG, "pointerCount == " + pointerCount + ", " + sb.toString());
        sb = new StringBuilder();
        for (int i = 0; i < SIZE_OF_TOUCH_ARRAY; i++) {

            sb.append(touchX[i] + ", ");
        }
        Log.d(TAG, "pointerCount == " + pointerCount + ", " + sb.toString());
    }

    // Temp - class to enable physicsAndBGRenderer's reference to this activity to be
    // nulled out before finishing the activity. This is only used in gameState == ...GAME_OVER.
    public void nullOutCallerAndFinish() {
        gameSurfaceView.physicsAndBGRenderer.currentActivity = null;
        finish();
    }


    @Override
    public void registerWithDifficultyLevelDirector() {

    }

    @Override
    public void unregisterWithDifficultyLevelDirector() {

    }

    @Override
    public void updateDifficultyDependents(DifficultyLevel level) {

    }

    void displayDiffLevCompleteFragment(DifficultyLevel difficultyLevel) {
        diffLevCompleteFragment = new DiffLevCompleteFragment();
        Bundle bundle = new Bundle();
        bundle.putString("DIFFICULTY_LEVEL", difficultyLevel.toString());
        diffLevCompleteFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.parent_gameactivity_relativelayout, diffLevCompleteFragment);
        fragmentTransaction.commit();
    }

    public void onReturnToMainFromDiffLev (View view) {
       finish();
    }


    class AdManager {

        InterstitialAd betweenLevelsStaticAd;

        AdManager() {
            betweenLevelsStaticAd = new InterstitialAd(getApplicationContext());

            betweenLevelsStaticAd.setAdUnitId(resources.getString(R.string.between_level_static_interstitial_unitID));
            betweenLevelsStaticAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewStaticInterstitial();
                }
            });
            requestNewStaticInterstitial();
        }

        void requestNewStaticInterstitial() {

            AdRequest adRequest;
            if (!IntRepConsts.IS_RELEASE_VERSION) {
                adRequest = new AdRequest.Builder()
                        .addTestDevice("283A16FB4D5ADB387BEA5F06286ADA4D")
                        .addTestDevice("4A565E697903F7556B14620EFEB7D21B")
                        .addTestDevice("4A4AEC508E18C5C9DAFDC3FD5725942D")
                        .build();
            } else {
                adRequest = new AdRequest.Builder().build();
            }
            betweenLevelsStaticAd.loadAd(adRequest);
        }

        void displayBetweenLevelsAd() {
            if (showAds) {
                if (betweenLevelsStaticAd.isLoaded()) {
                    betweenLevelsStaticAd.show();
                    requestNewStaticInterstitial();
                } else {
                    if (DEBUG) {
                        Log.d(getClass().getSimpleName(), "betweenLevelsStaticAd.isLoaded() == " + String.valueOf(betweenLevelsStaticAd.isLoaded()));
                    }
                }
            } else {
                if (DEBUG) {
                    Log.d(getClass().getSimpleName(), "showAds == " + String.valueOf(showAds));
                }
            }
        }

        void removeListeners() {
            betweenLevelsStaticAd.setAdListener(null);
        }
    }

    void showGameOverDialog(int costOfRestart, int livesRemaining, PhysicsAndBGRenderer.GameOverCause cause) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("gameover");
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = GameOverDialogFragment.newInstance(costOfRestart, livesRemaining, cause);
        newFragment.show(ft, "gameover");
    }

    @Override
    public void onGameOverDialogFinished(ActionToTake actionToTake, int deltaLives) {
        switch (actionToTake) {
            case RESUME_GAME_AT_CURRENT_POINT:
                gameSurfaceView.physicsAndBGRenderer.shouldRestartAfterGameOver = true;
                gameSurfaceView.physicsAndBGRenderer.adjustNumberOfLives(deltaLives);
                break;
            case END_GAME_AND_GO_TO_MAIN_MENU:
                gameSurfaceView.physicsAndBGRenderer.resetLevelData();
                nullOutCallerAndFinish();
                break;
        }
    }

    public static class GameOverDialogFragment extends DialogFragment {

        private OnGameOverDialogFinishedListener listener;

        TextView costTextView, currentLivesTextView;
        TextView noLivesRemainingMessage;
        private Button purchaseMoreLivesButton;

        static int costAmount, currentLivesAmount;
        static PhysicsAndBGRenderer.GameOverCause cause;

        static final String COST_OF_RESTART = "cost_of_restart";
        static final String CURRENT_LIVES = "current_lives";
        static final String CAUSE = "cause";
        String SHOULD_RESTART_AFTER_GAMEOVER;

        ImageView resumeGameButton;
        Button backToMainMenuButton;
        LinearLayout restartSubLayout;
        TextView gameOverCauseTV;

        static GameOverDialogFragment newInstance(int costOfRestart, int currentLives,
                                                  PhysicsAndBGRenderer.GameOverCause gameOverCause) {

            GameOverDialogFragment frag = new GameOverDialogFragment();
            frag.setCancelable(false);


            costAmount = costOfRestart;
            currentLivesAmount = currentLives;
            cause = gameOverCause;

            Bundle args = new Bundle();
            args.putInt(COST_OF_RESTART, costOfRestart);
            args.putInt(CURRENT_LIVES, currentLives);
            args.putInt(CAUSE, cause.ordinal());
            frag.setArguments(args);

            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);


        }



        @Override
        public void onStart()
        {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null)
            {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setLayout(width, height);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.game_over_dialog_layout, container, false);

            SHOULD_RESTART_AFTER_GAMEOVER = getResources().getString(R.string.should_restart_after_gameOver);

            noLivesRemainingMessage = (TextView) v.findViewById(R.id.no_lives_remaining_message);
            resumeGameButton = (ImageView) v.findViewById(R.id.restart_button);
            purchaseMoreLivesButton = (Button) v.findViewById(R.id.purchase_lives);
            purchaseMoreLivesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "getActivity : " + getActivity().getClass().getSimpleName());

                    Intent intent = new Intent(getActivity(), ShopActivity.class);
                    if (currentLivesAmount > 0) {
                        intent.putExtra(SHOULD_RESTART_AFTER_GAMEOVER, true);
                    } else {
                        intent.putExtra(SHOULD_RESTART_AFTER_GAMEOVER, false);
                    }
                    intent.putExtra("CAME_FROM_GAMEOVER", true);
                    startActivity(intent);
                }
            });

            backToMainMenuButton = (Button) v.findViewById(R.id.back_to_homescreen);
            backToMainMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onGameOverDialogFinished(ActionToTake.END_GAME_AND_GO_TO_MAIN_MENU, 0);
                }
            });

            costTextView = (TextView) v.findViewById(R.id.game_over_costs);
            String text = getResources().getString(R.string._game_over_costs)
                    + " "
                    + String.valueOf(costAmount)
                    + " ";
            costTextView.setText(text);

            currentLivesTextView = (TextView) v.findViewById(R.id.current_lives_quantity);
            currentLivesTextView.setText(" " + String.valueOf(currentLivesAmount) + " ");

            restartSubLayout = (LinearLayout) v.findViewById(R.id.clickable_restart_sublayout);
            restartSubLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentLivesAmount > 0) {
                        listener.onGameOverDialogFinished(ActionToTake.RESUME_GAME_AT_CURRENT_POINT, -costAmount);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), "You don't have any lives left!!!", Toast.LENGTH_LONG).show();
                    }
                }
            });
            if (currentLivesAmount == 0) {
                noLivesRemainingMessage.setVisibility(View.VISIBLE);
                restartSubLayout.setVisibility(View.GONE);
            } else {
                noLivesRemainingMessage.setVisibility(View.GONE);
                restartSubLayout.setVisibility(View.VISIBLE);
            }

            gameOverCauseTV = (TextView) v.findViewById(R.id.game_over_cause_tv);
            switch (cause) {
                case OUT_OF_TIME: {
                    gameOverCauseTV.setText("Out of Time!");
                    break;
                }
                case OUT_OF_ENERGY: {
                    gameOverCauseTV.setText("Out of Energy!");
                    break;
                }
            }

            return v;
        }

        public void onAttach(Context context) {
            super.onAttach(context);
            if (Build.VERSION.SDK_INT >=23) {
                listener = (OnGameOverDialogFinishedListener) context;
            }
        }

        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (Build.VERSION.SDK_INT < 23) {
                listener = (OnGameOverDialogFinishedListener) activity;
            }
        }
    }
}
