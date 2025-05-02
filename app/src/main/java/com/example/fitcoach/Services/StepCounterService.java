package com.example.fitcoach.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.widget.StepWidgetProvider;

public class StepCounterService extends Service implements SensorEventListener {

    public static final String ACTION_STEP_COUNT_UPDATE = "com.example.fitcoach.STEP_COUNT_UPDATE";
    public static final String EXTRA_STEP_COUNT = "step_count";
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int currentSteps = 0;
    private AppDataManager appDataManager;


    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Log.d("StepCounterService", "onCreate: service created");
        // Initialiser AppDataManager
        appDataManager = AppDataManager.getInstance(getApplicationContext());

        // Récupérer les données existantes depuis AppDataManager
        currentSteps = appDataManager.getSteps(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "step_counter_channel",
                    "Suivi des pas",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new Notification.Builder(this, "step_counter_channel")
                    .setContentTitle("FitCoach")
                    .setContentText("Le suivi des pas est actif")
                    .setSmallIcon(R.drawable.appli_icon) // Remplace par ton icône custom si dispo
                    .build();

            startForeground(1, notification); // OBLIGATOIRE sur Android 10+
        }

        if (stepCounterSensor == null) {
            Toast.makeText(this, "Pas de capteur de pas détecté", Toast.LENGTH_SHORT).show();
            Log.e("StepCounterService", "Sensor not available");
            return START_NOT_STICKY;
        }

        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY; // Maintient le service actif
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            String today = getTodayDate();
            String lastResetDate = appDataManager.getLastResetDate(0);

            int baseSteps = appDataManager.getBaseSteps(0);


            if (!today.equals(lastResetDate) || baseSteps == -1) {
                baseSteps = totalSteps;
                Toast.makeText(this, "Reset effectué", Toast.LENGTH_SHORT).show();
                appDataManager.updateStepCounter(0, today, 0,baseSteps);
            }

            currentSteps = totalSteps - baseSteps;
            appDataManager.updateStepCounter(0, today, currentSteps, baseSteps);

            sendStepCountUpdate();
        }
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
        views.setTextViewText(R.id.steps_text, currentSteps+" pas");
        views.setProgressBar(R.id.step_progress_bar, 10000, currentSteps, false);

        appWidgetManager.updateAppWidget(componentName, views);
    }

    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }


}