package com.example.fitcoach.ui.Social;

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


}
