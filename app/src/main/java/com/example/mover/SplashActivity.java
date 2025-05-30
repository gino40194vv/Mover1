package com.example.mover;

import static androidx.core.location.LocationManagerCompat.getCurrentLocation;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.content.pm.PackageManager;
import android.content.res.Resources;

public class SplashActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inizializza le SharedPreferences
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        // Applica il tema salvato
        int savedTheme = prefs.getInt("app_theme", 0);
        applyTheme(savedTheme);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Scegli il layout in base al tema
        setContentViewBasedOnTheme();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        int nightModeFlags = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.nerino));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.nerino));
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        // Inizializza la mappa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Nascondi il fragment della mappa
            getSupportFragmentManager().beginTransaction()
                    .hide(mapFragment)
                    .commit();
            mapFragment.getMapAsync(this);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            if (mMap != null && mMap.getCameraPosition() != null) {
                intent.putExtra("LAST_KNOWN_LAT", mMap.getCameraPosition().target.latitude);
                intent.putExtra("LAST_KNOWN_LNG", mMap.getCameraPosition().target.longitude);
                intent.putExtra("LAST_KNOWN_ZOOM", mMap.getCameraPosition().zoom);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void setContentViewBasedOnTheme() {
        int savedTheme = prefs.getInt("app_theme", 0);
        int nightModeFlags = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        // Se è impostato "Segui sistema"
        if (savedTheme == 0) {
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                setContentView(R.layout.splash_screen_dark);
            } else {
                setContentView(R.layout.splash_screen_dark);
            }
        }
        // Se è impostato "Chiaro"
        else if (savedTheme == 1) {
            setContentView(R.layout.splash_screen_dark);
        }
        // Se è impostato "Scuro"
        else {
            setContentView(R.layout.splash_screen_dark);
        }
    }

    private void applyTheme(int theme) {
        switch (theme) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            try {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style_dark));
            } catch (Resources.NotFoundException e) {
                Log.e("SplashActivity", "Can't find map style.", e);
            }
        }

        // Controlla i permessi e mostra la posizione attuale
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null && mMap != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                        }
                    });
        } catch (SecurityException e) {
            Log.e("SplashActivity", "Error getting location", e);
        }
    }
}
