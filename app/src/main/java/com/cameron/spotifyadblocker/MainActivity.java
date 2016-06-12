package com.cameron.spotifyadblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private boolean enabled;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        enabled = false;
        serviceIntent = new Intent(this, NotificationListener.class);
    }

    public void blockAds(View view) {
        if (enabled) {
            Log.d("DEBUG", "Stopping Service");
            NotificationListener.killService();
            stopService(serviceIntent);
            enabled = false;
        } else {
            startService(serviceIntent);
            enabled = true;
        }
    }

    public void notificationAccess(View view) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }
}
