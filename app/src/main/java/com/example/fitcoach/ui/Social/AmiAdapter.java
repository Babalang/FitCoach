// AmiAdapter.java
package com.example.fitcoach.ui.Social;
// Adapter pour afficher une liste d'amis dans un RecyclerView
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

    // Constructeur pour initialiser l'adaptateur avec une liste d'amis
    public AmiAdapter(List<Ami> amis) {
        this.amis = amis;
    }

    // Méthode pour créer une nouvelle vue de holder pour chaque ami
    @NonNull
    @Override
    public AmiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ami, parent, false);
        return new AmiViewHolder(view);
    }

    // Méthode pour lier les données d'un ami à la vue du holder
    @Override
    public void onBindViewHolder(@NonNull AmiViewHolder holder, int position) {
        Ami ami = amis.get(position);
        holder.nom.setText(ami.nom);
        holder.score.setText(ami.score);
    }

    // Méthode pour obtenir le nombre total d'amis dans la liste
    @Override
    public int getItemCount() {
        return amis.size();
    }

    // Classe interne pour représenter le holder d'un ami
    static class AmiViewHolder extends RecyclerView.ViewHolder {
        TextView nom, score;

        // Constructeur pour initialiser les vues de l'élément d'un ami
        AmiViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.amiNom);
            score = itemView.findViewById(R.id.amiScore);
        }
    }

    // Classe interne pour représenter un ami avec son nom et son score
    public static class Ami {
        public String nom;
        public String score;

        // Constructeur pour initialiser un ami avec son nom et son score
        public Ami(String nom, String score) {
            this.nom = nom;
            this.score = score;
        }
    }
}