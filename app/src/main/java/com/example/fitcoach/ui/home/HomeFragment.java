package com.example.fitcoach.ui.home;

import android.content.BroadcastReceiver;
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

import com.example.fitcoach.MainActivity;
import com.example.fitcoach.Services.StepCounterService;
import com.example.fitcoach.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private StepCountReceiver stepCountReceiver;
    private com.example.fitcoach.utils.CircularGauge circularGauge;
    private boolean isReceiverRegistered = false;
    private LocalBroadcastManager localBroadcastManager;
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        Toast.makeText(requireContext(), "onCreateView called", Toast.LENGTH_SHORT).show();
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        circularGauge = binding.circularGauge;
        circularGauge.setValue(0f);
        circularGauge.setTotal(10000f);

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        Toast.makeText(requireContext(), "onResume called", Toast.LENGTH_SHORT).show();
        registerReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toast.makeText(requireContext(), "onDestroyView called", Toast.LENGTH_SHORT).show();
        binding = null;
        // Désenregistrer le BroadcastReceiver
        unregisterReceiver();
    }

    private class StepCountReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(requireContext(), "onReceive called", Toast.LENGTH_SHORT).show();
            if (StepCounterService.ACTION_STEP_COUNT_UPDATE.equals(intent.getAction())) {
                int stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0);
                circularGauge.setValue(stepCount);
            }
        }
    }

    private void registerReceiver() {
        Toast.makeText(requireContext(), "registerReceiver called", Toast.LENGTH_SHORT).show();
        if (!isReceiverRegistered && isServiceStarted()) {
            stepCountReceiver = new StepCountReceiver();
            IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_COUNT_UPDATE);
            Toast.makeText(requireContext(), "register receiver", Toast.LENGTH_SHORT).show();
            localBroadcastManager.registerReceiver(stepCountReceiver, filter);
            isReceiverRegistered = true;
        } else {
            Toast.makeText(requireContext(), "registerReceiver not registering : isRegistered : " + isReceiverRegistered + "isServiceStarted : " + isServiceStarted(), Toast.LENGTH_LONG).show();
        }
    }

    private void unregisterReceiver() {
        Toast.makeText(requireContext(), "unregisterReceiver called", Toast.LENGTH_SHORT).show();
        if (isReceiverRegistered) {
            Toast.makeText(requireContext(), "unregister receiver", Toast.LENGTH_SHORT).show();
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