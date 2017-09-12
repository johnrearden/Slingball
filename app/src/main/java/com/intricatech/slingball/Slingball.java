package com.intricatech.slingball;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.intricatech.slingball.util.SystemUiHider;


/**
 * An com.intricatech.swingball2 full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Slingball extends Activity {

    private static final String TAG = "Slingball (Activity)";
    private static final boolean DEBUG = false;

    DifficultyLevel level, levelWhenResumed;
    TitleSurfaceView titleSurfaceView;

    SharedPreferences audioPrefs, permanentPlayerData, levelData, adData;
    SharedPreferences.Editor audioPrefEditor, permanentPlayerDataEditor;
    static final String AUDIO_PREFERENCES = "AUDIO";
    static final String PERMANENT_PLAYER_DATA = "PERMANENT_PLAYER_DATA";
    static final String LEVEL_DATA = "LEVEL_DATA";

    String MUSIC_VOLUME;
    String EFFECTS_VOLUME;
    String VIBRATION;
    String HIGH_SCORE_STRING;
    String LIVES_REMAINING;
    String PREVIOUS_LEVEL;
    String TIMES_INSTRUCTIONS_DISPLAYED;
    String RESUME_ALLOWED_STRING;
    String HARD_LEVEL_AVAILABLE_TAG;
    String NUMBER_GAMES_PLAYED;
    String SHOW_INSTRUCTIONS;
    String SHOULD_PLAY_VOICES;
    String EASY_COMPLETED;
    String NORMAL_COMPLETED;
    String HARD_COMPLETED;

    ImageButton playResumeButton, newGameButton;
    ImageButton ninjaButton, babyButton, mediumButton;
    Button launchInstructionsButton;
    int highScoreValue;
    float soundEffectsVolume;
    float musicVolume;
    boolean isVibrationOn;
    boolean shouldPlayVoices;
    boolean resumeLastGame;
    boolean canResumeLastGame;
    boolean hardLevelAvailable;
    int livesRemaining;
    private boolean showInstructions;
    private int numberOfGamesPlayed;

    private boolean easyCompleted, normalCompleted, hardCompleted;
    private ImageView easyIV, normalIV, hardIV;

    private LinearLayout mainLayout, askUserLayout;
    private TextView gamesPlayedReporter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get SharedPreferences keys from resources.
        MUSIC_VOLUME = getResources().getString(R.string.music_volume_tag);
        EFFECTS_VOLUME = getResources().getString(R.string.effects_volume_tag);
        VIBRATION = getResources().getString(R.string.vibration_tag);
        SHOULD_PLAY_VOICES = "SHOULD_PLAY_VOICES";
        PREVIOUS_LEVEL = getResources().getString(R.string.previous_level_tag);
        HIGH_SCORE_STRING = getResources().getString(R.string.high_score_text);
        TIMES_INSTRUCTIONS_DISPLAYED = getResources().getString(R.string.times_instructions_displayed_tag);
        RESUME_ALLOWED_STRING = getResources().getString(R.string.resume_allowed_string);
        LIVES_REMAINING = getResources().getString(R.string.lives_remaining_tag);
        HARD_LEVEL_AVAILABLE_TAG = getResources().getString(R.string._hard_level_available);
        NUMBER_GAMES_PLAYED = "NUMBER_GAMES_PLAYED";
        SHOW_INSTRUCTIONS = "SHOW_INSTRUCTIONS";
        EASY_COMPLETED = "EASY_COMPLETED";
        NORMAL_COMPLETED = "NORMAL_COMPLETED";
        HARD_COMPLETED = "HARD_COMPLETED";

        // Establish SharedPreferences for audio, high score and game state persistent storage.
        audioPrefs = getSharedPreferences(AUDIO_PREFERENCES, MODE_PRIVATE);
        permanentPlayerData = getSharedPreferences(PERMANENT_PLAYER_DATA, MODE_PRIVATE);
        levelData = getSharedPreferences(LEVEL_DATA, MODE_PRIVATE);
        audioPrefEditor = audioPrefs.edit();
        permanentPlayerDataEditor = permanentPlayerData.edit();

        //canResumeLastGame = levelData.getBoolean(RESUME_ALLOWED_STRING, false);
        int i = audioPrefs.getInt(PREVIOUS_LEVEL, 0);
        level = DifficultyLevel.values()[i];
        livesRemaining = permanentPlayerData.getInt(LIVES_REMAINING, 0);
        showInstructions = permanentPlayerData.getBoolean(SHOW_INSTRUCTIONS, true);
        numberOfGamesPlayed = permanentPlayerData.getInt(NUMBER_GAMES_PLAYED, 0);


        resumeLastGame = false;

        setContentView(R.layout.activity_launch_screen);
        titleSurfaceView = (TitleSurfaceView) findViewById(R.id.title1);
        titleSurfaceView.continueRenderingTitle = true;

    }

    @Override
    protected void onResume() {
        super.onResume();

        hardLevelAvailable = audioPrefs.getBoolean(HARD_LEVEL_AVAILABLE_TAG, false);
        easyCompleted = permanentPlayerData.getBoolean(EASY_COMPLETED, false);
        normalCompleted = permanentPlayerData.getBoolean(NORMAL_COMPLETED, false);

        // Change appearance of resumeButton depending on flag.
        canResumeLastGame = levelData.getBoolean(RESUME_ALLOWED_STRING, false);
        levelWhenResumed = DifficultyLevel.values()[audioPrefs.getInt(PREVIOUS_LEVEL, 0)];
        initializeViewElements();
        hideInstructionsChoices();

        titleSurfaceView.onResume();
        highScoreValue = permanentPlayerData.getInt(PERMANENT_PLAYER_DATA, 0);
    }

    private void loadPrefs() {
        soundEffectsVolume = audioPrefs.getFloat(EFFECTS_VOLUME, 50);
        musicVolume = audioPrefs.getFloat(MUSIC_VOLUME, 70);
        isVibrationOn = audioPrefs.getBoolean(VIBRATION, true);
        shouldPlayVoices = audioPrefs.getBoolean(SHOULD_PLAY_VOICES, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        titleSurfaceView.onPause();
        savePreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        titleSurfaceView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onBabyButtonClick(View view) {
        level = DifficultyLevel.EASY;
        babyButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_icon_selected));
        ninjaButton.setImageDrawable(
                hardLevelAvailable ?
                        getResources().getDrawable(R.drawable.ninjaicon)
                        : getResources().getDrawable(R.drawable.ninjaicon_locked)
        );
        mediumButton.setImageDrawable(getResources().getDrawable(R.drawable.medium_icon));
        setResumeGameButtonVisibility();
    }

    public void onMediumButtonClick(View view) {
        level = DifficultyLevel.NORMAL;
        mediumButton.setImageDrawable(getResources().getDrawable(R.drawable.medium_icon_selected));
        babyButton.setImageDrawable(getResources().getDrawable(R.drawable.babyicon));
        ninjaButton.setImageDrawable(
                hardLevelAvailable ?
                        getResources().getDrawable(R.drawable.ninjaicon)
                        : getResources().getDrawable(R.drawable.ninjaicon_locked)
        );
        setResumeGameButtonVisibility();
    }

    public void onNinjaButtonClick(View view) {
        if (hardLevelAvailable) {
            level = DifficultyLevel.HARD;
            ninjaButton.setImageDrawable(getResources().getDrawable(R.drawable.ninja_icon_selected));
            babyButton.setImageDrawable(getResources().getDrawable(R.drawable.babyicon));
            mediumButton.setImageDrawable(getResources().getDrawable(R.drawable.medium_icon));
        } else {
            Toast.makeText(this, "Get to Level 10 on MEDIUM to unlock Hard level, or buy it in the shop!", Toast.LENGTH_SHORT).show();
        }
        setResumeGameButtonVisibility();
    }

    public void onLaunchInstructionsButtonClick(View view) {
        Intent intent = new Intent(this, InstructionActivity.class);
        startActivity(intent);
    }

    public void onSettingsButtonClick(View view) {
        showSettingsDialog();
    }

    public void onPlayResumeGameButtonClick(View view) {
        if (showInstructions) {
            revealInstructionsChoices();
        } else {
            setResumeAllowedFlag();
            startGame();
        }
    }

    private void setResumeAllowedFlag() {
        if (canResumeLastGame && level.equals(levelWhenResumed)) {
            resumeLastGame = true;
        } else {
            resumeLastGame = false;
        }
    }

    public void onNewGameButtonClick(View view) {
        if (showInstructions) {
            revealInstructionsChoices();
        } else {
            resumeLastGame = false;
            startGame();
        }
    }

    private void showSettingsDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack("dialog");

        DialogFragment newFragment = SettingsDialogFragment.newInstance(
                isVibrationOn,
                musicVolume,
                soundEffectsVolume,
                shouldPlayVoices
        );

        newFragment.show(ft, "dialog");
        /*getFragmentManager().popBackStack("dialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Log.d(TAG, "popped fragment from back stack");*/
    }


    public void onGotoShopButtonClicked(View view) {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra("RESUME_LAST_GAME", false);
        intent.putExtra("CAME_FROM_GAMEOVER", false);
        startActivity(intent);
    }

    private void revealInstructionsChoices() {
        mainLayout.setVisibility(View.GONE);
        askUserLayout.setVisibility(View.VISIBLE);
    }

    private void hideInstructionsChoices() {
        mainLayout.setVisibility(View.VISIBLE);
        askUserLayout.setVisibility(View.GONE);
    }

    public void yesInstructClick(View view) {
        numberOfGamesPlayed++;
        savePreferences();
        Intent intent = new Intent(this, InstructionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("PROCEED_TO_GAME", true);
        intent.putExtra("RESUME_LAST_GAME", resumeLastGame);
        startActivity(intent);
    }

    public void notNowInstructClick(View view) {
        setResumeAllowedFlag();
        startGame();
    }

    public void neverInstructClick(View view) {
        permanentPlayerDataEditor.putBoolean(SHOW_INSTRUCTIONS, false);
        permanentPlayerDataEditor.commit();
        showInstructions = false;
        setResumeAllowedFlag();
        startGame();
    }

    public void neverSayNever(View view) {
        showInstructions = true;
        permanentPlayerDataEditor.putBoolean(SHOW_INSTRUCTIONS, true);
        permanentPlayerDataEditor.commit();
    }

    private void initializeViewElements() {

        highScoreValue = permanentPlayerData.getInt(PERMANENT_PLAYER_DATA, 0);
        //float defaultTextSize = ((TextView) findViewById(R.id.music_vol_text)).getTextSize();
        //highScoreTextView.setText(HIGH_SCORE_STRING + " : " + String.valueOf(highScoreValue));
        //highScoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize * 1.5f);
        loadPrefs();

        playResumeButton = (ImageButton) findViewById(R.id.play_resume_button);
        newGameButton = (ImageButton) findViewById(R.id.new_game_button);

        setResumeGameButtonVisibility();

        babyButton = (ImageButton) findViewById(R.id.baby_button);
        if (level == DifficultyLevel.EASY) {
            babyButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_icon_selected));
        } else {
            babyButton.setImageDrawable(getResources().getDrawable(R.drawable.babyicon));
        }

        ninjaButton = (ImageButton) findViewById(R.id.ninja_button);
        if (level == DifficultyLevel.HARD && hardLevelAvailable) {
            ninjaButton.setImageDrawable(getResources().getDrawable(R.drawable.ninja_icon_selected));
        } else if (hardLevelAvailable){
            ninjaButton.setImageDrawable(getResources().getDrawable(R.drawable.ninjaicon));
        } else {
            ninjaButton.setImageDrawable(getResources().getDrawable(R.drawable.ninjaicon_locked));
        }
        if (level == DifficultyLevel.HARD && !hardLevelAvailable) {
            ninjaButton.setImageDrawable(getResources().getDrawable(R.drawable.ninjaicon_locked));
            level = DifficultyLevel.NORMAL;
        } // just in case.

        mediumButton = (ImageButton) findViewById(R.id.medium_button);
        if (level == DifficultyLevel.NORMAL) {
            mediumButton.setImageDrawable(getResources().getDrawable(R.drawable.medium_icon_selected));
        } else {
            mediumButton.setImageDrawable(getResources().getDrawable(R.drawable.medium_icon));
        }

        mainLayout = (LinearLayout) findViewById(R.id.vertical_god);
        askUserLayout = (LinearLayout) findViewById(R.id.ask_user_instruct_panel);

        gamesPlayedReporter = (TextView) findViewById(R.id.games_played_reporter);
        gamesPlayedReporter.setText("Games played == " + numberOfGamesPlayed);
        if (IntRepConsts.IS_RELEASE_VERSION) {
            gamesPlayedReporter.setVisibility(View.GONE);
        }



        easyIV = (ImageView) findViewById(R.id.baby_button_completed_imageview);
        easyIV.setVisibility(
                easyCompleted ? View.VISIBLE : View.GONE
        );
        normalIV = (ImageView) findViewById(R.id.medium_button_completed_imageview);
        normalIV.setVisibility(
                normalCompleted ? View.VISIBLE : View.GONE
        );
        hardIV = (ImageView) findViewById(R.id.hard_button_completed_imageview);
        hardIV.setVisibility(
                hardCompleted ? View.VISIBLE : View.GONE
        );
    }

    public void startGame() {
        // Before starting the game, save the preferences.
        numberOfGamesPlayed++;
        savePreferences();

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("RESUME_LAST_GAME", resumeLastGame);
        startActivity(intent);
    }

    private void savePreferences() {

        audioPrefEditor.putFloat(MUSIC_VOLUME, musicVolume);
        audioPrefEditor.putFloat(EFFECTS_VOLUME, soundEffectsVolume);
        audioPrefEditor.putBoolean(VIBRATION, isVibrationOn);
        audioPrefEditor.putBoolean(SHOULD_PLAY_VOICES, shouldPlayVoices);
        audioPrefEditor.putInt(PREVIOUS_LEVEL, level.ordinal());
        audioPrefEditor.commit();

        permanentPlayerDataEditor.putInt(NUMBER_GAMES_PLAYED, numberOfGamesPlayed);
        permanentPlayerDataEditor.commit();

        if (showInstructions && numberOfGamesPlayed > IntRepConsts.NUMBER_OF_GAMES_WITH_AUTO_INSTRUCTIONS_SHOW) {
            showInstructions = false;
            permanentPlayerDataEditor.putBoolean(SHOW_INSTRUCTIONS, false);
            permanentPlayerDataEditor.commit();
        }
    }

    void savePreferencesFromDialog(
            boolean vib,
            float mus,
            float eff,
            boolean voices) {
        audioPrefEditor.putFloat(MUSIC_VOLUME, mus);
        audioPrefEditor.putFloat(EFFECTS_VOLUME, eff);
        audioPrefEditor.putBoolean(VIBRATION, vib);
        audioPrefEditor.putBoolean(SHOULD_PLAY_VOICES, voices);
        audioPrefEditor.commit();
        getFragmentManager().popBackStack("dialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Log.d(TAG, "popped fragment from back stack");
    }






    public static class SettingsDialogFragment extends DialogFragment {

        SeekBar soundEffectsSeekBar1;
        SeekBar musicSeekBar1;
        CheckBox vibrationCheckbox1;
        CheckBox voicesCheckbox;
        Button closeButton;
        View contentView;
        SharedPreferences.Editor editor;

        static final String VIBRATE_ARG = "VIBRATE_ARG";
        static final String MUSIC_VOL_ARG = "MUSIC_VOL_ARG";
        static final String EFFECT_VOL_ARG = "EFFECT_VOL_ARG";
        static final String SHOULD_PLAY_VOICES = "SHOULD_PLAY_VOICES";

        static SettingsDialogFragment newInstance(
                boolean vibrationOn,
                float musicVolume,
                float effectsVolume,
                boolean shouldPlayVoices) {

            SettingsDialogFragment frag = new SettingsDialogFragment();

            Bundle args = new Bundle();
            args.putBoolean(VIBRATE_ARG, vibrationOn);
            args.putFloat(MUSIC_VOL_ARG, musicVolume);
            args.putFloat(EFFECT_VOL_ARG, effectsVolume);
            args.putBoolean(SHOULD_PLAY_VOICES, shouldPlayVoices);
            frag.setArguments(args);
            

            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            editor = getActivity().getSharedPreferences("", MODE_PRIVATE).edit();
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.settings_dialog, container, false);
            contentView = v;

            soundEffectsSeekBar1 = (SeekBar) v.findViewById(R.id.sound_effects_volume);
            soundEffectsSeekBar1.setProgress(
                    (int) getArguments().getFloat(EFFECT_VOL_ARG)
            );
            musicSeekBar1 = (SeekBar) v.findViewById(R.id.music_volume);
            musicSeekBar1.setProgress(
                    (int) getArguments().getFloat(MUSIC_VOL_ARG)
            );
            vibrationCheckbox1 = (CheckBox) v.findViewById(R.id.vibration_checkbox);
            vibrationCheckbox1.setChecked(getArguments().getBoolean(VIBRATE_ARG));

            voicesCheckbox = (CheckBox) v.findViewById(R.id.voices_checkbox);
            voicesCheckbox.setChecked(getArguments().getBoolean(SHOULD_PLAY_VOICES));

            closeButton = (Button) v.findViewById(R.id.close_button);

            soundEffectsSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    getArguments().putFloat(EFFECT_VOL_ARG, (float) progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            musicSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    getArguments().putFloat(MUSIC_VOL_ARG, (float) progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            vibrationCheckbox1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    getArguments().putBoolean(VIBRATE_ARG, cb.isChecked());
                }
            });

            voicesCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    getArguments().putBoolean(SHOULD_PLAY_VOICES, cb.isChecked());
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            Rect displayRectangle = new Rect();
            Window window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            v.setMinimumWidth((int) (displayRectangle.width() * 0.8f));

            return v;
        }

        public void onDismiss(DialogInterface dialogInterface) {
            Log.d(TAG, "fragment.onDismiss() invoked.");
            Slingball enclosingActivity = (Slingball)getActivity();
            enclosingActivity.savePreferencesFromDialog(
                    getArguments().getBoolean(VIBRATE_ARG),
                    getArguments().getFloat(MUSIC_VOL_ARG),
                    getArguments().getFloat(EFFECT_VOL_ARG),
                    getArguments().getBoolean(SHOULD_PLAY_VOICES)
            );
            enclosingActivity.loadPrefs();

        }
    }

    private void setResumeGameButtonVisibility() {
        if (canResumeLastGame && level.equals(levelWhenResumed)) {
            playResumeButton.setBackground(getResources().getDrawable(R.drawable.resume_icon));
            newGameButton.setVisibility(View.VISIBLE);
        } else {
            playResumeButton.setBackground(getResources().getDrawable(R.drawable.play_icon));
            newGameButton.setVisibility(View.GONE);
        }
    }


}
