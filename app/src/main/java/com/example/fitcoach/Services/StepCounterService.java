package com.example.fitcoach.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StepCounterService extends Service implements SensorEventListener {

    public static final String ACTION_STEP_COUNT_UPDATE = "com.example.fitcoach.STEP_COUNT_UPDATE";
    public static final String EXTRA_STEP_COUNT = "step_count";
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int currentSteps = 0;
    private SharedPreferences sharedPreferences;
    private static final String STEP_PREFS = "step_counter_prefs";
    private static final String CURRENT_STEPS_KEY = "current_steps";

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sharedPreferences = getSharedPreferences(STEP_PREFS, Context.MODE_PRIVATE);
        Log.d("StepCounterService", "onCreate: service created");
        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        currentSteps = sharedPreferences.getInt(CURRENT_STEPS_KEY, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (stepCounterSensor == null) {
            Log.e("StepCounterService", "Sensor not available");
            Toast.makeText(this, "Sensor not available", Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }
        Toast.makeText(this, "onStartCommand called", Toast.LENGTH_SHORT).show();
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Toast.makeText(this, "onSensorChanged called", Toast.LENGTH_SHORT).show();
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                currentSteps++;
                Toast.makeText(this, "onSensorChanged : step detected", Toast.LENGTH_LONG).show();
                saveStepCount();
            }
        }

        sendStepCountUpdate();
    }

    private void saveStepCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_STEPS_KEY, currentSteps);
        editor.apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("StepCounterService", "onAccuracyChanged : accuracy changed :" + accuracy);
        Toast.makeText(this, "onAccuracyChanged called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendStepCountUpdate() {
        Intent intent = new Intent(ACTION_STEP_COUNT_UPDATE);
        intent.putExtra(EXTRA_STEP_COUNT, currentSteps);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("StepCounterService", "sendStepCountUpdate: step count updated");
        Toast.makeText(this, "sendStepCountUpdate called", Toast.LENGTH_SHORT).show();
    }
}