package com.example.fitcoach.ui.history;
// Classe adapter pour afficher l'historique des exercices dans un RecyclerView
import static androidx.core.content.ContextCompat.getString;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;
import com.example.fitcoach.ui.history.Exercise;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<Exercise> exerciseList;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

    // Constructeur pour initialiser l'adaptateur avec une liste d'exercices et un listener pour les clics
    public HistoryAdapter(List<Exercise> list, OnItemClickListener listener) {
        this.exerciseList = list;
        this.listener = listener;
    }

    // Classe interne pour représenter le holder d'un élément de l'historique
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, sportText, stepsText;

        // Constructeur pour initialiser les vues de l'élément de l'historique
        public ViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.text_date);
            sportText = itemView.findViewById(R.id.text_sport);
            stepsText = itemView.findViewById(R.id.text_steps);
        }

        // Méthode pour lier les données d'un exercice aux vues de l'élément de l'historique
        public void bind(Exercise exercise, OnItemClickListener listener) {
            dateText.setText("📅 " + exercise.getDate());
            sportText.setText("🏃 " + exercise.getSport() + " - " + (exercise.getDuration()/60) + " min " + (exercise.getDuration()%60) + " sec");
            stepsText.setText("👣 " + exercise.getSteps() + " " + itemView.getContext().getString(R.string.General_pas));
            itemView.setOnClickListener(v -> listener.onItemClick(exercise));
        }
    }

    // Méthode pour créer une nouvelle vue de holder pour chaque élément de l'historique
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    // Méthode pour lier les données d'un exercice à la vue du holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exerciseList.get(position), listener);
    }

    // Méthode pour obtenir le nombre total d'exercices dans la liste
    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
}

