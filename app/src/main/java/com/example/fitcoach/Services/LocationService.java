package com.example.fitcoach.Services;
// Classe pour le service de localisation
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fitcoach.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public static final String ACTION_LOCATION_UPDATE = "com.example.fitcoach.LOCATION_UPDATE";
    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";
    private static final String TAG = "LocationService";

    // Liaison pour le service de localisation
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Méthode appelée lors de la création du service
    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
    }

    // Méthode appelée lors du démarrage du service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    // Méthode appelée lors de la destruction du service
    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    // Méthode pour créer une requête de localisation
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();
    }

    // Méthode pour créer un rappel de localisation
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    sendLocationBroadcast(location);
                }
            }
        };
    }

    // Méthode pour envoyer une diffusion de mise à jour de localisation
    private void sendLocationBroadcast(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_UPDATE);
        intent.putExtra(EXTRA_LAT, location.getLatitude());
        intent.putExtra(EXTRA_LON, location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "sendLocationBroadcast: position send " + location.getLatitude() + ", " + location.getLongitude());
    }

    // Méthodes pour démarrer et arrêter les mises à jour de localisation
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    // Méthode pour arrêter les mises à jour de localisation
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Méthode pour créer une notification pour le service de localisation
    private Notification createNotification() {
        String channelId = "location_service_channel";
        String channelName = "Location Service Channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Location Service")
                .setContentText("Tracking your location")
                .setSmallIcon(R.drawable.appli_icon);
        return builder.build();
    }
}