package com.example.fitcoach.ui.Social;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Remplace par l'URL de base de ton serveur Flask
    // Pour l'émulateur: "http://10.0.2.2:5000/" (si Flask tourne sur le port 5000)
    // Pour un appareil physique sur le même réseau: "http://TON_ADRESSE_IP_LOCALE:5000/"
    private static final String BASE_URL = "http://10.0.2.2:5001/"; // Adapte ceci

    private static Retrofit retrofit = null;

    public static ApiService getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Pour voir les détails

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor) // Optionnel
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Optionnel
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
