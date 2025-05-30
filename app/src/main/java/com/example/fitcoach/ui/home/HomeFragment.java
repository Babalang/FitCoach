package com.example.fitcoach.ui.home;
// Fragment pour afficher les informations de la page d'accueil de l'application FitCoach
import  android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.Services.StepCounterService;
import com.example.fitcoach.databinding.FragmentHomeBinding;
import com.example.fitcoach.ui.history.Exercise;
import androidx.navigation.fragment.NavHostFragment;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private StepCountReceiver stepCountReceiver;
    private boolean isReceiverRegistered = false;
    private LocalBroadcastManager localBroadcastManager;
    private HomeViewModel homeViewModel;
    private static final String TAG = "HomeFragment";
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Méthode pour créer la vue du fragment et initialiser les éléments de l'interface utilisateur
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        AppDataManager appDataManager = AppDataManager.getInstance();
        TextView distance = binding.distanceInfo;
        TextView caloriesInfo = binding.caloriesInfo;
        homeViewModel.getDistance().observe(getViewLifecycleOwner(), dist -> {
            if (binding != null) {
                distance.setText(decimalFormat.format(dist)+" km");
            }
        });
        homeViewModel.getCalories().observe(getViewLifecycleOwner(), cal -> {
            if (binding != null) {
                caloriesInfo.setText(decimalFormat.format(cal) + " / "+appDataManager.getCaloriesObjective(appDataManager.getCompteId())+" kcal");
            }
        });
        int currentSteps = appDataManager.getSteps(0);
        homeViewModel.setStepCount(currentSteps);
        homeViewModel.getStepCount().observe(getViewLifecycleOwner(), stepCount -> {
            if (binding != null) {
                binding.circularGauge.setValue(stepCount);
                binding.circularGauge.setTotal(appDataManager.getStepsObjective(appDataManager.getCompteId()));
                binding.stepsInfo.setText(stepCount + " / "+appDataManager.getStepsObjective(appDataManager.getCompteId())+" " + getString(R.string.General_pas));
            }
        });
        Exercise lastEntry = appDataManager.getLastHistorique();
        TextView tvLastSport = binding.exerciseNameInfo;
        TextView tvLastCalories = binding.calInfo;
        TextView tvLastDate = binding.dateInfo;
        if (lastEntry != null) {
            tvLastSport.setText(lastEntry.getSport());
            tvLastCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", lastEntry.getCalories()));
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = outputFormat.format(inputFormat.parse(lastEntry.getDate()));
                tvLastDate.setText(formattedDate);
            } catch (Exception e) {
                tvLastDate.setText(lastEntry.getDate());
            }
        } else {
            tvLastSport.setText("None");
            tvLastCalories.setText("0 kcal");
            tvLastDate.setText("-");
        }
        binding.cardContainerHistory.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_home_to_history);
        });
        binding.buttonBottom.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_home_to_exercise);
        });
        return root;
    }

    // Méthode pour la reprise du fragment, où le BroadcastReceiver est enregistré pour recevoir les mises à jour du service de comptage de pas
    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        registerReceiver();
    }

    // Méthode pour la destruction de la vue du fragment, où le BroadcastReceiver est désenregistré pour éviter les fuites de mémoire
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        binding = null;
    }

    // Classe interne pour recevoir les mises à jour du service de comptage de pas
    private class StepCountReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_STEP_COUNT_UPDATE.equals(intent.getAction())) {
                int stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0);
                float calories = intent.getFloatExtra("calories", 0f);
                float distance = intent.getFloatExtra("distance", 0f);
                homeViewModel.setCalories(calories);
                homeViewModel.setDistance(distance);
                homeViewModel.setStepCount(stepCount);
            }
        }
    }

    // Méthodes pour enregistrer et désenregistrer le BroadcastReceiver
    private void registerReceiver() {
        if (!isReceiverRegistered) {
            stepCountReceiver = new StepCountReceiver();
            IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_COUNT_UPDATE);
            localBroadcastManager.registerReceiver(stepCountReceiver, filter);
            isReceiverRegistered = true;
        }
    }
    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            localBroadcastManager.unregisterReceiver(stepCountReceiver);
            isReceiverRegistered = false;
        }
    }

    // Méthode pour vérifier si le service de comptage de pas est démarré
    public boolean isServiceStarted() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).isServiceStarted();
        }
        return false;
    }
}
