package com.example.fitcoach.ui.Exercise;

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

    public ExerciseStepAdapter(List<ExerciseStep> stepList) {
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_step, parent, false);
        return new StepViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return stepList.size();
    }

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
