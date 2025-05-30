package com.example.fitcoach.ui.Exercise;
// Classe pour accéder à la map et lancer un exercice
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

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.Serveur.ApiService;
import com.example.fitcoach.Serveur.RetrofitClient;
import com.example.fitcoach.Serveur.User;
import com.example.fitcoach.Services.ExerciseService;
import com.example.fitcoach.Services.LocationService;
import com.example.fitcoach.databinding.FragmentExerciseBinding;
import com.example.fitcoach.ui.Social.AmiAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private List<Marker> markers = new ArrayList<>();


    // OnAttach est appelé lorsque le fragment est attaché à son activité
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // addfetchUserById2 est une méthode pour récupérer les informations d'un utilisateur par son ID et ajouter un marqueur sur la carte
    private void addfetchUserById2(String userId, String num) {
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        Log.d(TAG, "coordonnees: "+user.getLatitude()+" "+user.getLongitude());
                        addMarker(new GeoPoint(user.getLongitude(), user.getLatitude()), num);
                    } else {
                        Log.e(TAG, "Réponse réussie mais corps vide pour getUserById " + userId);
                    }
                } else {
                    Log.e(TAG, "Erreur getUserById " + userId + " (code " + response.code() + "): " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Corps de l'erreur: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Erreur lors de la lecture du corps de l'erreur", e);
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec de l'appel getUserById " + userId + ": " + t.getMessage(), t);
            }
        });
    }

    // addfetchUserById est une méthode pour récupérer les informations d'un utilisateur par son ID et ajouter un marqueur sur la carte
    private void addfetchUserById(String userId) {
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        addMarker(new GeoPoint(user.getLongitude(), user.getLatitude()), user.getNom());
                        if(user.getAmi1()!=null){
                            addfetchUserById2(user.getAmi1(),"1");
                            if(user.getAmi2()!=null) addfetchUserById2(user.getAmi2(),"2");
                            if(user.getAmi3()!=null) addfetchUserById2(user.getAmi3(),"3");
                            if(user.getAmi4()!=null) addfetchUserById2(user.getAmi4(),"4");
                            if(user.getAmi5()!=null) addfetchUserById2(user.getAmi5(),"5");
                        }
                    } else {
                        Log.e(TAG, "Réponse réussie mais corps vide pour getUserById " + userId);
                    }
                } else {
                    Log.e(TAG, "Erreur getUserById " + userId + " (code " + response.code() + "): " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Corps de l'erreur: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Erreur lors de la lecture du corps de l'erreur", e);
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec de l'appel getUserById " + userId + ": " + t.getMessage(), t);
            }
        });
    }

    // onCreateView est appelé pour créer la vue du fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        AppDataManager appDataManager = AppDataManager.getInstance(getContext());
        int ide = appDataManager.getCompteId();
        AppDataManager.Compte compte = appDataManager.getCompteById(ide);
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
        addfetchUserById(compte.getLogin());
        addMarker(new GeoPoint(43.6047, 1.4442), "Toulouse");
        return root;
    }

    // checkServiceStatus vérifie si le service d'exercice est actif et envoie une demande de statut
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

    // navigateToInExercise navigue vers le fragment InExercise avec les paramètres actuels de sport et d'exercice
    private void navigateToInExercise() {
        Bundle bundle = new Bundle();
        bundle.putString("selected_sport", currentSportType != null ? currentSportType : "marche");
        bundle.putString("exercise_type", currentExerciseType != null ? currentExerciseType : "chrono");

        NavController controller = NavHostFragment.findNavController(this);
        controller.navigate(R.id.action_exercise_to_inexercise, bundle);
        Log.d(TAG, "Navigation directe vers l'exercice en cours: " + currentSportType + ", " + currentExerciseType);
    }

    // ServiceCheckReceiver est un BroadcastReceiver qui écoute les réponses du service d'exercice
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

    // unregisterServiceReceiver désenregistre le récepteur de vérification de service
    private void unregisterServiceReceiver() {
        if (isReceiverRegistered && serviceCheckReceiver != null) {
            localBroadcastManager.unregisterReceiver(serviceCheckReceiver);
            isReceiverRegistered = false;
            Log.d(TAG, "Récepteur de vérification de service désenregistré");
        }
    }

    // onResume est appelé lorsque le fragment reprend son activité
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        map.onResume();
        getLastLocation();
        checkServiceStatus();
    }

    // onPause est appelé lorsque le fragment est mis en pause
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    // onDestroyView est appelé lorsque la vue du fragment est détruite
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceCheckReceiver);
        binding = null;
    }

    // LocationReceiver est un BroadcastReceiver qui écoute les mises à jour de position du service de localisation
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

    // registerReceiver enregistre les récepteurs pour les mises à jour de position et le statut du service d'exercice
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

    // unregisterReceiver désenregistre les récepteurs pour les mises à jour de position et le statut du service d'exercice
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

    // initPositionMarker initialise le marqueur de position de l'utilisateur sur la carte
    private void initPositionMarker() {
        myPositionMarker = new Marker(map);
        Drawable myIcon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_my_location_24);
        myPositionMarker.setIcon(myIcon);
        myPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myPositionMarker);
    }

    // updatePositionMarker met à jour la position du marqueur de l'utilisateur sur la carte
    private void updatePositionMarker(GeoPoint geoPoint) {
        if (myPositionMarker != null) {
            myPositionMarker.setPosition(geoPoint);
            map.invalidate();
        }
    }

    // addMarker ajoute un marqueur à la carte à une position géographique donnée avec un titre
    private void addMarker(GeoPoint geoPoint, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setTitle(title);
        marker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.baseline_my_location_24));
        markers.add(marker);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    // updateMarker met à jour la position d'un marqueur existant à un index donné
    private void updateMarker(GeoPoint geoPoint, int index) {
        if (index < 0 || index >= markers.size()) {
            Log.e(TAG, "updateMarker: Index out of bounds");
            return;
        }
        Marker marker = markers.get(index);
        marker.setPosition(geoPoint);
        map.invalidate();
        Log.d(TAG, "updateMarker: Marker updated at index " + index);
    }

    // getLastLocation récupère la dernière position connue de l'utilisateur
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
                            enregistrerLocation(location.getLatitude(),location.getLongitude());
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

    // enregistrerLocation enregistre la position actuelle de l'utilisateur dans la base de données
    private void enregistrerLocation(double longitude,double latitude){
        ApiService apiService = RetrofitClient.getInstance();
        AppDataManager appDataManager = AppDataManager.getInstance(getContext());
        int ide = appDataManager.getCompteId();
        AppDataManager.Compte compte = appDataManager.getCompteById(ide);
        Call<User> call = apiService.setCoordonnee(compte.getLogin(),latitude,longitude);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "vol coordonnee reussi : ");
                } else {
                    Log.e(TAG, "coordonnee non valide");
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Nom non valide");
            }
        });
    }
}