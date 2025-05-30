package com.example.fitcoach.ui.history;
// Classe pour afficher l'historique des exercices dans un fragment
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;

    // Méthode pour créer la vue du fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = root.findViewById(R.id.history_recycler);
        AppDataManager dataManager = AppDataManager.getInstance(requireContext());
        List<Exercise> exerciseList = dataManager.getAllHistorique();
        HistoryAdapter adapter = new HistoryAdapter(exerciseList, exercise -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("exercise", exercise);
            NavController navController = NavHostFragment.findNavController(HistoryFragment.this);
            navController.navigate(R.id.action_history_to_summary, bundle);
        });
        recyclerView.setAdapter(adapter);
        return root;
    }
}
