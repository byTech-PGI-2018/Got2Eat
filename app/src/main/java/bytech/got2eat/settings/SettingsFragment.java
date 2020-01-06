package bytech.got2eat.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import bytech.got2eat.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_prefs, rootKey);
    }
}