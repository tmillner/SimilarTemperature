package com.localhost.tmillner.similartemperature;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        // Ideally this should also add a new ListPreference based on
        // countries - rather than set the layout to be text
    }

}
