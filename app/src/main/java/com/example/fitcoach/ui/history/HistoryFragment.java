package com.example.fitcoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;

import java.util.Arrays;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = root.findViewById(R.id.history_recycler);

        List<Exercise> mockData = Arrays.asList(
                new Exercise("28 avril 2025", "Course", 32, 3487),
                new Exercise("27 avril 2025", "Marche", 45, 5100),
                new Exercise("27 avril 2025", "Cyclisme", 60, 0)
        );

        HistoryAdapter adapter = new HistoryAdapter(mockData, exercise ->
                Toast.makeText(getContext(), "Exercice: " + exercise.getSport(), Toast.LENGTH_SHORT).show()
        );

        recyclerView.setAdapter(adapter);

        return root;
    }
}
