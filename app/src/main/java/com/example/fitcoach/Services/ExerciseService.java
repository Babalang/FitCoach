    package com.example.fitcoach.Services;

    import static android.content.ContentValues.TAG;

    import android.Manifest;
    import android.app.Notification;
    import android.app.NotificationChannel;
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.app.Service;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.IBinder;
    import android.os.Looper;
    import android.os.SystemClock;
    import android.util.Log;
    import android.widget.Toast;

    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.core.content.ContextCompat;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;

    import com.example.fitcoach.Datas.AppDataManager;
    import com.example.fitcoach.MainActivity;
    import com.example.fitcoach.R;
    import com.example.fitcoach.ui.Exercise.ExerciseStep;

    import java.util.ArrayList;

    public class ExerciseService extends Service {

        private ExerciseReceiver exerciseReceiver;
        private boolean isReceiverRegistered = false;
        private LocalBroadcastManager localBroadcastManager;
        public static final String ACTION_PAUSE = "com.example.fitcoach.PAUSE";
        public static final String ACTION_STOP = "com.example.fitcoach.STOP";
        public static final String ACTION_RESUME = "com.example.fitcoach.RESUME";
        public static final String ACTION_START = "com.example.fitcoach.START";
        public static final String ACTION_INCREMENT_STEP = "com.example.fitcoach.INCREMENT_STEP";
        public static final String ACTION_UPDATE_UI = "com.example.fitcoach.UPDATE_UI";
        public static final String ACTION_REQUEST_STATUS = "com.example.fitcoach.ACTION_REQUEST_STATUS";
        public static final String ACTION_SEND_STATUS = "com.example.fitcoach.ACTION_SEND_STATUS";

    // Dans onStartCommand

        private boolean isRunning = false;
        private boolean isStopping = false;
        private long startTimeMillis = 0;
        private long totalDurationMillis = 0;
        private int totalSteps = 0;
        private int initialSteps = 0;
        private float distance = 0;
        private float speed = 0;
        private float currentCalories = 0;
        private int repetition = 0;
        private String sportType = "marche";
        private boolean isChronoMode = false;
        private LocationManager locationManager;
        private ArrayList<Location> gpsTrack = new ArrayList<>();

        private ArrayList<ExerciseStep> timerSteps;
        private LocationListener locationListener;
        private final Handler uiHandler = new Handler(Looper.getMainLooper());
        private final Runnable uiRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    startSendingUIUpdates();
                    uiHandler.postDelayed(this, 100); // chaque 1 sec
                }
            }
        };


        private class ExerciseReceiver extends BroadcastReceiver{
            @Override
            public void onReceive(Context context, Intent intent) {
                if (StepCounterService.ACTION_STEP_COUNT_UPDATE.equals(intent.getAction())) {
                    int stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0);
                    currentCalories = estimateCalories(sportType,distance);
                    totalSteps = stepCount;
                    startSendingUIUpdates();
                }else if (intent.getAction().equals(ACTION_PAUSE)) {
                    pauseExercise();
                } else if (intent.getAction().equals(ACTION_STOP)) {
                    stopExercise();
                } else if (intent.getAction().equals(ACTION_RESUME)) {
                    resumeExercise();
                } else if (intent.getAction().equals(ACTION_START)) {
                    startExercise(intent.getStringExtra("sport"), intent.getBooleanExtra("isChronoMode", false));
                } else if (intent.getAction().equals(ACTION_INCREMENT_STEP)) {
                    repetition++;
                    Log.d("InexerciseFragment", "onReceive: Incrementing step, current repetition: " + repetition);
                } else if (ACTION_REQUEST_STATUS.equals(intent.getAction())) {
                    Intent statusIntent = new Intent(ACTION_SEND_STATUS);
                    // Remplis avec les données en cours
                    statusIntent.putExtra("steps", totalSteps - initialSteps);
                    statusIntent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
                    statusIntent.putExtra("calories", currentCalories);
                    statusIntent.putExtra("distance", distance);
                    statusIntent.putExtra("speed", speed);
                    statusIntent.putExtra("isRunning", isRunning);
                    statusIntent.putExtra("sportType", sportType);
                    statusIntent.putExtra("isChronoMode", isChronoMode);  // Ajouter cette info
                    statusIntent.putExtra("repetition", repetition);
                    localBroadcastManager.sendBroadcast(statusIntent);
                }

            }
        }
        private void registerReceiver(){
            if(!isReceiverRegistered){
                exerciseReceiver = new ExerciseReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_STOP);
                filter.addAction(ACTION_RESUME);
                filter.addAction(ACTION_START);
                filter.addAction(ACTION_INCREMENT_STEP);
                filter.addAction(ACTION_REQUEST_STATUS);
                filter.addAction(StepCounterService.ACTION_STEP_COUNT_UPDATE);

                localBroadcastManager.registerReceiver(exerciseReceiver, filter);
                isReceiverRegistered = true;
            }
        }

        private void unregisterReceiver(){
            if(isReceiverRegistered){
                localBroadcastManager.unregisterReceiver(exerciseReceiver);
                isReceiverRegistered = false;
            } else {
                Toast.makeText(this, "Receiver not registered", Toast.LENGTH_SHORT).show();
            }
        }

        public boolean isReceiverRegistered() {
                return isReceiverRegistered;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            totalSteps = AppDataManager.getInstance().getSteps(0);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            distance = 0;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId){
            if(isStopping){
                return START_NOT_STICKY;
            }
            if(localBroadcastManager == null){
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
            }
            registerReceiver();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "exercise_channel",
                        "Exercice en cours",
                        NotificationManager.IMPORTANCE_LOW
                );

                channel.setShowBadge(true);
                channel.enableLights(true);
                channel.enableVibration(false);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }
            // Vérifier d'abord les permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("ExerciseService", "Permission de localisation non accordée");
                stopSelf();
                return START_NOT_STICKY;
            }
            Notification intitialNotification = createNotification("Exercice en cours...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, intitialNotification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(1, intitialNotification);
            }
            if(intent != null){
                String action = intent.getAction();
                if(action != null){
                    switch(action){
                        case ACTION_START:
                            String sport = intent.getStringExtra("sport");
                            boolean isChronoMode = intent.getBooleanExtra("isChronoMode", false);
                            startExercise(sport, isChronoMode);
                            break;
                        case ACTION_PAUSE:
                            pauseExercise();
                            updateNotification();
                            break;
                        case ACTION_RESUME:
                            resumeExercise();
                            updateNotification();
                            break;
                        case ACTION_STOP:
                            stopExercise();
                            break;
                        case ACTION_INCREMENT_STEP:
                            repetition++;
                            break;
                        default:
                            break;

                    }
                }
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (isRunning) {
                    startSendingUIUpdates();
                }
            });
            return START_STICKY;
        }

        // Démarre un exercice
        public void startExercise(String sport, boolean isChronoMode) {
            this.isChronoMode = isChronoMode;
            sportType = (sport != null) ? sport : "marche";
            startTimeMillis = SystemClock.elapsedRealtime();
            initialSteps = AppDataManager.getInstance().getSteps(0);
            currentCalories = AppDataManager.getInstance().getCalories(0);
            distance = 0;
            isRunning = true;
            startLocationUpdates();
            startSendingUIUpdates();
            if (isRunning) {
                uiHandler.removeCallbacks(uiRunnable);
                uiHandler.post(uiRunnable); // immediate
            }
            Toast.makeText(this, "Exercice démarré", Toast.LENGTH_SHORT).show();
            Log.d("ExerciseService", "Exercice démarré ; sport : " + sportType);
        }

        // Met en pause l'exercice
        public void pauseExercise() {
            isRunning = false;
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            if (startTimeMillis > 0) {
                totalDurationMillis += SystemClock.elapsedRealtime() - startTimeMillis;
                startTimeMillis = 0;
            }

            Intent intent = new Intent(ACTION_UPDATE_UI);
            intent.putExtra("isPaused", true);
            intent.putExtra("forceUpdateUI", true); // Indicateur de mise à jour forcée
            intent.putExtra("steps", totalSteps - initialSteps);
            intent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
            intent.putExtra("calories", currentCalories);
            intent.putExtra("distance", distance);
            intent.putExtra("speed", speed);
            intent.putExtra("repetition", repetition);
            localBroadcastManager.sendBroadcast(intent);

            Intent statusIntent = new Intent(ACTION_SEND_STATUS);
            statusIntent.putExtra("isRunning", isRunning);
            statusIntent.putExtra("steps", totalSteps - initialSteps);
            statusIntent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
            statusIntent.putExtra("calories", currentCalories);
            statusIntent.putExtra("distance", distance);
            statusIntent.putExtra("speed", speed);
            statusIntent.putExtra("sportType", sportType);
            statusIntent.putExtra("repetition", repetition);
            localBroadcastManager.sendBroadcast(statusIntent);

            uiHandler.removeCallbacks(uiRunnable);
        }

        // Reprend l'exercice
        public void resumeExercise() {
            startTimeMillis = SystemClock.elapsedRealtime();
            isRunning = true;
            startLocationUpdates();
            uiHandler.removeCallbacks(uiRunnable);
            uiHandler.post(uiRunnable);
            startSendingUIUpdates();

            Intent intent = new Intent(ACTION_SEND_STATUS);
            intent.putExtra("isRunning", true);
            intent.putExtra("steps", totalSteps - initialSteps);
            intent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
            intent.putExtra("calories", currentCalories);
            intent.putExtra("distance", distance);
            intent.putExtra("speed", speed);
            intent.putExtra("sportType", sportType);
            intent.putExtra("repetition", repetition);
            intent.putExtra("isPaused", false);

            localBroadcastManager.sendBroadcast(intent);
        }

        // Arrête l'exercice
        public void stopExercise() {
            isStopping = true;
            if (isRunning && startTimeMillis > 0) {
                totalDurationMillis += SystemClock.elapsedRealtime() - startTimeMillis;
                startTimeMillis = 0;
            }

            isRunning = false;

            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            Intent intent = new Intent(ACTION_SEND_STATUS);
            intent.putExtra("isRunning", false);
            intent.putExtra("steps", totalSteps - initialSteps);
            intent.putExtra("duration", totalDurationMillis/1000);
            intent.putExtra("calories", currentCalories);
            intent.putExtra("distance", distance);
            intent.putExtra("speed", speed);
            intent.putExtra("sportType", sportType);
            intent.putExtra("repetition", repetition);
            intent.putExtra("isStopping", true);
            intent.putExtra("showSummary", true);
            localBroadcastManager.sendBroadcast(intent);
            Log.d("ExerciseService", "Exercice terminé ; duration : " + totalDurationMillis/1000);
            uiHandler.removeCallbacks(uiRunnable);
            uiHandler.removeCallbacks(uiRunnable);
            stopForeground(true);
            stopSelf();
            Toast.makeText(this, "Exercice terminé", Toast.LENGTH_SHORT).show();
        }

        private void startSendingUIUpdates() {
            totalSteps = AppDataManager.getInstance().getSteps(0);
            estimateDistance();
            currentCalories = estimateCalories(sportType, distance);
            Intent intent = new Intent(ACTION_UPDATE_UI);
            intent.putExtra("steps", totalSteps - initialSteps);
            intent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
            Log.d("ExerciseService", "startSendingUIUpdates: "+getElapsedExerciseTimeMillis()/1000);
            intent.putExtra("calories", currentCalories);
            intent.putExtra("distance", distance);
            intent.putExtra("speed", speed);
            intent.putExtra("repetition", repetition);
            localBroadcastManager.sendBroadcast(intent);
            String content = String.format("Durée: %ds - Distance: %.2fm - Vitesse: %.2fkm/h",
                getElapsedExerciseTimeMillis() / 1000,
                distance,
                speed
            );
            Log.d("ExerciseService", "startSendingUIUpdates: UI updated"+content);
            Log.d("ExerciseService", "Broadcasting: steps=" + (totalSteps-initialSteps) + ", duration=" + getElapsedExerciseTimeMillis() / 1000 + ", distance=" + distance + ", speed=" + speed + ", calories=" + currentCalories + "...");

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                Notification updatedNotification = createNotification(content);
                manager.notify(1, updatedNotification);
            }
        }

        // Mise à jour des données de localisation (distance et vitesse)
        private void startLocationUpdates() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationListener = location -> gpsTrack.add(location);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);

            estimateDistance();
        }

        // Estimation des calories brûlées en fonction du type de sport
        private float estimateCalories(String sport, float distanceMeters) {
            float km = distanceMeters;
            switch (sport.toLowerCase()) {
                case "marche": return km * 50;
                case "course": return km * 60;
                case "vélo": return km * 40;
                default: return km * 35;
            }
        }

        // Estimation de la distance parcourue
        private float estimateDistance() {
            if(sportType != "marche" && gpsTrack.size() >= 2) {
                distance = 0;
                for (int i = 1; i < gpsTrack.size(); i++) {
                    Location previousLocation = gpsTrack.get(i - 1);
                    Location currentLocation = gpsTrack.get(i);
                    distance += previousLocation.distanceTo(currentLocation); // distance en mètres
                }

                // Calcul de la vitesse seulement si nous avons au moins 2 points
                Location lastLocation = gpsTrack.get(gpsTrack.size() - 1);
                Location secondLastLocation = gpsTrack.get(gpsTrack.size() - 2);
                long timeDelta = lastLocation.getTime() - secondLastLocation.getTime();
                if (timeDelta > 0) {
                    speed = (distance / (timeDelta / 1000f)) * 3.6f;
                }
            } else {
                // Mode pas à pas ou pas assez de points GPS
                distance = 0;
                int stepCount = totalSteps - initialSteps;
                distance = stepCount * 0.7f;

                // Dans ce cas la vitesse est calculée par rapport au temps écoulé
                long elapsedSeconds = getElapsedExerciseTimeMillis() / 1000;
                if (elapsedSeconds > 0) {
                    speed = (distance / elapsedSeconds) * 3.6f;  // km/h
                }
            }

            distance = distance / 1000f;  // Convertir en km
            return distance;
        }


        // Création de la notification avec l'info mise à jour
        private Notification createNotification(String content) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra("OPEN_EXERCISE", true);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent pauseIntent = new Intent(this, ExerciseService.class);
            pauseIntent.setAction(ACTION_PAUSE);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Intent resumeIntent = new Intent(this, ExerciseService.class);
            resumeIntent.setAction(ACTION_RESUME);
            PendingIntent resumePendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Intent stopIntent = new Intent(this, ExerciseService.class);
            stopIntent.setAction(ACTION_STOP);
            PendingIntent stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "exercise_channel")
                    .setContentTitle("FitCoach - Exercice en cours")
                    .setContentText(isRunning? content : "Exercice en pause")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(null)
                    .setVibrate(null)
                    .setOngoing(true);


            if (isRunning) {
                notificationBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent);
            } else {
                notificationBuilder.addAction(android.R.drawable.ic_media_play, "Reprendre", resumePendingIntent);
            }
            notificationBuilder.addAction(android.R.drawable.ic_menu_delete, "Arrêter", stopPendingIntent);

            return notificationBuilder.build();
        }

        private void updateNotification(){
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                String content = String.format("Durée: %ds - Distance: %.2fm - Vitesse: %.2fkm/h",
                        getElapsedExerciseTimeMillis() / 1000,
                        distance,
                        speed
                );
                Notification updatedNotification = createNotification(content);
                manager.notify(1, updatedNotification);
            }
        }


        // Méthode appelée lorsque le service est détruit
        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver();
            pauseExercise();
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            stopForeground(true);
            Toast.makeText(this, "Exercice terminé, duration : "+totalDurationMillis+" repetitions : "+repetition+" steps : "+(totalSteps-initialSteps)+" calories : "+currentCalories+" distance : "+distance+" speed : "+speed+"", Toast.LENGTH_SHORT).show();
            Log.d("ExerciseService", "Exercice terminé, duration : "+totalDurationMillis+" repetitions : "+repetition+" steps : "+(totalSteps-initialSteps)+" calories : "+currentCalories+" distance : "+distance+" speed : "+speed+"");
        }

        // Méthode pour lier le service à un client
        @Override
        public IBinder onBind(Intent intent) {return null;}

        private long getElapsedExerciseTimeMillis() {
            if (isRunning) {
                return totalDurationMillis + (SystemClock.elapsedRealtime() - startTimeMillis);
            } else {
                return totalDurationMillis;
            }
        }

    }
