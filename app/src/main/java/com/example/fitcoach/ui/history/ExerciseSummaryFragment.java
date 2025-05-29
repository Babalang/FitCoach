package com.example.fitcoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.databinding.FragmentExerciseSummaryBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExerciseSummaryFragment extends Fragment {

    private FragmentExerciseSummaryBinding binding;
    private boolean exerciseSaved = false;

    private String sportType;
    private long duration;
    private int steps;
    private float calories;
    private float distance;
    private float speed;
    private int repetition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExerciseSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {

            if(getArguments().getSerializable("exercise") == null) {

                steps = getArguments().getInt("steps", 0);
                duration = getArguments().getLong("duration", 0);
                calories = getArguments().getFloat("calories", 0);
                distance = getArguments().getFloat("distance", 0);
                speed = getArguments().getFloat("speed", 0);
                sportType = getArguments().getString("sportType", "marche");
                repetition = getArguments().getInt("repetition", 0);

                // Afficher les données dans l'interface
                binding.tvSportType.setText(sportType);
                binding.tvDuration.setText(formatDuration(duration));
                binding.tvSteps.setText(String.valueOf(steps));
                binding.tvCalories.setText(String.format("%.1f kcal", calories));
                binding.tvDistance.setText(String.format("%.2f km", distance));
                binding.tvSpeed.setText(String.format("%.1f km/h", speed));
            } else {
                Exercise exercise = (Exercise) getArguments().getSerializable("exercise");
                if (exercise != null) {
                    sportType = exercise.getSport();
                    duration = exercise.getDuration();
                    steps = exercise.getSteps();
                    calories = exercise.getCalories();
                    distance = exercise.getDistance();
                    speed = exercise.getSpeed();
                    repetition = exercise.getRepetition();

                    // Afficher les données dans l'interface
                    binding.tvSportType.setText(sportType);
                    binding.tvDuration.setText(formatDuration(duration));
                    binding.tvSteps.setText(String.valueOf(steps));
                    binding.tvCalories.setText(String.format("%.1f kcal", calories));
                    binding.tvDistance.setText(String.format("%.2f km", distance));
                    binding.tvSpeed.setText(String.format("%.1f km/h", speed));
                    binding.btnSaveToHistory.setVisibility(View.GONE);
                }
            }

            if (repetition > 0) {
                binding.tvRepetitionLabel.setVisibility(View.VISIBLE);
                binding.tvRepetition.setVisibility(View.VISIBLE);
                binding.tvRepetition.setText(String.valueOf(repetition));
            } else {
                binding.tvRepetitionLabel.setVisibility(View.GONE);
                binding.tvRepetition.setVisibility(View.GONE);
            }
        }

        binding.btnSaveToHistory.setOnClickListener(v -> {
            if (!exerciseSaved) {
                saveExerciseToHistory();
                exerciseSaved = true;
                binding.btnSaveToHistory.setText("Enregistré ✓");
                binding.btnSaveToHistory.setEnabled(false);
            }
        });

        binding.btnBackToHome.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_summary_to_history);
        });
    }

    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void saveExerciseToHistory() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        AppDataManager dataManager = AppDataManager.getInstance(requireContext());
        dataManager.insertHistorique(currentDate, sportType, (int) calories, steps, distance, duration, repetition, speed);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}