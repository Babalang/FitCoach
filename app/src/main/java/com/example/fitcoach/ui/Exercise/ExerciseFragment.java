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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.R;
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
        //set map
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        //set start position

        //init myPositionMarker
        initPositionMarker();
        Button btn1 = root.findViewById(R.id.button1);
        btn1.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.exercise_to_choose);
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        map.onResume();
        getLastLocation();
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
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            localBroadcastManager.unregisterReceiver(locationReceiver);
            isReceiverRegistered = false;
            locationReceiver = null;
            Log.d(TAG, "unregisterReceiver: LocationReceiver unregistered");
        } else {
            Toast.makeText(requireContext(), "Receiver not registered", Toast.LENGTH_SHORT).show();
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
            map.invalidate(); // Refresh the map to show the updated marker
        }
    }
    private void getLastLocation() {
        Log.d(TAG, "getLastLocation: function called");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "getLastLocation: permission not granted");
            return;
        }
        Log.d(TAG, "getLastLocation: permission granted");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
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