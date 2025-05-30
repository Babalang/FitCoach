package com.example.fitcoach.Services;
// Service pour gérer les exercices, la musique et les notifications
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.ui.Exercise.ExerciseStep;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class ExerciseService extends Service {

    private ExerciseReceiver exerciseReceiver;
    private boolean isReceiverRegistered = false;
    private LocalBroadcastManager localBroadcastManager;
    private MediaPlayer mediaPlayer;

    public static final String ACTION_PAUSE = "com.example.fitcoach.PAUSE";
    public static final String ACTION_STOP = "com.example.fitcoach.STOP";
    public static final String ACTION_RESUME = "com.example.fitcoach.RESUME";
    public static final String ACTION_START = "com.example.fitcoach.START";
    public static final String ACTION_INCREMENT_STEP = "com.example.fitcoach.INCREMENT_STEP";
    public static final String ACTION_UPDATE_UI = "com.example.fitcoach.UPDATE_UI";
    public static final String ACTION_REQUEST_STATUS = "com.example.fitcoach.ACTION_REQUEST_STATUS";
    public static final String ACTION_SEND_STATUS = "com.example.fitcoach.ACTION_SEND_STATUS";
    public static final String ACTION_CHANGE_MUSIC = "com.example.fitcoach.ACTION_CHANGE_MUSIC";
    public static final String ACTION_STOP_MUSIC = "com.example.fitcoach.ACTION_STOP_MUSIC";
    public static final String ACTION_RESUME_MUSIC = "com.example.fitcoach.ACTION_RESUME_MUSIC";
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
                uiHandler.postDelayed(this, 100);
            }
        }
    };


    // BroadcastReceiver pour recevoir les mises à jour de l'exercice
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
                updateNotification();
            } else if (intent.getAction().equals(ACTION_RESUME)) {
                resumeExercise();
                updateNotification();
            } else if (intent.getAction().equals(ACTION_START)) {
                startExercise(intent.getStringExtra("sport"), intent.getBooleanExtra("isChronoMode", false));
            } else if (intent.getAction().equals(ACTION_INCREMENT_STEP)) {
                repetition++;
                Log.d("InexerciseFragment", "onReceive: Incrementing step, current repetition: " + repetition);
            } else if (ACTION_REQUEST_STATUS.equals(intent.getAction())) {
                Intent statusIntent = new Intent(ACTION_SEND_STATUS);
                statusIntent.putExtra("steps", totalSteps - initialSteps);
                statusIntent.putExtra("duration", getElapsedExerciseTimeMillis()/1000);
                statusIntent.putExtra("calories", currentCalories);
                statusIntent.putExtra("distance", distance);
                statusIntent.putExtra("speed", speed);
                statusIntent.putExtra("isRunning", isRunning);
                statusIntent.putExtra("sportType", sportType);
                statusIntent.putExtra("isChronoMode", isChronoMode);
                statusIntent.putExtra("repetition", repetition);
                localBroadcastManager.sendBroadcast(statusIntent);
            } else if (ACTION_CHANGE_MUSIC.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: Changing music");
                stopMusic();
                startMusic();
            } else if (ACTION_STOP_MUSIC.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: Stopping music");
                pauseMusic();
            } else if (ACTION_RESUME_MUSIC.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: Resuming music");
                resumeMusic();
            } else {
                Log.w(TAG, "onReceive: Action non reconnue : " + intent.getAction());
            }

        }
    }

    // Enregistre le BroadcastReceiver pour écouter les actions spécifiques
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
            filter.addAction(ACTION_CHANGE_MUSIC);
            filter.addAction(ACTION_STOP_MUSIC);
            filter.addAction(ACTION_RESUME_MUSIC);

            localBroadcastManager.registerReceiver(exerciseReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    // Désenregistre le BroadcastReceiver pour éviter les fuites de mémoire
    private void unregisterReceiver(){
        if(isReceiverRegistered){
            localBroadcastManager.unregisterReceiver(exerciseReceiver);
            isReceiverRegistered = false;
        } else {
        }
    }

    // Vérifie si le BroadcastReceiver est enregistré
    public boolean isReceiverRegistered() {
            return isReceiverRegistered;
    }

    // Méthode pour récupérer une piste aléatoire depuis Jamendo et la jouer
    private void fetchAndPlayRandomTrack() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.jamendo.com/v3.0/tracks/?client_id=b960794f&format=json&limit=100&order=popularity_total_desc&tags=fitness");
                Log.d("ExerciseService", "Requête Jamendo : " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                Log.d("ExerciseService", "Réponse Jamendo : " + response);
                JSONObject json = new JSONObject(response.toString());
                JSONArray tracks = json.getJSONArray("results");
                if (tracks.length() > 0) {
                    int randomIndex = (int) (Math.random() * tracks.length());
                    String streamUrl = tracks.getJSONObject(randomIndex).getString("audio");
                    Log.d("ExerciseService", "URL musique trouvée : " + streamUrl);
                    new Handler(Looper.getMainLooper()).post(() -> playMusic(streamUrl));
                } else {
                    Log.e("ExerciseService", "Aucune piste trouvée dans la réponse Jamendo");
                }
            } catch (Exception e) {
                Log.e("ExerciseService", "Erreur Jamendo", e);
            }
        }).start();
    }

    // Joue la musique à partir de l'URL fournie
    private void playMusic(String url) {
        stopMusic();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> fetchAndPlayRandomTrack());
            Log.d("ExerciseService", "Lecture de la musique : " + url);
        } catch (Exception e) {
            Log.e("ExerciseService", "Erreur MediaPlayer", e);
        }
    }

    // Démarre la musique en récupérant une piste aléatoire
    private void startMusic() {
        fetchAndPlayRandomTrack();
    }

    // Pause, reprend ou arrête la musique en fonction de l'état du MediaPlayer
    private void pauseMusic() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        } catch (IllegalStateException e) {
            Log.e("ExerciseService", "Erreur lors de la pause de la musique", e);
        } catch (Exception e) {
            Log.e("ExerciseService", "Exception inattendue lors de la pause", e);
        }
    }

    // Reprend la musique si elle est en pause
    private void resumeMusic() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (IllegalStateException e) {
            Log.e("ExerciseService", "Erreur lors de la reprise de la musique", e);
        }
    }

    // Arrête la musique et libère les ressources du MediaPlayer
    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                Log.w("ExerciseService", "MediaPlayer déjà arrêté ou pas prêt", e);
            } catch (Exception e) {
                Log.e("ExerciseService", "Erreur inattendue lors de l'arrêt", e);
            }
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("ExerciseService", "Erreur lors du release", e);
            }
            mediaPlayer = null;
        }
    }

    // Constructeur par défaut
    @Override
    public void onCreate() {
        super.onCreate();
        totalSteps = AppDataManager.getInstance().getSteps(0);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        distance = 0;
    }

    // Méthode appelée lorsque le service est démarré
    // Elle gère les actions reçues et initialise les notifications
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
                    case ACTION_CHANGE_MUSIC:
                        stopMusic();
                        startMusic();
                        break;
                    case ACTION_STOP_MUSIC:
                        stopMusic();
                        break;
                    case ACTION_RESUME_MUSIC:
                        resumeMusic();
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

    // Démarre un exercice avec le sport et le mode chrono spécifiés
    public void startExercise(String sport, boolean isChronoMode) {
        startMusic();
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
            uiHandler.post(uiRunnable);
        }
    }

    // Met à jour l'état de l'exercice en pause
    public void pauseExercise() {
        pauseMusic();
        isRunning = false;
        updateNotification();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (startTimeMillis > 0) {
            totalDurationMillis += SystemClock.elapsedRealtime() - startTimeMillis;
            startTimeMillis = 0;
        }

        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.putExtra("isPaused", true);
        intent.putExtra("forceUpdateUI", true);
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

    // Reprend l'exercice en cours
    public void resumeExercise() {
        resumeMusic();
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

    // Arrête l'exercice en cours, met à jour les données et envoie une notification
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
        uiHandler.removeCallbacks(uiRunnable);
        uiHandler.removeCallbacks(uiRunnable);
        stopForeground(true);
        stopSelf();
    }

    // Démarre l'envoi des mises à jour de l'interface utilisateur
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
        String content = String.format(getString(R.string.General_duree)+" %ds - Distance: %.2fm - "+ getString(R.string.Summary_avg_speed)+" %.2fkm/h",
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

    // Démarre les mises à jour de localisation pour suivre l'exercice
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationListener = location -> gpsTrack.add(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);

        estimateDistance();
    }

    // Estime les calories brûlées en fonction du sport et de la distance parcourue
    private float estimateCalories(String sport, float distanceMeters) {
        float km = distanceMeters;
        switch (sport.toLowerCase()) {
            case "marche": return km * 50;
            case "course": return km * 60;
            case "vélo": return km * 40;
            default: return km * 35;
        }
    }

    // Estime la distance parcourue en fonction des données GPS ou du nombre de pas
    private float estimateDistance() {
        if(sportType != "marche" && gpsTrack.size() >= 2) {
            distance = 0;
            for (int i = 1; i < gpsTrack.size(); i++) {
                Location previousLocation = gpsTrack.get(i - 1);
                Location currentLocation = gpsTrack.get(i);
                distance += previousLocation.distanceTo(currentLocation);
            }

            Location lastLocation = gpsTrack.get(gpsTrack.size() - 1);
            Location secondLastLocation = gpsTrack.get(gpsTrack.size() - 2);
            long timeDelta = lastLocation.getTime() - secondLastLocation.getTime();
            if (timeDelta > 0) {
                speed = (distance / (timeDelta / 1000f)) * 3.6f;
            }
        } else {
            int stepCount = totalSteps - initialSteps;
            distance = stepCount * 0.7f;

            long elapsedSeconds = getElapsedExerciseTimeMillis() / 1000;
            if (elapsedSeconds > 0) {
                speed = (distance / elapsedSeconds) * 3.6f;
            }
        }

        distance = distance / 1000f;
        return distance;
    }


    // Crée une notification pour l'exercice en cours
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
        Intent changeMusicIntent = new Intent(this, ExerciseService.class);
        changeMusicIntent.setAction(ACTION_CHANGE_MUSIC);
        PendingIntent changeMusicPendingIntent = PendingIntent.getService(this, 4, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "exercise_channel")
                .setContentTitle("FitCoach - Exercice en cours")
                .setContentText(isRunning? content : "Exercise Paused")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true);


        if (isRunning) {
            notificationBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent);
        } else {
            notificationBuilder.addAction(android.R.drawable.ic_media_play, getString(R.string.Reprendre), resumePendingIntent);
        }
        notificationBuilder.addAction(android.R.drawable.ic_menu_delete, getString(R.string.arreter), stopPendingIntent);
        notificationBuilder.addAction(R.drawable.ic_next_music, getString(R.string.change_musique), changeMusicPendingIntent);

        return notificationBuilder.build();
    }

    // Met à jour la notification avec les informations actuelles de l'exercice
    private void updateNotification(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            String content = String.format(getString(R.string.General_duree)+" %ds - Distance: %.2fm - "+ getString(R.string.Summary_avg_speed)+" %.2fkm/h",
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
        stopMusic();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        stopForeground(true);
        Log.d("ExerciseService", "Exercice terminé, duration : "+totalDurationMillis+" repetitions : "+repetition+" steps : "+(totalSteps-initialSteps)+" calories : "+currentCalories+" distance : "+distance+" speed : "+speed+"");
    }

    // Méthode appelée pour lier le service à une activité
    @Override
    public IBinder onBind(Intent intent) {return null;}

    // Méthode pour obtenir le temps écoulé de l'exercice en millisecondes
    private long getElapsedExerciseTimeMillis() {
        if (isRunning) {
            return totalDurationMillis + (SystemClock.elapsedRealtime() - startTimeMillis);
        } else {
            return totalDurationMillis;
        }
    }

}
