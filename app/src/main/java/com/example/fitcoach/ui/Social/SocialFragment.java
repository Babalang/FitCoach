package com.example.fitcoach.ui.Social;

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

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;
import com.example.fitcoach.Serveur.ApiService;
import com.example.fitcoach.Serveur.RetrofitClient;
import com.example.fitcoach.Serveur.User;
import com.example.fitcoach.databinding.FragmentSocialBinding;

import java.io.IOException;
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
                        if(user.getAmi1()!=null){
                            Log.d(TAG, "score ami: " + user.getAmi1Score());
                            amis+="Ami : "+user.getAmi1()+". Son score est de : "+user.getAmi1Score()+"\n";
                            if(user.getAmi2()!=null){
                                amis+="Ami : "+user.getAmi2()+". Son score est de : "+user.getAmi2Score()+"\n";
                            }
                            if(user.getAmi3()!=null){
                                amis+="Ami : "+user.getAmi3()+". Son score est de : "+user.getAmi3Score()+"\n";
                            }
                            if(user.getAmi4()!=null){
                                amis+="Ami : "+user.getAmi4()+". Son score est de : "+user.getAmi4Score()+"\n";
                            }
                            if(user.getAmi5()!=null){
                                amis+="Ami : "+user.getAmi5()+". Son score est de : "+user.getAmi5Score()+"\n";
                            }
                        }else{
                            amis="Aucun ami\n";
                        }
                        Textami.setText(amis);
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        AppDataManager appDataManager = AppDataManager.getInstance(getContext());
        int ide = appDataManager.getCompteId();
        AppDataManager.Compte compte = appDataManager.getCompteById(ide);
        id=view.findViewById(R.id.id);
        score=view.findViewById(R.id.score);
        id.setText(compte.getLogin());
        score.setText("0");
        Textami=view.findViewById(R.id.listeAmi);
        boutonAjouteAmi=view.findViewById(R.id.BoutonAjoutAmi);
        texteAjoutAmi=view.findViewById(R.id.TexteAjoutAmi);
        boutonAjouteAmi.setOnClickListener(v->{
            ajouterAmi(compte.getLogin(),texteAjoutAmi.getText().toString());
        });
        fetchUserById(compte.getLogin());
        return view;
    }
}