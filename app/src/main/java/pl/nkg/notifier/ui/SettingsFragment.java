package pl.nkg.notifier.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import pl.nkg.notifier.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
