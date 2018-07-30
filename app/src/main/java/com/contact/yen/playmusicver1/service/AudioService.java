package com.contact.yen.playmusicver1.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.contact.yen.playmusicver1.MainActivity;
import com.contact.yen.playmusicver1.R;
import com.contact.yen.playmusicver1.data.Audio;
import com.contact.yen.playmusicver1.data.AudioTask;
import com.contact.yen.playmusicver1.data.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, TaskListener {

    private final IBinder mBinder = new AudioBinder();
    private AudioListener mAudioListener;
    private boolean mIsRunning                      = false;
    private int currentAudioPosition                = 0;
    private boolean mIsPlayByClient                 = false;
    private boolean mIsCompleteLoadAudios           = false;
    private static final String ACTION_PREVIOUS     = "ACTION_PREVIOUS";
    private static final String ACTION_PLAY         = "ACTION_PLAY";
    private static final String ACTION_PAUSE        = "ACTION_PAUSE";
    private static final String ACTION_NEXT         = "ACTION_NEXT";
    private static final String ID_CHANNEL          = "ID_CHANNEL";
    private static final int ID_NOTIFICATION        = 1;

    private List<Audio> mAudios;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private Runnable mRunnableCurrentTime = new Runnable() {
        @Override
        public void run() {
            mAudioListener.onCurrentTime(mMediaPlayer.getCurrentPosition());
            if (isAudioPlaying()) {
                mHandler.postDelayed(this, 500);
        }
        }
    };

    @Override
    public void onProgressUpdateTaks(Audio audio) {
        postUpdateAudioTask(audio);
        mAudios.add(audio);
    }

    @Override
    public void completeTask(List<Audio> audios) {
        mIsCompleteLoadAudios = true;
        if (audios != null && audios.size() != 0) {
            initAudio(audios.get(0));
            postTitleAudio();
            postTotalTime();
            postCurrentTime();
        }
    }

    public void setAudioListener(MainActivity mainActivity) {
        mAudioListener = mainActivity;

    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        mIsRunning = true;
        loadAudios();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        cancelUpdateClient();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mIsRunning = false;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (currentAudioPosition < mAudios.size() - 1) {
            currentAudioPosition++;
            initAudio(mAudios.get(currentAudioPosition));
        } else if (currentAudioPosition == mAudios.size() - 1) {
            currentAudioPosition = 0;
            initAudio(mAudios.get(currentAudioPosition));
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        postTitleAudio();
        postTotalTime();
        postCurrentTime();
        if (mIsPlayByClient) {
            mMediaPlayer.start();
            postPlayAudio();
            updateNotification(ACTION_PAUSE);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mMediaPlayer.reset();
        return false;
    }

    private void loadAudios() {
        mAudios = new ArrayList<>();
        File file = Environment.getExternalStorageDirectory();
        AudioTask task = new AudioTask(this);
        task.execute(file);
    }

    /**
     * init media player when services was created
     */
    private void initMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    /**
     *
     * @param audio
     * set data source and prepare
     */
    private void initAudio(Audio audio) {
        initMediaPlayer();
        Uri uri = Uri.parse(audio.getmPath());
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postTitleAudio() {
        mAudioListener.onTitleAudio(mAudios.get(currentAudioPosition).getmName());
    }

    private void postTotalTime() {
        mAudioListener.onTotalTime(mMediaPlayer.getDuration());
    }

    private void postCurrentTime() {
        mHandler.post(mRunnableCurrentTime);
    }

    private void postPlayAudio() {
        mAudioListener.onStartAudio();
    }

    private void postPauseAudio() {
        mAudioListener.onPauseAudio();
    }

    private void postUpdateAudioTask(Audio audio) {
        mAudioListener.onUpdateAudio(audio);
    }

    private void postUpdateAudios(List<Audio> audios) {
        mAudioListener.onAudiosUpdate(audios);
    }

    private void cancelUpdateClient() {
        mHandler.removeCallbacks(mRunnableCurrentTime);
    }

    private void handleIntent(Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case ACTION_PLAY:
                startAudio();
                break;
            case ACTION_PAUSE:
                pauseAudio();
                break;
            case ACTION_NEXT:
                nextAudio();
                break;
            case ACTION_PREVIOUS:
                previousAudio();
                break;
        }
    }

    private void updateNotification(String intentAction) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseStartIntent = new Intent(this, AudioService.class);
        Intent nextIntent = new Intent(this, AudioService.class);
        Intent previousIntent = new Intent(this, AudioService.class);

        pauseStartIntent.setAction(intentAction);
        nextIntent.setAction(ACTION_NEXT);
        previousIntent.setAction(ACTION_PREVIOUS);

        PendingIntent pauseStartPendingIntent = PendingIntent.getService(this, 0,
                pauseStartIntent, 0);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);


        RemoteViews remoteViewLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_pause);

        switch (intentAction) {
            case ACTION_PLAY:
                remoteViewLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_play);
                remoteViewLayout.setOnClickPendingIntent(R.id.image_button_play, pauseStartPendingIntent);
                break;
            case ACTION_PAUSE:
                remoteViewLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_pause);
                remoteViewLayout.setOnClickPendingIntent(R.id.image_button_pause, pauseStartPendingIntent);
                break;
        }

        remoteViewLayout.setOnClickPendingIntent(R.id.image_button_next, nextPendingIntent);
        remoteViewLayout.setOnClickPendingIntent(R.id.image_button_previous, previousPendingIntent);

        Notification notification = new NotificationCompat.Builder(this, ID_CHANNEL)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContent(remoteViewLayout)
                .build();

        startForeground(ID_NOTIFICATION, notification);
    }

    /**
     *
     * @param position
     */
    public void startPlay(int position) {
        currentAudioPosition = position;
        mIsPlayByClient = true;
        initAudio(mAudios.get(position));
    }

    /**
     * start audio, update current time, UI player, notification
     */
    public void startAudio() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
        postCurrentTime();
        postPlayAudio();
        mIsPlayByClient = true;
        updateNotification(ACTION_PAUSE);
    }

    /**
     * pause audio, update UI player, notification
     */
    public void pauseAudio() {
        mMediaPlayer.pause();
        postPauseAudio();
        updateNotification(ACTION_PLAY);
    }

    /**
     *
     * @param msec
     */
    public void seekToAudio(int msec) {
        mMediaPlayer.seekTo(msec);
    }

    /**
     * next audio
     */
    public void nextAudio() {
        if (currentAudioPosition < mAudios.size() - 1) {
            mIsPlayByClient = true;
            currentAudioPosition++;
            initAudio(mAudios.get(currentAudioPosition));
        }
    }

    /**
     * previous audio
     */
    public void previousAudio() {
        if (currentAudioPosition > 0) {
            mIsPlayByClient = true;
            currentAudioPosition--;
            initAudio(mAudios.get(currentAudioPosition));
        }
    }

    /**
     * update state: current time, total time, UI player
     */

    public void getStateAudio() {

        if (mIsCompleteLoadAudios) {
            postTitleAudio();
            postTotalTime();
            postCurrentTime();
            if (isAudioPlaying()) {
                postPlayAudio();
            } else {
                postPauseAudio();
            }

            postUpdateAudios(mAudios);
        }
    }

    /**
     *
     * @return true if media player is running
     */
    public boolean isAudioPlaying() {
        return (mMediaPlayer != null) && mMediaPlayer.isPlaying();
    }

    public boolean isRunning() {
        return mIsRunning;
    }

//    public void setAudioListener(MainActivity activity) {
//        mAudioListener = activity;
//    }
}
