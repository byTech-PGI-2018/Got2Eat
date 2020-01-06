package bytech.got2eat.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import bytech.got2eat.R;

public class Settings extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Customize toolbar options (toolbar appears due to theme in manifest)*/
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(R.string.settings));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*Add the fragment containing the PreferenceScreen*/
        getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*If we click on the '<-' arrow, finish the activity*/
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}