package com.example.fitcoach.ui.Social;
// Fragment pour afficher et gérer les amis dans l'application FitCoach
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.Serveur.ApiService;
import com.example.fitcoach.Serveur.RetrofitClient;
import com.example.fitcoach.Serveur.User;
import com.example.fitcoach.databinding.FragmentSocialBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialFragment extends Fragment {
    private static final String TAG = "SocialFragment";
    private TextView id;
    private TextView score;
    private TextView Textami;
    private String scoreAmi;
    private Button boutonAjouteAmi;
    private EditText texteAjoutAmi;
    private AppDataManager appDataManager;
    private Integer MyScore = 0;
    private RecyclerView recyclerViewAmis;
    private AmiAdapter amiAdapter;
    private List<AmiAdapter.Ami> listeAmis = new ArrayList<>();

    // Méthode pour récupérer les informations de l'utilisateur par son ID
    private void fetchUserById(String userId) {
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        Log.d(TAG, "Utilisateur récupéré: " + user.toString());
                        id.setText(user.getNom());
                        score.setText(user.getScore());
                        String amis="";
                        listeAmis.clear();
                        if(user.getAmi1()!=null){
                            listeAmis.add(new AmiAdapter.Ami(user.getAmi1(), user.getAmi1Score()));
                            if(user.getAmi2()!=null) listeAmis.add(new AmiAdapter.Ami(user.getAmi2(), user.getAmi2Score()));
                            if(user.getAmi3()!=null) listeAmis.add(new AmiAdapter.Ami(user.getAmi3(), user.getAmi3Score()));
                            if(user.getAmi4()!=null) listeAmis.add(new AmiAdapter.Ami(user.getAmi4(), user.getAmi4Score()));
                            if(user.getAmi5()!=null) listeAmis.add(new AmiAdapter.Ami(user.getAmi5(), user.getAmi5Score()));
                        }
                        amiAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Réponse réussie mais corps vide pour getUserById " + userId);
                    }
                } else {
                    Log.e(TAG, "Erreur getUserById " + userId + " (code " + response.code() + "): " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Corps de l'erreur: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Erreur lors de la lecture du corps de l'erreur", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec de l'appel getUserById " + userId + ": " + t.getMessage(), t);
            }
        });
    }

    // Méthode pour ajouter un nouvel ami à la liste des amis de l'utilisateur
    private String ajouterAmi(String nom,String ami){
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.nouveauAmi(nom,ami);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "ajout ami : "+ami);
                } else {
                    Log.e(TAG, "Nom non valide");
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Nom non valide");
            }
        });
        return scoreAmi;
    }

    // Méthode pour créer la vue du fragment et initialiser les éléments de l'interface utilisateur
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        appDataManager = AppDataManager.getInstance(getContext());
        int ide = appDataManager.getCompteId();
        AppDataManager.Compte compte = appDataManager.getCompteById(ide);
        id=view.findViewById(R.id.id);
        score=view.findViewById(R.id.score);
        id.setText(compte.getLogin());
        MyScore = appDataManager.getAllCalories();
        score.setText(String.valueOf(MyScore) + " kcal");
        recyclerViewAmis = view.findViewById(R.id.recyclerViewAmis);
        recyclerViewAmis.setLayoutManager(new LinearLayoutManager(getContext()));
        amiAdapter = new AmiAdapter(listeAmis);
        recyclerViewAmis.setAdapter(amiAdapter);
        boutonAjouteAmi=view.findViewById(R.id.BoutonAjoutAmi);
        texteAjoutAmi=view.findViewById(R.id.TexteAjoutAmi);
        boutonAjouteAmi.setOnClickListener(v->{
            ajouterAmi(compte.getLogin(),texteAjoutAmi.getText().toString());
        });
        fetchUserById(compte.getLogin());
        return view;
    }
}