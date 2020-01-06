package bytech.got2eat.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import bytech.got2eat.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        /*Select our preference screen XML*/
        setPreferencesFromResource(R.xml.settings_prefs, rootKey);

        /*Open a web browser activity when clicking on 'feedback' preference*/
        Preference github = findPreference("github");
        if (github != null){
            github.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/byTech-PGI-2018/Got2Eat"));
                    startActivity(browserIntent);

                    return true;
                }
            });
        }
    }
}