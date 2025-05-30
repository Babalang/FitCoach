package com.example.fitcoach.Serveur;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("nom")
    private String nom;

    @SerializedName("score")
    private String score;

    @SerializedName("ami1")
    private String ami1;

    @SerializedName("ami2")
    private String ami2;

    @SerializedName("ami3")
    private String ami3;

    @SerializedName("ami4")
    private String ami4;

    @SerializedName("ami5")
    private String ami5;

    @SerializedName("ami1Score")
    private String ami1Score;

    @SerializedName("ami2Score")
    private String ami2Score;

    @SerializedName("ami3Score")
    private String ami3Score;

    @SerializedName("ami4Score")
    private String ami4Score;

    @SerializedName("ami5Score")
    private String ami5Score;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public User(String nom, String score) {
        this.nom = nom;
        this.score = score;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getAmi1() {
        return ami1;
    }

    public void setAmi1(String ami1) {
        this.ami1 = ami1;
    }

    public String getAmi2() {
        return ami2;
    }

    public void setAmi2(String ami2) {
        this.ami2 = ami2;
    }

    public String getAmi3() {
        return ami3;
    }

    public void setAmi3(String ami3) {
        this.ami3 = ami3;
    }

    public String getAmi4() {
        return ami4;
    }

    public void setAmi4(String ami4) {
        this.ami4 = ami4;
    }

    public String getAmi5() {
        return ami5;
    }

    public void setAmi5(String ami5) {
        this.ami5 = ami5;
    }

    public String getAmi1Score() {
        return ami1Score;
    }

    public void setAmi1Score(String ami1Score) {
        this.ami1Score = ami1Score;
    }

    public String getAmi2Score() {
        return ami2Score;
    }

    public void setAmi2Score(String ami2Score) {
        this.ami2Score = ami2Score;
    }

    public String getAmi3Score() {
        return ami3Score;
    }

    public void setAmi3Score(String ami3Score) {
        this.ami3Score = ami3Score;
    }

    public String getAmi4Score() {
        return ami4Score;
    }

    public void setAmi4Score(String ami4Score) {
        this.ami4Score = ami4Score;
    }

    public String getAmi5Score() {
        return ami5Score;
    }

    public void setAmi5Score(String ami5Score) {
        this.ami5Score = ami5Score;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;

    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
