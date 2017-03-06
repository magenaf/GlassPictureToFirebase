package com.example.magena.glasspicturetofirebase;

import android.app.PendingIntent;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

// AppService class controls the LiveCard of the application through manipulating an AppDrawer object (which updates the
// surfaceView). This is possible since AppService enables direct rendering of the liveCard through a RemoteViews object
public class AppService extends Service {
    private static final String TAG = "AppService";

    private AppDrawer mAppDrawer; // in order to affect rendering of mLiveCard directly
    private LiveCard mLiveCard;
    private static AppService mAppService;

    public static AppService appService() {
        return mAppService;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "method: onCreate");
        super.onCreate();
        mAppService = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "method: onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,  "method: onStartCommand");
        if (mLiveCard == null) {
            Log.v(TAG, "Making new LiveCard");
            mLiveCard = new LiveCard(this, "LiveCard");

            // remoteViewOfLiveCard allows the liveCard to be displayed in another process
            RemoteViews remoteViewOfLiveCard = new RemoteViews(getPackageName(), R.layout.live_card);
            mLiveCard.setViews(remoteViewOfLiveCard);

            // AppDrawer can update the surface of the LiveCard since the LiveCard has its view set by a remoteView
            mAppDrawer = new AppDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mAppDrawer);

            Intent i = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, i, 0));

            mLiveCard.publish(PublishMode.REVEAL);
            Log.v(TAG, "LiveCard Published");
        }
        else {
            mLiveCard.navigate();
            Log.v(TAG, "Navigating to LiveCard");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "method: OnDestroy()");

        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.v(TAG, "LiveCard exists and is published");
            if (mAppDrawer != null) {
                mLiveCard.getSurfaceHolder().removeCallback(mAppDrawer);
            }
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        else {
            Log.e(TAG, "OnDestroy: false");
        }
        super.onDestroy();
    }
}