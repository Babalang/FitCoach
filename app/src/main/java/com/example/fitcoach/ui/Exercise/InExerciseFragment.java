package com.example.fitcoach.ui.Exercise;

import static android.app.PendingIntent.getActivity;

import static java.security.AccessController.getContext;

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

import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.Services.ExerciseService;
import com.example.fitcoach.ui.Exercise.ExerciseStep;
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
            if (viewModel.getSteps() == null || viewModel.getSteps().isInitialized()) {
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
            NavHostFragment.findNavController(this).navigate(R.id.inexercise_to_execise);
        });
    }

    private void setupTimerView(View view) {
        viewModel.getCurrentStepIndex().observe(getViewLifecycleOwner(), val -> {
            if (current_step_value != null && viewModel.getSteps().getValue() != null && val < viewModel.getSteps().getValue().size()){
                current_step_value.setText(viewModel.getSteps().getValue().get(val).getName());
                if(next_step_value != null && val + 1 < viewModel.getSteps().getValue().size()){
                    next_step_value.setText(viewModel.getSteps().getValue().get(val + 1).getName());
                } else if(next_step_value != null){
                    next_step_value.setText("Fin de l'exercice");
                }

                if(timer != null){
                    timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
                    if(viewModel.getIsPaused().getValue()){
                        timer.start();
                    } else {
                        timer.stop();
                    }
                }
            }
        });

        current_step_value = view.findViewById(R.id.current_step_value);
        next_step_value = view.findViewById(R.id.next_step_value);
        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
        timer = view.findViewById(R.id.time_value);
        timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
        timer.setTimerListener(() -> {
            Toast.makeText(requireContext(), "Temps écoulé", Toast.LENGTH_SHORT).show();
            if (viewModel.getCurrentStepIndex().getValue() + 1 < viewModel.getSteps().getValue().size()) {
                viewModel.incrementStep();
                current_step_value.setText(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getName());
                if (viewModel.getCurrentStepIndex().getValue() + 1 < viewModel.getSteps().getValue().size())
                    next_step_value.setText(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue() + 1).getName());
                timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
                timer.start();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Exercice terminé")
                        .setMessage("Veux-tu recommencer ?")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            viewModel.setCurrentStep(0);
                            current_step_value.setText(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getName());
                            if (viewModel.getCurrentStepIndex().getValue() + 1 < viewModel.getSteps().getValue().size())
                                next_step_value.setText(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue() + 1).getName());
                            timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
                            timer.start();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            Log.d("InExerciseFragment", "Le service d'exercice a été arrêté.");
                            timer.stop();
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
                            NavHostFragment.findNavController(InExerciseFragment.this).navigate(R.id.inexercise_to_execise);
                        })
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .show();
            }
        });
        Button restart = view.findViewById(R.id.btn_restart);
        restart.setOnClickListener(v -> {
            timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
            timer.start();
            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_START));
        });
        Button next = view.findViewById(R.id.btn_next_step);
        next.setOnClickListener(v -> {
            if (viewModel.getCurrentStepIndex().getValue() + 1 < viewModel.getSteps().getValue().size()) {
                viewModel.incrementStep();
                timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
                timer.start();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Exercice terminé")
                        .setMessage("Veux-tu recommencer ?")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            viewModel.setCurrentStep(0);
                            timer.setCountdown(viewModel.getSteps().getValue().get(viewModel.getCurrentStepIndex().getValue()).getDuration());
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_INCREMENT_STEP));
                            timer.start();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            Log.d("InExerciseFragment", "Le service d'exercice a été arrêté.");
                            timer.stop();
                            localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_STOP));
                            NavHostFragment.findNavController(InExerciseFragment.this).navigate(R.id.inexercise_to_execise);
                        })
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .show();
            }
        });
        Button pause = view.findViewById(R.id.pause_button);
        pause.setOnClickListener(v -> {
            if (timer.isRunning()) {
                timer.stop();
                pause.setText("Reprendre");
                Toast.makeText(getContext(), "Pause", Toast.LENGTH_SHORT).show();
                localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_PAUSE));
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
            NavHostFragment.findNavController(this).navigate(R.id.inexercise_to_execise);
        });
        timer.start();
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
        // Demande l'état au service (s'il est actif il répondra)
        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_REQUEST_STATUS));

        // Lance un timer pour savoir s’il a répondu dans les 500ms
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
        unregisterReceiver(); // clean
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
            // Vérifier que l'intent contient les bonnes données
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
                    Log.d("ExerciseFragmentReceiver", "onReceive: " + duration+" "+calories+" "+distance+" "+speed);
                    // Mise à jour dans le ViewModel
                    viewModel.setCurrentStep(steps);
                    viewModel.setDuration(duration);
                    viewModel.setCalories(calories);
                    viewModel.setDistance(distance);
                    viewModel.setSpeed(speed);
                    viewModel.setIsPaused(isPaused);

                    if ("timer".equals(viewModel.getExerciseType().getValue())) {
                        viewModel.setCurrentStepIndex(repetition);
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
                    Log.d("ExerciseFragmentReceiver", "onReceive: " + steps+" "+calories+" "+distance+" "+speed);
                    // Mise à jour dans le ViewModel
                    viewModel.setCurrentStep(steps);
                    viewModel.setDuration(duration);
                    viewModel.setCalories(calories);
                    viewModel.setDistance(distance);
                    viewModel.setSpeed(speed);
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
        if (viewModel.getSteps() != null) {
            intent.putExtra("steps", viewModel.getSteps().getValue());
        }
        requireContext().startService(intent);
        localBroadcastManager.sendBroadcast(new Intent(ExerciseService.ACTION_START));
    }
}
