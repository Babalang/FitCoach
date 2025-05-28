package com.example.fitcoach.ui.home;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.Services.StepCounterService;
import com.example.fitcoach.databinding.FragmentHomeBinding;
import com.example.fitcoach.ui.history.Exercise;
import com.example.fitcoach.ui.history.HistoryFragment;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        // ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // View Binding
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

        int currentSteps = appDataManager.getSteps(0);  // Obtenir les pas depuis AppDataManager
        homeViewModel.setStepCount(currentSteps); // Initialiser le ViewModel avec les données sauvegardées

        // Observe ViewModel
        homeViewModel.getStepCount().observe(getViewLifecycleOwner(), stepCount -> {
            if (binding != null) {
                binding.circularGauge.setValue(stepCount);
                binding.circularGauge.setTotal(appDataManager.getStepsObjective(appDataManager.getCompteId()));
                binding.stepsInfo.setText(stepCount + " / "+appDataManager.getStepsObjective(appDataManager.getCompteId())+" pas");
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
                tvLastDate.setText(lastEntry.getDate()); // fallback en cas d'erreur
            }
        } else {
            tvLastSport.setText("Aucun");
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

    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        registerReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        binding = null;
    }

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
        } else {
            Toast.makeText(requireContext(), "Receiver not registered", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isServiceStarted() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).isServiceStarted();
        }
        return false;
    }
}
