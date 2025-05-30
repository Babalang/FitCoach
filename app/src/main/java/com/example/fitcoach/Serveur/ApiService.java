package com.example.fitcoach.Serveur;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

// Communication avec le serveur
public interface ApiService {
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

    @FormUrlEncoded
    @POST("/setCoordonnee")
    Call<User> setCoordonnee(@Field("nom") String nom, @Field("latitude") double la, @Field("longitude") double lo);
}
