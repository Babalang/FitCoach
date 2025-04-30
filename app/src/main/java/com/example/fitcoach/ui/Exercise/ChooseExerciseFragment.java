package com.example.fitcoach.ui.Exercise;

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
import com.google.android.gms.location.LocationServices;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ChooseExerciseFragment extends Fragment {
    private MapView map;

    private long coords[];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_choose_exercise, container, false);
        setHasOptionsMenu(true);
        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        // Point par défaut
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522); // Paris
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);
        map.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                GeoPoint point = (GeoPoint) map.getProjection().fromPixels((int) event.getX(), (int) event.getY());

                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle("Destination choisie");
                map.getOverlays().clear();
                map.getOverlays().add(marker);
                map.invalidate();

                coords = new long[2];
                coords[0] = (long) point.getLatitudeE6();
                coords[1] = (long) point.getLongitudeE6();
            }
            return false;
        });
        Spinner spinner = view.findViewById(R.id.sport_spinner);
        Button btn1 = view.findViewById(R.id.start_exercise_button);
        btn1.setOnClickListener(v -> {
            // Créer un Bundle pour passer les coordonnées au fragment suivant
            Bundle bundle = new Bundle();
            bundle.putLongArray("coords", coords);  // Passer les coordonnées comme long[]
            String sport = spinner.getSelectedItem().toString();
            bundle.putString("sport", sport);  // Passer le sport comme String

            // Naviguer vers le fragment suivant avec les coordonnées
            NavController navController = NavHostFragment.findNavController(ChooseExerciseFragment.this);
            navController.navigate(R.id.choose_to_inexercise, bundle);
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Action de retour personnalisée (ou juste revenir)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.choose_to_exercise);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
