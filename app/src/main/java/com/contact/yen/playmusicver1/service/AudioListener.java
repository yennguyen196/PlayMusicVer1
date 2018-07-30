package com.contact.yen.playmusicver1.service;

import com.contact.yen.playmusicver1.data.Audio;


import java.util.List;

public interface AudioListener {

    void onTitleAudio(String title);

    void onTotalTime(int totalTime);

    void onCurrentTime(int currentTime);

    void onPauseAudio();

    void onStartAudio();

    void onUpdateAudio(Audio audio);

    void onAudiosUpdate(List<Audio> audios);
}
