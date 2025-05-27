package com.example.fitcoach.ui.Exercise;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.example.fitcoach.R;
import com.example.fitcoach.Services.ExerciseService;

public class ChooseExerciseFragment extends Fragment {

    private Spinner sportSpinner;
    private ImageView sportImage;
    private Button startButton;

    private String[] sports;
    private String[] sportsTimer;
    private int[] sportImages = {
            R.drawable.appli_icon,         // Assure-toi d’avoir ces ressources
            R.drawable.walking,
            R.drawable.bike,
            R.drawable.appli_icon
    };
    private int[] sportImagesTimer = {
            R.drawable.musculation,
            R.drawable.appli_icon,
            R.drawable.appli_icon
    };
    private List<ExerciseStep> timerSteps = new ArrayList<>();
    private RecyclerView stepsRecycler;
    private Button addStepButton;
    private ExerciseStepAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_exercise, container, false);

        // Initialisation
        sportSpinner = view.findViewById(R.id.sport_spinner);
        sportImage = view.findViewById(R.id.sport_image);
        startButton = view.findViewById(R.id.start_exercise_button);
        sportImage.setVisibility(View.INVISIBLE);
        sportSpinner.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        RadioGroup group = view.findViewById(R.id.exercise_type_group);
        stepsRecycler = view.findViewById(R.id.timer_steps_recycler);
        addStepButton = view.findViewById(R.id.add_step_button);
        adapter = new ExerciseStepAdapter(timerSteps);
        stepsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        stepsRecycler.setAdapter(adapter);

        group.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.gps_radio) {
                stepsRecycler.setVisibility(View.GONE);
                addStepButton.setVisibility(View.GONE);
                sportImage.setVisibility(View.VISIBLE);
                sportSpinner.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.VISIBLE);
                sports = getResources().getStringArray(R.array.sports_array);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, sports);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sportSpinner.setAdapter(adapter);

                // Image change en fonction du choix
                sportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < sportImages.length) {
                            sportImage.setImageResource(sportImages[position]);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } else if (checkedId == R.id.timer_radio) {
                stepsRecycler.setVisibility(View.VISIBLE);
                //addStepButton.setVisibility(View.VISIBLE);

                //addStepButton.setOnClickListener(v -> {
                //    timerSteps.add(new ExerciseStep("Nouvelle étape", 30));
                //    adapter.notifyItemInserted(timerSteps.size() - 1);
                //});

                sportImage.setVisibility(View.VISIBLE);
                sportSpinner.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.VISIBLE);
                sportsTimer = getResources().getStringArray(R.array.sportsTimer_array);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, sportsTimer);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sportSpinner.setAdapter(adapter);

                // Image change en fonction du choix
                sportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < sportImagesTimer.length) {
                            sportImage.setImageResource(sportImagesTimer[position]);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

            }
        });

        // Bouton démarrer
        startButton.setOnClickListener(v -> {
            String selectedSport = (String) sportSpinner.getSelectedItem();
            int checkedId = group.getCheckedRadioButtonId();
            boolean isChrono = true;
            String exerciseType = "chrono";

            ArrayList<ExerciseStep> stepsToSend = isChrono ? new ArrayList<>() : new ArrayList<>(timerSteps);

            Bundle bundle = new Bundle();
            bundle.putString("selected_sport", selectedSport);
            bundle.putString("exercise_type", exerciseType);
            bundle.putSerializable("timer_steps", stepsToSend);
            Log.d("ChooseExerciseFragment", "ExerciseService started, isChrono: " + isChrono + ", stepsToSend: " + stepsToSend.size() + ", selectedSport: " + selectedSport + ", exerciseType: " + exerciseType);

            Navigation.findNavController(v).navigate(R.id.choose_to_inexercise, bundle);
        });

        return view;
    }

    private void startExerciseService(String sportType, boolean isChrono, ArrayList<ExerciseStep> steps) {
        Intent intent = new Intent(requireContext(), ExerciseService.class);
        intent.putExtra("sport_type", sportType);
        intent.putExtra("is_chrono", isChrono);
        requireContext().startService(intent);
    }


}
