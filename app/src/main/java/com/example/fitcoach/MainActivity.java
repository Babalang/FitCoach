package com.example.fitcoach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fitcoach.Services.ExerciseService;
import com.example.fitcoach.Services.LocationService;
import com.example.fitcoach.Services.StepCounterService;
import com.example.fitcoach.databinding.ActivityMainBinding;
import com.example.fitcoach.ui.login.loginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.fitcoach.Datas.AppDataManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;
    private static final int LOCATION_REQUEST_CODE = 101;
    private boolean isServiceStarted = false;
    private boolean isLocationStarted = false;

    private static AppDataManager appDataManager;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDataManager = AppDataManager.getInstance(this);
        Log.d(TAG, String.valueOf((!appDataManager.isCompleted(appDataManager.getCompteId()))));
        if (appDataManager != null && !appDataManager.isCompleted(appDataManager.getCompteId())) {
            Intent intent = new Intent(this, loginActivity.class);
            startActivity(intent);
            finish(); // Pour éviter le retour en arrière sur MainActivity
            return;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Vérifier et demander la permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            if(!isServiceStarted){
                startStepCounterService();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_RECOGNITION_REQUEST_CODE);
        }
        //Request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            Log.d(TAG, "onCreate: ACCESS_FINE_LOCATION is already granted");
            if(!isLocationStarted){
                startLocationService();
            }
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_social, R.id.navigation_history, R.id.navigation_music, R.id.navigation_exercise)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void startStepCounterService() {
        Intent serviceIntent = new Intent(MainActivity.this, StepCounterService.class);
        ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
        isServiceStarted = true;
        Log.d(TAG, "startStepCounterService: step service started");
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
        ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
        isLocationStarted = true;
        Log.d(TAG, "startLocationService: location service started");
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(!isServiceStarted){
                    startStepCounterService();
                }
            } else {
                Log.e(TAG, "Permission ACTIVITY_RECOGNITION refusée");
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == LOCATION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: Location permission result");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission ACCESS_FINE_LOCATION granted");
                if(!isLocationStarted){
                    startLocationService();
                }

            } else {
                Log.e(TAG, "onRequestPermissionsResult: Permission ACCESS_FINE_LOCATION refusée");
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public boolean isServiceStarted() {
        return isServiceStarted;
    }

    public boolean isLocationStarted() {
        return isLocationStarted;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu); // Tu peux y ajouter le bouton "Paramètres"
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_global_to_infos);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent != null && intent.getBooleanExtra("OPEN_EXERCISE", false)){
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.in_exercise);
        }
    }

}