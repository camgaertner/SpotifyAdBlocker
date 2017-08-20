package com.cameron.spotifyadblocker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements ViewAdditionalFiltersDialogFragment.ViewAdditionalFiltersDialogListener {
    private boolean enabled;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        serviceIntent = new Intent(this, CustomNotificationListener.class);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        restoreCheckboxState();
    }

    private void restoreCheckboxState()
    {
        enabled = false;
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_enabled), MODE_PRIVATE);
        enabled = preferences.getBoolean(getString(R.string.saved_enabled), enabled);
        CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.checkBox);
        enabledCheckbox.setChecked(enabled);
        if(enabled && !CustomNotificationListener.isRunning())
            startService(serviceIntent);
    }

    public void onCheckboxClick(View view) {
        if (enabled) {
            Log.d("DEBUG", "Stopping Service");
            CustomNotificationListener.killService();
            stopService(serviceIntent);
            enabled = false;
        } else if (!CustomNotificationListener.isRunning()){
            startService(serviceIntent);
            enabled = true;
        }
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_enabled), MODE_PRIVATE).edit();
        preferencesEditor.putBoolean(getString(R.string.saved_enabled), enabled);
        preferencesEditor.apply();
    }

    public void notificationAccess(View view) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void addAdditionalFilter(View view) {
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE).edit();
        EditText et = (EditText)view.getRootView().findViewById(R.id.editTextAddFilter);
        String newFilter = et.getText().toString();
        et.setText("");
        preferencesEditor.putString("filter_" + newFilter, newFilter);
        preferencesEditor.apply();
        Toast.makeText(this, "Added filter: " + newFilter, Toast.LENGTH_SHORT).show();
    }

    public void openAdditionalFilterListDialog(View view) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE);
        Collection<? extends String> additionalFilters = (Collection<String>) preferences.getAll().values();
        ViewAdditionalFiltersDialogFragment viewAdditionalFiltersDialogFragment = ViewAdditionalFiltersDialogFragment.newInstance(additionalFilters.toArray(new String[additionalFilters.size()]));
        viewAdditionalFiltersDialogFragment.show(getFragmentManager(), "additionalFiltersDialog");
    }

    @Override
    public void onFilterClick(DialogInterface dialogInterface, int i) {
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE).edit();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE);
        Collection<String> additionalFilters = (Collection<String>) preferences.getAll().values();
        String filterToRemove = additionalFilters.toArray(new String[additionalFilters.size()])[i];
        preferencesEditor.remove("filter_" + filterToRemove);
        Toast.makeText(this, "Deleted filter: " + filterToRemove, Toast.LENGTH_SHORT).show();
        preferencesEditor.apply();
    }
}
