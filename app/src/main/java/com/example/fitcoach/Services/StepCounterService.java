package com.example.fitcoach.Services;
// Classe pour le service de podomètre
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.widget.StepWidgetProvider;

import java.text.DecimalFormat;

public class StepCounterService extends Service implements SensorEventListener {
    public static final String ACTION_STEP_COUNT_UPDATE = "com.example.fitcoach.STEP_COUNT_UPDATE";
    public static final String EXTRA_STEP_COUNT = "step_count";
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int currentSteps = 0;
    private AppDataManager appDataManager;
    private static final float METRIC_WALKING_FACTOR = 0.708f;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Méthode pour créer le service
    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Log.d("StepCounterService", "onCreate: service created");
        appDataManager = AppDataManager.getInstance(getApplicationContext());

        currentSteps = appDataManager.getSteps(0);
    }

    // Méthode appelée lors du démarrage du service
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
                    .setSmallIcon(R.drawable.appli_icon)
                    .build();

            startForeground(1, notification);
        }

        if (stepCounterSensor == null) {
            Log.e("StepCounterService", "Sensor not available");
            return START_NOT_STICKY;
        }

        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    // Méthode appelée lorsque le capteur détecte un changement
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            String today = getTodayDate();
            String lastResetDate = appDataManager.getLastResetDate(0);

            int baseSteps = appDataManager.getBaseSteps(0);


            if (!today.equals(lastResetDate) || baseSteps == -1) {
                baseSteps = totalSteps;
                appDataManager.updateStepCounter(0, today, 0,baseSteps,0f, 0f);
            }
            currentSteps = totalSteps - baseSteps;
            AppDataManager.Compte compte = appDataManager.getCompteById(appDataManager.getCompteId());
            float stepLength = estimateStepLength(compte.getTaille(), false);
            float DISTANCE = (currentSteps * stepLength) / 100000f;
            float CALORIES = compte.getPoids() * DISTANCE * METRIC_WALKING_FACTOR;
            appDataManager.updateStepCounter(0, today, currentSteps, baseSteps, CALORIES, DISTANCE);

            sendStepCountUpdate();
        }
    }

    // Méthode appelée lorsque la précision du capteur change
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("StepCounterService", "onAccuracyChanged : accuracy changed :" + accuracy);
    }

    // Méthode appelée lors de la destruction du service
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    // Méthode pour lier le service
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Envoi d'une mise à jour du nombre de pas via un Intent
    private void sendStepCountUpdate() {
        Intent intent = new Intent(ACTION_STEP_COUNT_UPDATE);
        intent.putExtra(EXTRA_STEP_COUNT, currentSteps);
        intent.putExtra("calories", appDataManager.getCalories(0));
        Log.d("StepCounterService", "sendStepCountUpdate: calories updated to " + appDataManager.getCalories(0));
        intent.putExtra("distance", appDataManager.getDistance(0));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("StepCounterService", "sendStepCountUpdate: step count updated");

        updateWidget();
    }

    // Mise à jour du widget avec le nombre de pas et d'autres informations
    private void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, StepWidgetProvider.class);

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_step);
        views.setTextViewText(R.id.steps_text, currentSteps+" "+getString(R.string.General_pas));
        views.setProgressBar(R.id.step_progress_bar, appDataManager.getStepsObjective(appDataManager.getCompteId()), currentSteps, false);

        views.setTextViewText(R.id.distance_text, decimalFormat.format(appDataManager.getDistance(0))+" km");
        views.setTextViewText(R.id.calories_text, decimalFormat.format(appDataManager.getCalories(0))+" kcal");

        appWidgetManager.updateAppWidget(componentName, views);
    }

    // Méthode pour obtenir la date du jour au format "yyyy-MM-dd"
    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    // Estimation de la longueur du pas en fonction de la taille et de l'activité (course ou marche)
    public float estimateStepLength(float height, boolean isRunning) {
        return isRunning ? height * 0.65f : height * 0.415f;
    }



}