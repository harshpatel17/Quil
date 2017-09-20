package com.ryzen.quil;

/**
 * Created by Ryan on 6/23/2017.
 */

public class ShouldersData {

    private String name;
    private int set;
    private int rep;
    private int weight;
    private int month;
    private int day;
    private int year;

    public ShouldersData(String name, int set, int rep, int weight, int month, int day, int year){
        this.name = name;
        this.set = set;
        this.rep = rep;
        this.weight = weight;
        this.month = month;
        this.day = day;
        this.year = year;;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getRep() {
        return rep;
    }

    public void setRep(int rep) {
        this.rep = rep;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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
