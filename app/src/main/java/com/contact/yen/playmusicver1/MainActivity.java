package com.contact.yen.playmusicver1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.contact.yen.playmusicver1.data.Audio;
import com.contact.yen.playmusicver1.data.AudioAdapter;
import com.contact.yen.playmusicver1.data.ItemListener;
import com.contact.yen.playmusicver1.service.AudioListener;
import com.contact.yen.playmusicver1.service.AudioService;
import com.contact.yen.playmusicver1.service.ClientServiceManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AudioListener,
        SeekBar.OnSeekBarChangeListener, ItemListener {

    public static final String ACTION_BIND_SERVICE = "ACTION_BIND_SERVICE";
    private static final int REQUEST_CODE = 100;

    private LinearLayout mLinearLayoutPlayer;
    private RecyclerView mRecyclerView;
    private ImageButton mButtonPrevious;
    private ImageButton mButtonPlayPause;
    private ImageButton mButtonNext;
    private TextView mTextViewCurrentTime;
    private TextView mTextViewTotalTime;
    private SeekBar mSeekBar;
    private AudioAdapter mAudioAdapter;
    private boolean mBound = false;
    private boolean mTrackingSeekBar = false;
    private Intent mIntent;
    private AudioService mAudioService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mAudioService = ((AudioService.AudioBinder) iBinder).getService();
            mAudioService.setAudioListener(MainActivity.this);
            mBound = true;
            initStatePlayer(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
    private ClientServiceManager mServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

//    private void checkpermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                initService();
//            } else {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
//            }
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initService();
        } else
            finish();
    }

    @Override
    protected void onStart() {
        initService();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mServiceManager.doUnbindService();
        mBound = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!mAudioService.isAudioPlaying()) {
            mServiceManager.stopService();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_button_play_pause:
                if (mBound) {
                    if (mAudioService.isAudioPlaying()) {
                        mAudioService.pauseAudio();
                    } else {
                        mAudioService.startAudio();
                    }
                }
                break;
            case R.id.image_button_next:
                if (mBound) {
                    mAudioService.nextAudio();
                }
                break;
            case R.id.image_button_previous:
                if (mBound) {
                    mAudioService.previousAudio();
                }
                break;
        }
    }

    @Override
    public void onTitleAudio(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onTotalTime(int totalTime) {
        mTextViewTotalTime.setText(TimeUtils.convertMilisecondToFormatTime(totalTime));
        initSeekBar(totalTime);
    }

    @Override
    public void onCurrentTime(int currentTime) {
        mTextViewCurrentTime.setText(TimeUtils.convertMilisecondToFormatTime(currentTime));
        if (!mTrackingSeekBar) {
            mSeekBar.setProgress(currentTime);
        }
    }

    @Override
    public void onPauseAudio() {
        mButtonPlayPause.setImageResource(R.drawable.ic_launcher_background);
    }

    @Override
    public void onStartAudio() {
        mButtonPlayPause.setImageResource(R.drawable.ic_launcher_background);
    }

//    @Override
//    public void onUpdateAudio(Audio audio) {
//
//    }

    @Override
    public void onUpdateAudio(Audio audio) {
        mAudioAdapter.updateAudios(audio);
        mLinearLayoutPlayer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAudiosUpdate(List<Audio> audios) {
        mAudioAdapter.setAudios(audios);
        mLinearLayoutPlayer.setVisibility(View.VISIBLE);
    }

//    @Override
//    public void onUpdateAudios(List<Audio> audios) {
//        mAudioAdapter.setAudios(audios);
//        mLinearLayoutPlayer.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingSeekBar = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingSeekBar = false;
        mAudioService.seekToAudio(seekBar.getProgress());
        if (!mAudioService.isAudioPlaying()) {
            mAudioService.startAudio();
        }
    }


    private void init() {
        mLinearLayoutPlayer = findViewById(R.id.linear_player);
        mRecyclerView = findViewById(R.id.recycler_view);
        mButtonPrevious = findViewById(R.id.image_button_previous);
        mButtonPlayPause = findViewById(R.id.image_button_play_pause);
        mButtonNext = findViewById(R.id.image_button_next);
        mTextViewCurrentTime = findViewById(R.id.text_view_current_time);
        mTextViewTotalTime = findViewById(R.id.text_view_total_time);
        mSeekBar = findViewById(R.id.seek_bar);

        mAudioAdapter = new AudioAdapter(this,this);
        mRecyclerView.setAdapter(mAudioAdapter);

        mButtonPlayPause.setOnClickListener(this);
        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        mLinearLayoutPlayer.setVisibility(View.GONE);

        mIntent = new Intent(this, AudioService.class);
        mIntent.setAction(ACTION_BIND_SERVICE);
        mServiceManager = new ClientServiceManager(getApplicationContext(), mIntent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void initService() {
        mServiceManager.startService();
        mServiceManager.doBindService();
    }

    private void initStatePlayer(Intent intent) {
        mAudioService.getStateAudio();
    }

    private void initSeekBar(int totalTime) {
        mSeekBar.setMax(totalTime);
    }

    @Override
    public void setOnclickListener(int position) {
        mAudioService.startPlay(position);
    }
}
