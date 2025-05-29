package com.example.fitcoach.ui.history;


public class Exercise implements  java.io.Serializable {
    private String date;
    private String sport;
    private long duration;
    private int steps;
    private float calories;
    private float distance;
    private float speed;
    private int repetition;

    public Exercise(String date, String sport, long duration, int steps, float calories, float distance, float speed, int repetition) {
        this.date = date;
        this.sport = sport;
        this.duration = duration;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.speed = speed;
        this.repetition = repetition;
    }

    public String getDate() { return date; }
    public String getSport() { return sport; }
    public long getDuration() { return duration; }
    public int getSteps() { return steps; }
    public float getCalories() { return calories; }
    public float getDistance() { return distance; }
    public float getSpeed() { return speed; }
    public int getRepetition() { return repetition; }
    public void setDate(String date) { this.date = date; }
    public void setSport(String sport) { this.sport = sport; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setSteps(int steps) { this.steps = steps; }
    public void setCalories(float calories) { this.calories = calories; }
    public void setDistance(float distance) { this.distance = distance; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setRepetition(int repetition) { this.repetition = repetition; }

}
