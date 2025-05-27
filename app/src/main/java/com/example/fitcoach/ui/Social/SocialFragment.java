package com.example.fitcoach.ui.Social;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fitcoach.R;
import com.example.fitcoach.databinding.FragmentSocialBinding;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialFragment extends Fragment {

    private static final String TAG = "SocialFragment";
    private FragmentSocialBinding binding;
    private TextView id;
    private TextView score;


    private void fetchAllUsers() {
        ApiService apiService = RetrofitClient.getInstance();
        Call<List<User>> call = apiService.getUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    List<User> userList = response.body();
                    if (userList != null && !userList.isEmpty()) {
                        Log.d(TAG, "Utilisateurs récupérés: " + userList.size());
                        for (User user : userList) {
                            Log.d(TAG, "User: " + user.getNom());
                            //id.setText(user.getNom());
                            //score.setText(user.getScore());
                        }
                    } else {
                        Log.e(TAG, "Réponse réussie mais corps vide ou liste vide pour getUsers");
                    }
                } else {
                    Log.e(TAG, "Erreur getUsers (code " + response.code() + "): " + response.message());
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
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec de l'appel getUsers: " + t.getMessage(), t);
            }
        });
    }

    private void fetchUserById(int userId) {
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        Log.d(TAG, "Utilisateur récupéré: " + user.toString());
                        // iiiiiiiiiiiiii
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        //id=view.findViewById(R.id.id);
        //score=view.findViewById(R.id.score);
        fetchAllUsers();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}