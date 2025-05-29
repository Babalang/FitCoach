package com.example.fitcoach.ui.Exercise;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.R;
import com.example.fitcoach.Services.ExerciseService;
import com.example.fitcoach.Services.LocationService;
import com.example.fitcoach.databinding.FragmentExerciseBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ExerciseFragment extends Fragment {
    private FragmentExerciseBinding binding;
    private MapView map;
    private LocationReceiver locationReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private ServiceCheckReceiver serviceCheckReceiver;

    private boolean isServiceActive = false;
    private String currentSportType = null;
    private String currentExerciseType = null;
    private static final String TAG = "ExerciseFragment";
    private Marker myPositionMarker;
    private boolean isReceiverRegistered = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        binding = FragmentExerciseBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        initPositionMarker();
        checkServiceStatus();
        Button btn1 = root.findViewById(R.id.button1);
        btn1.setOnClickListener(v -> {
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_exercise_to_choose);
        });

        return root;
    }

    private void checkServiceStatus() {
        if (serviceCheckReceiver == null) {
            serviceCheckReceiver = new ServiceCheckReceiver();
        }

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ExerciseService.ACTION_SEND_STATUS);
            localBroadcastManager.registerReceiver(serviceCheckReceiver, filter);
            isReceiverRegistered = true;
            Log.d(TAG, "Récepteur enregistré pour vérifier le statut du service");
        }

        Intent statusRequest = new Intent(ExerciseService.ACTION_REQUEST_STATUS);
        localBroadcastManager.sendBroadcast(statusRequest);
        Log.d(TAG, "Demande de statut envoyée au service");
        new Handler().postDelayed(() -> {
            if (!isServiceActive && isReceiverRegistered) {
                unregisterServiceReceiver();
                Log.d(TAG, "Aucune réponse du service après délai d'attente");
            }
        }, 1000);
    }

    private void navigateToInExercise() {
        Bundle bundle = new Bundle();
        bundle.putString("selected_sport", currentSportType != null ? currentSportType : "marche");
        bundle.putString("exercise_type", currentExerciseType != null ? currentExerciseType : "chrono");

        NavController controller = NavHostFragment.findNavController(this);
        controller.navigate(R.id.action_exercise_to_inexercise, bundle);
        Log.d(TAG, "Navigation directe vers l'exercice en cours: " + currentSportType + ", " + currentExerciseType);
    }

    private class ServiceCheckReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ExerciseService.ACTION_SEND_STATUS.equals(intent.getAction()) && isAdded() && getActivity() != null) {
                isServiceActive = true;
                currentSportType = intent.getStringExtra("sportType");
                boolean isRunning = intent.getBooleanExtra("isRunning", true);

                Log.d(TAG, "Réponse du service reçue - sport: " + currentSportType + ", isRunning: " + isRunning);

                if (isServiceActive) {
                    currentExerciseType = (intent.getBooleanExtra("isChronoMode", true)) ? "chrono" : "timer";
                    navigateToInExercise();
                }

                unregisterServiceReceiver();
            }
        }
    }

    private void unregisterServiceReceiver() {
        if (isReceiverRegistered && serviceCheckReceiver != null) {
            localBroadcastManager.unregisterReceiver(serviceCheckReceiver);
            isReceiverRegistered = false;
            Log.d(TAG, "Récepteur de vérification de service désenregistré");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        map.onResume();
        getLastLocation();
        checkServiceStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceCheckReceiver);
        binding = null;
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Position received");
            if (LocationService.ACTION_LOCATION_UPDATE.equals(intent.getAction())) {
                double latitude = intent.getDoubleExtra(LocationService.EXTRA_LAT, 0.0);
                double longitude = intent.getDoubleExtra(LocationService.EXTRA_LON, 0.0);
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                map.getController().setCenter(geoPoint);
                updatePositionMarker(geoPoint);
                Log.d(TAG, "onReceive: position updated");
            }
        }
    }

    private void registerReceiver() {
        if(!isReceiverRegistered){
            locationReceiver = new LocationReceiver();
            IntentFilter filter = new IntentFilter(LocationService.ACTION_LOCATION_UPDATE);
            localBroadcastManager.registerReceiver(locationReceiver, filter);
            isReceiverRegistered = true;
            Log.d(TAG, "registerReceiver: LocationReceiver registered");
        }
        if (!isReceiverRegistered) {
            serviceCheckReceiver = new ServiceCheckReceiver();
            IntentFilter filter = new IntentFilter(ExerciseService.ACTION_SEND_STATUS);
            localBroadcastManager.registerReceiver(serviceCheckReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            localBroadcastManager.unregisterReceiver(locationReceiver);
            isReceiverRegistered = false;
            locationReceiver = null;
            Log.d(TAG, "unregisterReceiver: LocationReceiver unregistered");
        }
        if (isReceiverRegistered && localBroadcastManager != null && serviceCheckReceiver != null) {
            localBroadcastManager.unregisterReceiver(serviceCheckReceiver);
            isReceiverRegistered = false;
        }
    }
    private void initPositionMarker() {
        myPositionMarker = new Marker(map);
        Drawable myIcon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_my_location_24);
        myPositionMarker.setIcon(myIcon);
        myPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myPositionMarker);
    }

    private void updatePositionMarker(GeoPoint geoPoint) {
        if (myPositionMarker != null) {
            myPositionMarker.setPosition(geoPoint);
            map.invalidate();
        }
    }
    private void getLastLocation() {
        Log.d(TAG, "getLastLocation: function called");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "getLastLocation: permission not granted");
            return;
        }
        Log.d(TAG, "getLastLocation: permission granted");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lastKnownLocation = location;
                            Log.d(TAG, "getLastLocation: position found");
                            map.getController().setZoom(18.0);
                            map.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                            updatePositionMarker(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        } else {
                            Log.d(TAG, "getLastLocation: position not found");
                            map.getController().setZoom(18.0);
                            map.getController().setCenter(new GeoPoint(43.6047, 1.4442));
                        }
                    }
                });
    }
}