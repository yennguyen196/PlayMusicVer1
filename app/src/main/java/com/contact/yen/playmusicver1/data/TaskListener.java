package com.contact.yen.playmusicver1.data;

import java.util.List;

public interface TaskListener {
    public void onProgressUpdateTaks(Audio audio);
    public void completeTask(List<Audio> audios);

}
