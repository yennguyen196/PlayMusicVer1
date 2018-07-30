package com.contact.yen.playmusicver1.service;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class ClientServiceManager {

    private Context mContext;
    private Intent mIntent;
    private ServiceConnection mConnection;
    private int mFlags;

    public ClientServiceManager(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    public ClientServiceManager(Context context, Intent intent, ServiceConnection connection, int flags) {
        mContext = context;
        mIntent = intent;
        mConnection = connection;
        mFlags = flags;
    }

    public void startService() {
        mContext.startService(mIntent);
    }

    public void stopService() {
        mContext.stopService(mIntent);
    }

    public void doBindService() {
        mContext.bindService(mIntent, mConnection, mFlags);
    }

    public void doUnbindService() {
        mContext.unbindService(mConnection);
    }

}
