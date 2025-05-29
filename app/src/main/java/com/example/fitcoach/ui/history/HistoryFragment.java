package com.example.fitcoach.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.ui.Exercise.InExerciseFragment;

import java.util.Arrays;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = root.findViewById(R.id.history_recycler);
        // Récupérer les exercices depuis la base de données
        AppDataManager dataManager = AppDataManager.getInstance(requireContext());

        List<Exercise> exerciseList = dataManager.getAllHistorique();

        HistoryAdapter adapter = new HistoryAdapter(exerciseList, exercise -> {
            Bundle bundle = new Bundle();
            Log.d("HistoryFragment", "Exercise clicked: " + exercise.getSport() + " on " + exercise.getDate() + " with " + exercise.getSteps() + " steps");
            bundle.putSerializable("exercise", exercise);
            NavController navController = NavHostFragment.findNavController(HistoryFragment.this);
            navController.navigate(R.id.action_history_to_summary, bundle);
        });
        recyclerView.setAdapter(adapter);
        return root;
    }
}
