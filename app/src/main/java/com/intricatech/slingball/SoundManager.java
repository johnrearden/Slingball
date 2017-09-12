package com.intricatech.slingball;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Bolgbolg on 17/07/2016.
 */
public class SoundManager implements MediaPlayer.OnPreparedListener {

    private final String TAG;

    SoundPool soundPool;
    MediaPlayer mediaPlayer1;
    boolean mediaPlayer1IsPaused;
    AudioManager audioManager;

    float musicVolume, effectsVolume;
    private boolean playVoices;

    int musicID;
    int targetCollision0ID;
    int zapID;
    int timerAlertID;
    int evilLaughID;
    int powerUpID;
    int autopilotID;
    int takeOverVoiceID;
    int rewardAvailableID;
    int metallicImpactID;
    int tadpoleShieldedImpactID;
    int tenMoreSecondsID;
    int twentyMoreSecondsID;
    int blockersBegoneID;
    int protectMeID;
    int dontTouchTheEdgeID;
    int youreOnFireID;
    int soCloseID;

    int musicPriority;
    int targetCollisionPriority;
    int zapPriority;
    int timerAlertPriority;
    int evilLaughPriority;
    int powerUpPriority;
    int autopilotPriority;
    int takeOverVoicePriority;
    int rewardAvailablePriority;
    int metallicImpactPriority;
    int tadpoleShieldedPriority;
    int tenMoreSecondsPriority;
    int twentyMoreSecondsPriority;
    int blockersBegonePriority;
    int protectMePriority;
    int dontTouchTheEdgePriority;
    int youreOnFirePriority;
    int soClosePriority;

    int timerAlertStreamID;

    boolean isTimerPlaying;

    static final int MAX_STREAMS = 8;

    SoundManager (Context context,
                  float musVol,
                  float effectsVol,
                  boolean shouldPlayVoices) {

        TAG = getClass().getSimpleName();

        this.musicVolume = musVol;
        this.effectsVolume = effectsVol;

        musicVolume = musicVolume * 0.01f;
        effectsVolume = effectsVolume * 0.01f;
        this.playVoices = shouldPlayVoices;

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        initializeMediaPlayer(context, musicVolume);
        new LoadSoundFiles(context).execute();

    }

    private class LoadSoundFiles extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        LoadSoundFiles(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            targetCollisionPriority = 1;
            targetCollision0ID = soundPool.load(context, R.raw.targetcollision0, targetCollisionPriority);

            zapPriority = 1;
            zapID = soundPool.load(context, R.raw.electriczap, zapPriority);

            timerAlertPriority = 1;
            timerAlertStreamID = 0;
            timerAlertID = soundPool.load(context, R.raw.timer_alert, timerAlertPriority);
            isTimerPlaying = false;

            evilLaughPriority = 1;
            evilLaughID = soundPool.load(context, R.raw.evil_laugh, evilLaughPriority);

            powerUpPriority = 1;
            powerUpID = soundPool.load(context, R.raw.power_up, powerUpPriority);

            autopilotPriority = 1;
            autopilotID = soundPool.load(context, R.raw.autopilot_engage, autopilotPriority);

            takeOverVoicePriority = 1;
            takeOverVoiceID = soundPool.load(context, R.raw.take_over_voice, takeOverVoicePriority);

            rewardAvailablePriority = 1;
            rewardAvailableID = soundPool.load(context, R.raw.reward_available, rewardAvailablePriority);

            metallicImpactPriority = 1;
            metallicImpactID = soundPool.load(context, R.raw.circle_impact, metallicImpactPriority);

            tadpoleShieldedPriority = 1;
            tadpoleShieldedImpactID = soundPool.load(context, R.raw.tadpole_shielded_impact, tadpoleShieldedPriority);

            tenMoreSecondsPriority = 1;
            tenMoreSecondsID = soundPool.load(context, R.raw.ten_more_seconds, tenMoreSecondsPriority);

            twentyMoreSecondsPriority = 1;
            twentyMoreSecondsID = soundPool.load(context, R.raw.ten_more_seconds, twentyMoreSecondsPriority);

            blockersBegonePriority = 1;
            blockersBegoneID = soundPool.load(context, R.raw.blockers_begone, blockersBegonePriority);

            protectMePriority = 1;
            protectMeID = soundPool.load(context, R.raw.protect_me, protectMePriority);

            dontTouchTheEdgePriority = 1;
            dontTouchTheEdgeID = soundPool.load(context, R.raw.dont_touch_the_edge, dontTouchTheEdgePriority);

            youreOnFirePriority = 1;
            youreOnFireID = soundPool.load(context, R.raw.youre_on_fire, youreOnFirePriority);

            soClosePriority = 1;
            soCloseID = soundPool.load(context, R.raw.so_close, soClosePriority);

            return true;
        }
    }

    public void initializeMediaPlayer(Context context, float musicVolume) {

        mediaPlayer1 = new MediaPlayer();
        mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/quick_slinger_theme_2");
        mediaPlayer1.setOnPreparedListener(this);
        mediaPlayer1.setLooping(true);
        mediaPlayer1.setVolume(musicVolume, musicVolume);
        mediaPlayer1IsPaused = false;
        try {
            mediaPlayer1.setDataSource(context, uri);
            mediaPlayer1.prepareAsync();
        } catch (IOException e) {
            Log.d(TAG, "mp3 does not exist");
            e.printStackTrace();
        }
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        playMusic();
    }

    void playMusic() {
        mediaPlayer1.start();
    }

    void playTargetCollision(int consecutiveHits) {
        soundPool.play(
                targetCollision0ID,
                effectsVolume,
                effectsVolume,
                targetCollisionPriority,
                0,
                1.0f
        );
    }

    void playCircleCollision() {
        soundPool.play(
                zapID,
                effectsVolume,
                effectsVolume,
                zapPriority,
                0,
                1.0f
        );
    }

    void playMetallicCollision() {
        soundPool.play(
                metallicImpactID,
                effectsVolume,
                effectsVolume,
                metallicImpactPriority,
                0,
                1.0f
        );
    }

    void playTimerAlert() {
        if (!isTimerPlaying) {
            timerAlertStreamID = soundPool.play(
                    timerAlertID,
                    effectsVolume,
                    effectsVolume,
                    timerAlertPriority,
                    -1,
                    1.0f
            );
            isTimerPlaying = true;
        }

    }

    void stopTimerAlert() {
        soundPool.stop(
                timerAlertStreamID
        );
        isTimerPlaying = false;
        timerAlertStreamID = 0;
    }

    void playEvilLaugh() {
        if (playVoices) {
            soundPool.play(
                    evilLaughID,
                    effectsVolume,
                    effectsVolume,
                    evilLaughPriority,
                    0,
                    1.0f
            );
        }
    }

    void playPowerUpVoice() {
        if (playVoices) {
            soundPool.play(
                    powerUpID,
                    effectsVolume,
                    effectsVolume,
                    powerUpPriority,
                    0,
                    1.0f
            );
        }
    }

    void playAutopilotVoice() {
        if (playVoices) {
            soundPool.play(
                    autopilotID,
                    effectsVolume,
                    effectsVolume,
                    autopilotPriority,
                    0,
                    1.0f
            );
        }
    }

    void playTakeOverVoice() {
        if (playVoices) {
            soundPool.play(
                    takeOverVoiceID,
                    effectsVolume,
                    effectsVolume,
                    takeOverVoicePriority,
                    0,
                    1.0f
            );
        }
    }

    void playRewardAvailableSound() {
        soundPool.play(
                rewardAvailableID,
                effectsVolume,
                effectsVolume,
                rewardAvailablePriority,
                0,
                1.0f
        );
    }

    void playTadpoleShieldedImpactSound() {
        soundPool.play(
                tadpoleShieldedImpactID,
                effectsVolume,
                effectsVolume,
                tadpoleShieldedPriority,
                0,
                1.0f
        );
    }

    void playTenMoreSeconds() {
        if (playVoices) {
            soundPool.play(
                    tenMoreSecondsID,
                    effectsVolume,
                    effectsVolume,
                    tenMoreSecondsPriority,
                    0,
                    1.0f
            );
        }
    }

    void playTwentyMoreSeconds() {
        if (playVoices) {
            soundPool.play(
                    twentyMoreSecondsID,
                    effectsVolume,
                    effectsVolume,
                    twentyMoreSecondsPriority,
                    0,
                    1.0f
            );
        }
    }

    void playBlockersBegone() {
        if (playVoices) {
            soundPool.play(
                    blockersBegoneID,
                    effectsVolume,
                    effectsVolume,
                    blockersBegonePriority,
                    0,
                    1.0f
            );
        }
    }

    void playProtectMe() {
        if (playVoices) {
            soundPool.play(
                    protectMeID,
                    effectsVolume,
                    effectsVolume,
                    protectMePriority,
                    0,
                    1.0f
            );
        }
    }

    void playDontTouchTheEdge() {
        if (playVoices) {
            soundPool.play(
                    dontTouchTheEdgeID,
                    effectsVolume,
                    effectsVolume,
                    dontTouchTheEdgePriority,
                    0,
                    1.0f
            );
        }
    }

    void playYoureOnFire() {
        if (playVoices) {
            soundPool.play(
                    youreOnFireID,
                    effectsVolume,
                    effectsVolume,
                    youreOnFirePriority,
                    0,
                    1.0f
            );
        }
    }

    void playSoClose() {
        if (playVoices) {
            soundPool.play(
                    soCloseID,
                    effectsVolume,
                    effectsVolume,
                    soClosePriority,
                    0,
                    1.0f
            );
        }
    }

    void pauseAll() {
        mediaPlayer1.pause();
        mediaPlayer1IsPaused = true;
        soundPool.autoPause();
    }

    void stopAndRelease() {
        mediaPlayer1.stop();
        mediaPlayer1IsPaused = false;
        /*soundPool.release();
        soundPool = null;*/
    }

    void resumeAll() {
        if (mediaPlayer1IsPaused) {
            mediaPlayer1.start();
        }
        soundPool.autoResume();
    }

}
