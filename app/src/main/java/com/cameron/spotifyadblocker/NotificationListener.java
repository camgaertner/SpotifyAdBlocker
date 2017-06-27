package com.cameron.spotifyadblocker;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cameron on 6/7/2016.
 */
public class NotificationListener extends NotificationListenerService {
    private boolean muted;
    private int originalVolume;
    private int zeroVolume;
    private static Timer timer;
    private HashSet<String> blocklist;

    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        timer = new Timer();
        blocklist = new HashSet<String>();
        muted = false;
        originalVolume = 0;
        zeroVolume = 0;

        // Load blocklist
        Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(R.raw.blocklist);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                blocklist.add(line);
            }
            SharedPreferences preferences = getSharedPreferences("additionalFilters", MODE_PRIVATE);
            blocklist.addAll((Collection<? extends String>) preferences.getAll().values());
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                StatusBarNotification[] notifications = getActiveNotifications();
                Notification notification = new Notification();
                boolean foundNotification = false;
                if (notifications != null) {
                    Log.d("DEBUG", "Checking notifications.");

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
                        String title = extras.getCharSequence(Notification.EXTRA_TITLE).toString();
                        if (title != null) {
                            Log.d("DEBUG", title);
                            boolean isAdPlaying = blocklist.contains(title);
                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            if (isAdPlaying && !muted) {
                                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, zeroVolume, AudioManager.FLAG_SHOW_UI);
                            }
                            else if (!isAdPlaying && muted) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, AudioManager.FLAG_SHOW_UI);
                            }
                            muted = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == zeroVolume);
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
