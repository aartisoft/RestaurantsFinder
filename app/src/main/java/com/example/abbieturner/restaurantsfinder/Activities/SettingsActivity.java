package com.example.abbieturner.restaurantsfinder.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abbieturner.restaurantsfinder.Dialogs.GetLocationDialog;
import com.example.abbieturner.restaurantsfinder.R;
import com.example.abbieturner.restaurantsfinder.Singletons.DeviceLocation;
import com.example.abbieturner.restaurantsfinder.Singletons.LocationSharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GetLocationDialog locationDialog;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private LocationSharedPreferences locationSharedPreferences;
    private String SHARED_PREFERENCES_DEFAULT_LOCATION;

    @BindView(R.id.btn_manage_default_location)
    TextView btnManageDefaultLocation;
    @BindView(R.id.default_location_status)
    TextView defaultLocationStatus;
    @BindView(R.id.lang_spinner)
    Spinner lang_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lang_spinner.setAdapter(adapter);

        setNewInstances();
        setListeners();
        languageSetting();
    }

    public void languageSetting() {
        String lang = lang_spinner.getSelectedItem().toString();

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences lang_pref = getSharedPreferences("language", MODE_PRIVATE);
        SharedPreferences.Editor editor = lang_pref.edit();
        editor.putString("languageToLoad", lang);
        editor.apply();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (locationSharedPreferences.userHasLocationsSet()) {
            defaultLocationStatus.setText("location set");
        } else {
            defaultLocationStatus.setText("location not set");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setListeners() {
        btnManageDefaultLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserLoggedIn()) {
                    locationDialog.showDialog();
                } else {
                    Toast.makeText(SettingsActivity.this, "Log In Required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setNewInstances() {
        mAuth = FirebaseAuth.getInstance();
        locationDialog = new GetLocationDialog(this, true);
        DeviceLocation locationSingleton = DeviceLocation.getInstance();
        gson = new Gson();
        //sharedPreferences = getPreferences(MODE_PRIVATE);
        sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        locationSharedPreferences = LocationSharedPreferences.getInstance();
        SHARED_PREFERENCES_DEFAULT_LOCATION = getResources().getString(R.string.SHARED_PREFERENCES_DEFAULT_LOCATION);
    }

    public void locationSetFromUser(LatLng location) {
        locationSharedPreferences.setLocation(location);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        if (locationSharedPreferences.userHasLocationsSet()) {
            String json = gson.toJson(locationSharedPreferences.getUsersLocation());
            prefsEditor.putString(getLocationPreferencesKey(), json);
            prefsEditor.commit();

            String key = getLocationPreferencesKey();
            String jsonn = sharedPreferences.getString(key, "");
            Gson localGson = new Gson();
            LatLng defaultLocation = localGson.fromJson(jsonn, LatLng.class);
        }
    }

    private boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private String getLocationPreferencesKey() {
        return mAuth.getUid() + SHARED_PREFERENCES_DEFAULT_LOCATION;
    }
}