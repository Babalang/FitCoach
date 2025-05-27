package com.example.fitcoach.ui.Social;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Remplace par l'URL de base de ton serveur Flask
    // Pour l'émulateur: "http://10.0.2.2:5000/" (si Flask tourne sur le port 5000)
    // Pour un appareil physique sur le même réseau: "http://TON_ADRESSE_IP_LOCALE:5000/"
    public static String BASE_URL = "http://127.0.0.1:5001/";
    private static Retrofit retrofit;
    public static Retrofit getClient(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
