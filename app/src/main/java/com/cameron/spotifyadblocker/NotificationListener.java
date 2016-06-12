package com.cameron.spotifyadblocker;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cameron on 6/7/2016.
 */
public class NotificationListener extends NotificationListenerService {
    private boolean muted;
    private int volume;
    private static Timer timer;
    private HashSet<String> blocklist;

    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        timer = new Timer();
        blocklist = new HashSet<String>();
        muted = false;
        volume = 0;
        // Load blocklist
        Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(R.raw.blocklist);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                blocklist.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                StatusBarNotification[] notifications = getActiveNotifications();
                Notification notification = new Notification();
                boolean foundNotification = false;
                if (notifications != null) {
                    // Find which notification is Spotify
                    for (int i = 0; i < notifications.length; ++i) {
                        String name = notifications[i].getPackageName();
                        if (name.contains("spotify")) {
                            Log.d("DEBUG", name);
                            notification = notifications[i].getNotification();
                            foundNotification = true;
                            break;
                        }
                    }
                    // Check if it is an ad
                    if (foundNotification) {
                        Bundle extras = notification.extras;
                        String title = extras.getString(Notification.EXTRA_TITLE);
                        if (title != null) {
                            Log.d("DEBUG", title);
                            boolean isAdPlaying = blocklist.contains(title);
                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            if (isAdPlaying && !muted) {
                                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                                muted = true;
                            } else if (!isAdPlaying && muted) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                                muted = false;
                            }
                        }
                    }
                }
            }
        }, 10, 250);
        return START_NOT_STICKY;
    }

    public static void killService() {
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        Log.d("DEBUG", "Destroying Service");
        killService();
        Log.d("DEBUG", "Timer canceled.");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {

    }
}
