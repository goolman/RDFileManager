package com.xdockalr.rdfilemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import java.io.File;

import static com.xdockalr.rdfilemanager.MainActivity.getmActualPath;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference);
        EditTextPreference mEditTextPref = (EditTextPreference) getPreferenceManager().findPreference("DEFAULT_PATH");
        mEditTextPref.setSummary(mEditTextPref.getText());

        mEditTextPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null && (new File((String) newValue).isDirectory())) {
                    preference.setSummary(newValue.toString());
                    return true;
                }
                     else{
                    Toast.makeText(getContext(), getString(R.string.incorrect_path_toast) + newValue, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        mEditTextPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String key = preference.getKey();
                return true;
            }
        });

        mEditTextPref.setDialogMessage(getString(R.string.current_folder_dialog) + getmActualPath());
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
}
