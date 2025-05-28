package com.example.fitcoach.Serveur;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // ... (ta méthode POST existante si tu l'as en Java)
    // @POST("api/data")
    // Call<ServerResponse> sendData(@Body MyData data);

    //@GET("api/users") // Endpoint pour obtenir la liste des utilisateurs
    //Call<List<User>> getUsers(); // Attend une liste d'objets User

    @FormUrlEncoded
    @POST("/moi")
    Call<User> getUserById(@Field("nom") String nom);

    @GET("/")
    Call<List<User>> getUsers();

    @FormUrlEncoded
    @POST("/create")
    Call<User> create(@Field("nom") String nom);

    @FormUrlEncoded
    @POST("/setAmi")
    Call<User> nouveauAmi(@Field("nom") String nom, @Field("ami") String ami);
}
