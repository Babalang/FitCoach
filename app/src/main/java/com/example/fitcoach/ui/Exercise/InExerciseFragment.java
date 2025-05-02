package com.example.fitcoach.ui.Exercise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.utils.Timer;

public class InExerciseFragment extends Fragment {
    private Timer timer;
    private AppDataManager appDataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            long[] coords = args.getLongArray("coords");
            if (coords != null) {
                double latitude = coords[0] / 1E6;  // Convertir en double
                double longitude = coords[1] / 1E6;  // Convertir en double

                // Utiliser ces coordonnées comme tu veux dans ce fragment
                // Par exemple, afficher une notification avec la destination choisie :
                Toast.makeText(getContext(), "Sport : "+ args.getString("sport")+" Destination: Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_SHORT).show();
            }
        }
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_in_exercise, container, false);
        timer = view.findViewById(R.id.time_value);
        timer.start();
        view.findViewById(R.id.stop_button).setOnClickListener(v -> {
            timer.stop();
            Toast.makeText(getContext(), "L'exercice est terminé : " + timer.getElapsedTime(), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.inexercise_to_execise);
        });
        appDataManager= AppDataManager.getInstance();
        TextView textPas = view.findViewById(R.id.steps_value);
        textPas.setText(String.valueOf(appDataManager.getSteps(0)));
        Button pause = view.findViewById(R.id.pause_button);
        pause.setOnClickListener(v -> {
            if(!timer.isRunning()){
                timer.start();
                pause.setText("Pause");
                Toast.makeText(getContext(), "L'exercice reprend", Toast.LENGTH_SHORT).show();
            } else {
                timer.stop();
                pause.setText("Reprendre");
                Toast.makeText(getContext(), "L'exercice est en pause : " + timer.getElapsedTime(), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Action de retour personnalisée (ou juste revenir)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.inexercise_to_execise);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
