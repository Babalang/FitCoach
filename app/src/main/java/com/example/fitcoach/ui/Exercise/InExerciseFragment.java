package com.example.fitcoach.ui.Exercise;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.R;
import com.example.fitcoach.Services.ExerciseService;
import com.example.fitcoach.utils.Timer;

import java.util.ArrayList;

public class InExerciseFragment extends Fragment {
    private ExerciseViewModel viewModel;


    private ExerciseService exerciseService;
    private ExerciseFragmentReceiver exerciseReceiver;
    private boolean isReceiverRegistered = false;
    private LocalBroadcastManager localBroadcastManager;

    private boolean isServiceBound = false;
    private Timer timer;
    private TextView textPas, textCalories, textDistance, textSpeed, current_step_value, next_step_value;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        setHasOptionsMenu(true);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());

        Bundle args = getArguments();
        if (args != null) {
            viewModel.setExerciseType(args.getString("exercise_type"));
            viewModel.setSportType(args.getString("selected_sport"));
            viewModel.setSteps((ArrayList<ExerciseStep>) args.getSerializable("timer_steps"));
        }
        if (viewModel.getExerciseType() == null) {
            return null;
        }

        View view;
        if ("timer".equals(viewModel.getExerciseType().getValue())) {
            view = inflater.inflate(R.layout.fragment_in_exercise_timer, container, false);
            viewModel.setSteps((ArrayList<ExerciseStep>)args.getSerializable("timer_steps"));
            if (viewModel.getSteps() == null || !viewModel.getSteps().isInitialized()) {
                Toast.makeText(getContext(), "Aucune étape trouvée", Toast.LENGTH_SHORT).show();
                return view;
            }
            setupTimerView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_in_exercise, container, false);
            setupChronoView(view);
        }
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), val -> {
            if (textPas != null)
                textPas.setText("Pas : " + val);
        });
        viewModel.getCalories().observe(getViewLifecycleOwner(), val -> {
            if (textCalories != null)
                textCalories.setText("Calories : " + val + " kcal");
        });

        viewModel.getDistance().observe(getViewLifecycleOwner(), val -> {
            if (textDistance != null)
                textDistance.setText("Distance : " + val + " km");
        });

        viewModel.getSpeed().observe(getViewLifecycleOwner(), val -> {
            if (textSpeed != null)
                textSpeed.setText("Vitesse : " + val + " km/h");
        });


        return view;
    }

    private void setupChronoView(View view) {
        timer = view.findViewById(R.id.time_value);
        timer.setChronoMode();
        timer.start();

        textPas = view.findViewById(R.id.steps_value);
        textCalories = view.findViewById(R.id.calories_value);
        textDistance = view.findViewById(R.id.distance_value);
        textSpeed = view.findViewById(R.id.speed_value);

        Button pause = view.findViewById(R.id.pause_button);
        pause.setOnClickListener(v -> {
            if (timer.isRunning()) {
                timer.stop();
                pause.setText("Reprendre");
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_PAUSE));
                Toast.makeText(getContext(), "Pause", Toast.LENGTH_SHORT).show();
            } else {
                timer.start();
                pause.setText("Pause");
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_RESUME));
                Toast.makeText(getContext(), "Reprise", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.stop_button).setOnClickListener(v -> {
            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
            timer.stop();
            Toast.makeText(getContext(), "Exercice terminé", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTimerView(View view) {
        timer = view.findViewById(R.id.time_value);
        current_step_value = view.findViewById(R.id.current_step_value);
        next_step_value = view.findViewById(R.id.next_step_value);
        Button pauseButton = view.findViewById(R.id.pause_button);
        Button stopButton = view.findViewById(R.id.stop_button);
        Button nextStepButton = view.findViewById(R.id.btn_next_step);
        Button restartButton = view.findViewById(R.id.btn_restart);

        viewModel.getCurrentStepIndex().observe(getViewLifecycleOwner(), index -> {
            ArrayList<ExerciseStep> steps = viewModel.getSteps().getValue();
            if (current_step_value != null && steps != null && index < steps.size()) {
                current_step_value.setText(steps.get(index).getName());

                if (next_step_value != null) {
                    if (index + 1 < steps.size()) {
                        next_step_value.setText(steps.get(index + 1).getName());
                    } else {
                        next_step_value.setText("Fin de l'exercice");
                    }
                }

                if (timer != null) {
                    timer.setCountdown(steps.get(index).getDuration());
                    timer.reset();
                    timer.start();
                }
            }
        });

        timer.setTimerListener(() -> {
            ArrayList<ExerciseStep> steps = viewModel.getSteps().getValue();
            Integer currentIndex = viewModel.getCurrentStepIndex().getValue();

            if (steps != null && currentIndex != null && currentIndex + 1 < steps.size()) {
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));

            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Exercice terminé")
                        .setMessage("Veux-tu recommencer ?")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
                            timer.start();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
                            timer.stop();
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        pauseButton.setOnClickListener(v -> {
            if (timer.isRunning()) {
                timer.stop();
                pauseButton.setText("Reprendre");
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_PAUSE));
                Toast.makeText(requireContext(), "Pause", Toast.LENGTH_SHORT).show();
            } else {
                timer.start();
                pauseButton.setText("Pause");
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_RESUME));
                Toast.makeText(requireContext(), "Reprise", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Arrêter l'exercice")
                    .setMessage("Voulez-vous vraiment arrêter l'exercice ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        timer.stop();
                        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        nextStepButton.setOnClickListener(v -> {
            if(viewModel.getCurrentStepIndex().getValue() + 1 < viewModel.getSteps().getValue().size()) {
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Exercice terminé")
                        .setMessage("Veux-tu recommencer ?")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
                            timer.start();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
                            timer.stop();
                        })
                        .setCancelable(false)
                        .show();
            }
            Log.d("InExerciseFragment", "nextStepButton clicked");
        });

        restartButton.setOnClickListener(v -> {
            timer.reset();
            timer.start();
        });

        viewModel.setCurrentStepIndex(0);

        ArrayList<ExerciseStep> steps = viewModel.getSteps().getValue();
        if (steps != null && !steps.isEmpty()) {
            timer.setCountdown(steps.get(0).getDuration());
            timer.reset();
            timer.start();
        }
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
    }


    @Override
    public void onStart() {
        super.onStart();
        registerReceiver();
        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_REQUEST_STATUS));

        new Handler().postDelayed(() -> {
            if (!isServiceBound) {
                if (getArguments() != null && getArguments().containsKey("selected_sport")) {
                    startExerciseService();
                }            }
        }, 500);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavHostFragment.findNavController(this).navigate(R.id.inexercise_to_execise);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ExerciseFragmentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ExerciseService.ACTION_SEND_STATUS.equals(intent.getAction())) {
                    isServiceBound = true;
                    int steps = intent.getIntExtra("steps", 0);
                    long duration = intent.getLongExtra("duration", 0L);
                    float calories = intent.getFloatExtra("calories", 0);
                    float distance = intent.getFloatExtra("distance", 0);
                    float speed = intent.getFloatExtra("speed", 0);
                    boolean isPaused = !intent.getBooleanExtra("isRunning", true);
                    int repetition = intent.getIntExtra("repetition", 0);
                    boolean isStopping = intent.getBooleanExtra("isStopping", false);
                    Log.d("ExerciseFragmentReceiver", "onReceive: " + duration+" "+calories+" "+distance+" "+speed);
                    if(isStopping){
                        if(timer != null) timer.stop();
                        if(getView() != null && isAdded() && !isRemoving()){
                            NavHostFragment.findNavController(InExerciseFragment.this).navigate(R.id.inexercise_to_home);
                        }
                        return;
                    }
                    viewModel.setCurrentStep(steps);
                    viewModel.setDuration(duration);
                    viewModel.setCalories(calories);
                    viewModel.setDistance(distance);
                    viewModel.setSpeed(speed);
                    viewModel.setIsPaused(isPaused);

                    Log.d("inExerciseFragment", "onReceive: " + repetition);
                    if ("timer".equals(viewModel.getExerciseType().getValue())) {
                        Log.d("inExerciseFragment", "onReceive: " + repetition);
                        viewModel.setCurrentStepIndex(repetition % (viewModel.getSteps().getValue().size()));
                    }
                    if (timer != null) {
                        if ("chrono".equals(viewModel.getExerciseType().getValue())) {
                            timer.setElapsedTime(duration);
                        }
                        if (isPaused) {
                            timer.stop();
                        } else {
                            timer.start();
                        }
                    }
                    Button pauseButton = getView().findViewById(R.id.pause_button);
                    if(pauseButton!=null){
                        pauseButton.setText(isPaused? "Reprendre" : "Pause");
                    }
                } else if (ExerciseService.ACTION_UPDATE_UI.equals(intent.getAction())) {
                    int steps = intent.getIntExtra("steps", 0);
                    long duration = intent.getLongExtra("duration", 0L);
                    float calories = intent.getFloatExtra("calories", 0);
                    float distance = intent.getFloatExtra("distance", 0);
                    float speed = intent.getFloatExtra("speed", 0);
                    String currentStepName = intent.getStringExtra("currentSteps");
                    String nextStepName = intent.getStringExtra("nextSteps");
                    Log.d("ExerciseFragmentReceiver", "onReceive: " + steps+" "+calories+" "+distance+" "+speed);
                    int repetition = intent.getIntExtra("repetition", 0);
                    viewModel.setCurrentStep(steps);
                    viewModel.setDuration(duration);
                    viewModel.setCalories(calories);
                    viewModel.setDistance(distance);
                    viewModel.setSpeed(speed);
                    Log.d("inExerciseFragment", "onReceive: " + repetition);
                    if ("timer".equals(viewModel.getExerciseType().getValue()) && viewModel.getSteps() != null && viewModel.getSteps().getValue() != null && viewModel.getCurrentStepIndex().getValue() != (repetition % (viewModel.getSteps().getValue().size())) ) {
                        Log.d("inExerciseFragment", "onReceive: " + repetition);
                        viewModel.setCurrentStepIndex(repetition % (viewModel.getSteps().getValue().size()));
                    }
                }


            }
        }
    }

            private void registerReceiver() {
                if (!isReceiverRegistered) {
                    exerciseReceiver = new ExerciseFragmentReceiver();
                    IntentFilter filter = new IntentFilter(ExerciseService.ACTION_UPDATE_UI);
                    filter.addAction(ExerciseService.ACTION_SEND_STATUS);
                    localBroadcastManager.registerReceiver(exerciseReceiver,filter);
                    isReceiverRegistered = true;
                }
            }

            private void unregisterReceiver() {
                if (isReceiverRegistered) {
                    localBroadcastManager.unregisterReceiver(exerciseReceiver);
                    isReceiverRegistered = false;
                } else {
                    Toast.makeText(requireContext(), "Receiver not registered", Toast.LENGTH_SHORT).show();
                }
            }

    private void updateUI(int steps, float calories, float distance, float speed) {
        if(textPas == null) return;
        textPas.setText("Pas : " + steps);
        textCalories.setText("Calories : " + calories + " kcal");
        textDistance.setText("Distance : " + distance + " km");
        textSpeed.setText("Vitesse : " + speed + " km/h");
    }

    private void startExerciseService() {
        Intent intent = new Intent(requireContext(), ExerciseService.class);
        intent.setAction(ExerciseService.ACTION_START);
        intent.putExtra("sport", viewModel.getSportType().getValue());
        intent.putExtra("isChronoMode", "chrono".equals(viewModel.getExerciseType().getValue()));

        ArrayList<ExerciseStep> steps = viewModel.getSteps().getValue();
        if (viewModel.getSteps() != null && steps != null && !steps.isEmpty()) {
            Bundle stepsBundle = new Bundle();
            stepsBundle.putSerializable("steps", steps);
            intent.putExtras(stepsBundle);
        }
        requireContext().startService(intent);
        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_START));
    }
}
