package com.to.let.bd.model;

import java.io.Serializable;

/**
 * Created by MAKINUL on 11/5/17.
 */

public class MessInfo implements Serializable {
    private int memberType;
    private int numberOfSeat;
    private int numberOfRoom;
    private int totalMember;
    private int messManagementSystem;

    private boolean mealFacility;
    private boolean maidServant;
    private boolean twentyFourWater;
    private boolean nonSmoker;
    private boolean wifi;
    private boolean fridge;

    private int mealRate;

    public int getMemberType() {
        return memberType;
    }

    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }

    public int getNumberOfSeat() {
        return numberOfSeat;
    }

    public void setNumberOfSeat(int numberOfSeat) {
        this.numberOfSeat = numberOfSeat;
    }

    public int getNumberOfRoom() {
        return numberOfRoom;
    }

    public void setNumberOfRoom(int numberOfRoom) {
        this.numberOfRoom = numberOfRoom;
    }

    public int getTotalMember() {
        return totalMember;
    }

    public void setTotalMember(int totalMember) {
        this.totalMember = totalMember;
    }

    public int getMessManagementSystem() {
        return messManagementSystem;
    }

    public void setMessManagementSystem(int messManagementSystem) {
        this.messManagementSystem = messManagementSystem;
    }

    public boolean isMealFacility() {
        return mealFacility;
    }

    public void setMealFacility(boolean mealFacility) {
        this.mealFacility = mealFacility;
    }

    public boolean isMaidServant() {
        return maidServant;
    }

    public void setMaidServant(boolean maidServant) {
        this.maidServant = maidServant;
    }

    public boolean isTwentyFourWater() {
        return twentyFourWater;
    }

    public void setTwentyFourWater(boolean twentyFourWater) {
        this.twentyFourWater = twentyFourWater;
    }

    public boolean isNonSmoker() {
        return nonSmoker;
    }

    public void setNonSmoker(boolean nonSmoker) {
        this.nonSmoker = nonSmoker;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isFridge() {
        return fridge;
    }

    public void setFridge(boolean fridge) {
        this.fridge = fridge;
    }

    public int getMealRate() {
        return mealRate;
    }

    public void setMealRate(int mealRate) {
        this.mealRate = mealRate;
    }
}
