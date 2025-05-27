package com.example.fitcoach.ui.Social;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // ... (ta méthode POST existante si tu l'as en Java)
    // @POST("api/data")
    // Call<ServerResponse> sendData(@Body MyData data);

    //@GET("api/users") // Endpoint pour obtenir la liste des utilisateurs
    //Call<List<User>> getUsers(); // Attend une liste d'objets User

    @GET("api/user/{id}") // Endpoint pour obtenir un utilisateur spécifique par son ID
    Call<User> getUserById(@Path("id") int userId); // Attend un seul objet User

    @GET("/")
    Call<List<User>> getUsers();
}
