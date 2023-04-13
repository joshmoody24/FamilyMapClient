package com.example.familymapclient.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.familymapclient.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}