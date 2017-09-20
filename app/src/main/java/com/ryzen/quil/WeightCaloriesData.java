package com.ryzen.quil;

public class WeightCaloriesData {

    private double weight;
    private int calories;
    private int month, day, year;

    public WeightCaloriesData(double weight, int calories, int month, int day, int year){
        this.weight = weight;
        this.calories = calories;
        this.month = month;
        this.day = day;
        this.year = year;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
