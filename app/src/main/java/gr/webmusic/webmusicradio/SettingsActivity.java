package gr.webmusic.webmusicradio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class SettingsActivity extends ActionBarActivity {

    private CheckBox chckBoxOnlyWiFi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initialization of the CheckBox
        chckBoxOnlyWiFi = (CheckBox) findViewById(R.id.checkBox_only_wifi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load settings
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.web_music_preferences), Context.MODE_PRIVATE);
        chckBoxOnlyWiFi.setChecked(sharedPreferences.getBoolean("wifi_check_box", false));
    }

    public void saveSettings(View view) {

        // save settings
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.web_music_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("wifi_check_box", chckBoxOnlyWiFi.isChecked());
        editor.commit();

        finish();
    }
}
