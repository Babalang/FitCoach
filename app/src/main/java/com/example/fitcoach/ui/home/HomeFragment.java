package com.example.fitcoach.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.example.fitcoach.MainActivity;
import com.example.fitcoach.Services.StepCounterService;
import com.example.fitcoach.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private StepCountReceiver stepCountReceiver;
    private com.example.fitcoach.utils.CircularGauge circularGauge;
    private boolean isReceiverRegistered = false;
    private LocalBroadcastManager localBroadcastManager;
    private int currentSteps;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE);
        currentSteps = sharedPreferences.getInt("current_steps", 0);

        circularGauge = binding.circularGauge;
        circularGauge.setValue(currentSteps);
        circularGauge.setTotal(10000);

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

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
        binding = null;
        // Désenregistrer le BroadcastReceiver
        unregisterReceiver();
    }

    private class StepCountReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_STEP_COUNT_UPDATE.equals(intent.getAction())) {
                int stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0);
                circularGauge.setValue(stepCount);
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
            Toast.makeText(requireContext(), "unregisterReceiver not unregistering : isRegistered : " + isReceiverRegistered, Toast.LENGTH_LONG).show();
        }
    }

    public boolean isServiceStarted() {
        if(getActivity() != null){
            if (getActivity() instanceof MainActivity) {
                return ((MainActivity) getActivity()).isServiceStarted();
            }
        }
        return false;
    }
}