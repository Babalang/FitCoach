package com.example.fitcoach.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.R;
import com.example.fitcoach.ui.Exercise.ChooseExerciseFragment;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class loginFragment extends Fragment {
    private long coords[];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_infos, container, false);
        setHasOptionsMenu(true);
        Button btn1 = view.findViewById(R.id.save_button);
        btn1.setOnClickListener(v -> {
            // Naviguer vers le fragment suivant avec les coordonnées
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.infos_to_home);
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Action de retour personnalisée (ou juste revenir)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.infos_to_home);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
