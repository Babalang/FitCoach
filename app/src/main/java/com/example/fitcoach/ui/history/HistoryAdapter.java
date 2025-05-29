package com.example.fitcoach.ui.history;

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

    public HistoryAdapter(List<Exercise> list, OnItemClickListener listener) {
        this.exerciseList = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, sportText, stepsText;

        public ViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.text_date);
            sportText = itemView.findViewById(R.id.text_sport);
            stepsText = itemView.findViewById(R.id.text_steps);
        }

        public void bind(Exercise exercise, OnItemClickListener listener) {
            dateText.setText("📅 " + exercise.getDate());
            sportText.setText("🏃 " + exercise.getSport() + " - " + (exercise.getDuration()/60) + " min " + (exercise.getDuration()%60) + " sec");
            stepsText.setText("👣 " + exercise.getSteps() + " pas");
            itemView.setOnClickListener(v -> listener.onItemClick(exercise));
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exerciseList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
}

