    package com.example.fitcoach.Services;

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
    import android.os.Handler;
    import android.os.IBinder;
    import android.os.Looper;
    import android.os.SystemClock;
    import android.util.Log;
    import android.widget.Toast;

    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;

    import com.example.fitcoach.Datas.AppDataManager;
    import com.example.fitcoach.MainActivity;
    import com.example.fitcoach.R;

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
        private long startTimeMillis = 0;
        private long totalDurationMillis = 0;
        private int totalSteps = 0;
        private int initialSteps = 0;
        private float distance = 0;
        private float speed = 0;
        private float currentCalories = 0;
        private int repetition = 0;
        private String sportType = "marche";
        private LocationManager locationManager;
        private ArrayList<Location> gpsTrack = new ArrayList<>();
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
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }
            startForeground(1, createNotification("Exercice en cours..."));
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
            return START_STICKY;
        }

        // Démarre un exercice
        public void startExercise(String sport, boolean isChronoMode) {
            sportType = sport;
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
            localBroadcastManager.sendBroadcast(intent);

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

            Intent intent = new Intent(ACTION_UPDATE_UI);
            intent.putExtra("isPaused", false);
            localBroadcastManager.sendBroadcast(intent);
        }

        // Arrête l'exercice
        public void stopExercise() {
            isRunning = false;
            Intent intent = new Intent(ACTION_UPDATE_UI);
            intent.putExtra("isPaused", true);
            localBroadcastManager.sendBroadcast(intent);
            uiHandler.removeCallbacks(uiRunnable);
            stopForeground(true);
            stopSelf();
            Toast.makeText(this, "Exercice terminé", Toast.LENGTH_SHORT).show();
        }

        // Envoie les mises à jour de l'UI (notamment pour la notification)
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
            if (gpsTrack.size() < 2) return distance;

            distance = 0; // Réinitialiser la distance

            // Calculer la distance totale en parcourant tous les segments
            for (int i = 1; i < gpsTrack.size(); i++) {
                Location previousLocation = gpsTrack.get(i - 1);
                Location currentLocation = gpsTrack.get(i);
                distance += previousLocation.distanceTo(currentLocation); // distance en mètres
            }

            // Conversion en kilomètres
            distance = distance / 1000f;

            // Calculer la vitesse basée sur la distance totale et la durée écoulée
            if (gpsTrack.size() > 1) {
                Location lastLocation = gpsTrack.get(gpsTrack.size() - 1);
                Location secondLastLocation = gpsTrack.get(gpsTrack.size() - 2);
                long timeDelta = lastLocation.getTime() - secondLastLocation.getTime(); // en millisecondes
                if (timeDelta > 0) {
                    speed = (distance / (timeDelta / 1000f)) * 3.6f; // Conversion en km/h
                }
            }

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
