// AmiAdapter.java
package com.example.fitcoach.ui.Social;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitcoach.R;
import java.util.List;

public class AmiAdapter extends RecyclerView.Adapter<AmiAdapter.AmiViewHolder> {
    private final List<Ami> amis;

    public AmiAdapter(List<Ami> amis) {
        this.amis = amis;
    }

    @NonNull
    @Override
    public AmiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ami, parent, false);
        return new AmiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmiViewHolder holder, int position) {
        Ami ami = amis.get(position);
        holder.nom.setText(ami.nom);
        holder.score.setText(ami.score);
    }

    @Override
    public int getItemCount() {
        return amis.size();
    }

    static class AmiViewHolder extends RecyclerView.ViewHolder {
        TextView nom, score;
        AmiViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.amiNom);
            score = itemView.findViewById(R.id.amiScore);
        }
    }

    public static class Ami {
        public String nom;
        public String score;
        public Ami(String nom, String score) {
            this.nom = nom;
            this.score = score;
        }
    }
}