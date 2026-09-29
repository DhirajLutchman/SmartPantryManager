package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
/**
 * Settings screen. Stores simple user preferences (expiry alerts toggle,
 * preferred unit system) in SharedPreferences, so they survive closing the app
 * without needing their own database table.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    public static final String KEY_UNIT_SYSTEM = "unit_system"; // "metric" or "imperial"

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        SwitchCompat expirySwitch = findViewById(R.id.switchExpiryAlerts);
        RadioGroup unitGroup = findViewById(R.id.radioGroupUnits);
        int metricId = R.id.radioMetric;
        int imperialId = R.id.radioImperial;

        // Load saved values (defaults: alerts on, metric)
        expirySwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        String savedUnitSystem = prefs.getString(KEY_UNIT_SYSTEM, "metric");
        unitGroup.check(savedUnitSystem.equals("imperial") ? imperialId : metricId);

        // Save whenever the user changes something
        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        unitGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String value = (checkedId == imperialId) ? "imperial" : "metric";
            prefs.edit().putString(KEY_UNIT_SYSTEM, value).apply();
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                finish();
            } else if (id == R.id.nav_suggestions) {
                startActivity(new Intent(SettingsActivity.this, SuggestedRecipesActivity.class));
                finish();
            }
            return true;
        });
    }
}