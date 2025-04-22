package com.example.fitcoach.Services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fitcoach.R;
import com.example.fitcoach.widget.StepWidgetProvider;

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
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sharedPreferences = getSharedPreferences(STEP_PREFS, Context.MODE_PRIVATE);
        Log.d("StepCounterService", "onCreate: service created");
        currentSteps = sharedPreferences.getInt(CURRENT_STEPS_KEY, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (stepCounterSensor == null) {
            Log.e("StepCounterService", "Sensor not available");
            return START_NOT_STICKY;
        }
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];
            currentSteps = totalSteps; // ou logique différente si tu veux faire un delta
            saveStepCount();
            sendStepCountUpdate();
        }

    }

    private void saveStepCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_STEPS_KEY, currentSteps);
        editor.apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("StepCounterService", "onAccuracyChanged : accuracy changed :" + accuracy);
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_STEPS_KEY, currentSteps);
        editor.apply();

        Intent intent = new Intent(ACTION_STEP_COUNT_UPDATE);
        intent.putExtra(EXTRA_STEP_COUNT, currentSteps);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("StepCounterService", "sendStepCountUpdate: step count updated");

        updateWidget();
    }

    private void updateWidget(){
        // Mise à jour du widget avec la nouvelle valeur
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, StepWidgetProvider.class);

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_step);
        views.setTextViewText(R.id.steps_text, String.valueOf(currentSteps));
        views.setProgressBar(R.id.step_progress_bar, 10000, currentSteps, false);

        appWidgetManager.updateAppWidget(componentName, views);
    }
}