package com.contact.yen.playmusicver1.data;

import android.os.AsyncTask;

import com.contact.yen.playmusicver1.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioTask extends AsyncTask<File, Audio, List<Audio>> {
    private List<Audio> mAudios;
    private TaskListener mTaskListener;
    public static final String TAG_MP3 = ".mp3";
    public static final String TAG_ZING = "zing";
    public static final int IMAGE = R.drawable.music;

    public AudioTask(TaskListener taskListener){
        mTaskListener = taskListener;
        mAudios = new ArrayList<>();
    }

    @Override
    protected List<Audio> doInBackground(File... files) {
        loadAudio(files[0]);
        return mAudios;
    }

    @Override
    protected void onProgressUpdate(Audio... values) {
        super.onProgressUpdate(values);
        mTaskListener.onProgressUpdateTaks(values[0]);
        mAudios.add(values[0]);
    }

    @Override
    protected void onPostExecute(List<Audio> audios) {
        super.onPostExecute(audios);
        mTaskListener.completeTask(audios);
    }

    public List<Audio> loadAudio(File dir){
        File[] listFile = dir.listFiles();
        if(listFile == null)
            return null;
        for (int i = 0; i<listFile.length; i++){
            if(listFile[i].isDirectory()){
                loadAudio(listFile[i]);
            }
            else {
                if(listFile[i].getName().endsWith(TAG_MP3)&& !listFile[i].getName().contains(TAG_ZING)){
                    File file = listFile[i];
                    Audio audio = new Audio();
                    audio.setmImage(IMAGE);
                    audio.setmName(file.getName());
                    audio.setmPath(file.getAbsolutePath());
                    publishProgress(audio);
                }
            }
        }
        return new ArrayList<>();
    }
}
