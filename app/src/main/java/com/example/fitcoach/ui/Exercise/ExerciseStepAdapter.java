package com.example.fitcoach.ui.Exercise;
// Classe pour modéliser une étape d'exercice
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.R;

import java.util.List;

public class ExerciseStepAdapter extends RecyclerView.Adapter<ExerciseStepAdapter.StepViewHolder> {
    private final List<ExerciseStep> stepList;

    // Constructeur pour initialiser l'adaptateur avec une liste d'étapes d'exercice
    public ExerciseStepAdapter(List<ExerciseStep> stepList) {
        this.stepList = stepList;
    }

    // Méthode pour créer une nouvelle vue de holder pour chaque étape d'exercice
    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_step, parent, false);
        return new StepViewHolder(view);
    }

    // Méthode pour lier les données d'une étape d'exercice à la vue du holder
    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        ExerciseStep step = stepList.get(position);
        holder.name.setText(step.getName());
        holder.duration.setText(String.valueOf(step.getDuration()));

        holder.delete.setOnClickListener(v -> {
            stepList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });

        holder.name.addTextChangedListener(new SimpleTextWatcher(text -> step.setName(text)));
        holder.duration.addTextChangedListener(new SimpleTextWatcher(text -> {
            step.setDuration(text.isEmpty() ? 0 : Integer.parseInt(text));
        }));
    }

    // Méthode pour obtenir le nombre total d'étapes d'exercice dans la liste
    @Override
    public int getItemCount() {
        return stepList.size();
    }

    // Classe interne pour représenter le holder d'une étape d'exercice
    static class StepViewHolder extends RecyclerView.ViewHolder {
        EditText name, duration;
        ImageButton delete;

        StepViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.step_name);
            duration = itemView.findViewById(R.id.step_duration);
            delete = itemView.findViewById(R.id.delete_step);
        }
    }
}
