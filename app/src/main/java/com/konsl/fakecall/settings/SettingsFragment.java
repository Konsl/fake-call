package com.konsl.fakecall.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.konsl.fakecall.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
