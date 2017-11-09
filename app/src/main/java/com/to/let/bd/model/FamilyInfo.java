package com.to.let.bd.model;

/**
 * Created by MAKINUL on 11/5/17.
 */

public class FamilyInfo {
    private int bedRoom;
    private int bathroom;
    private int balcony;
    private boolean isItDuplexLay;
    private boolean hasDrawingDining;

    private boolean twentyFourWater;
    private boolean gasSupply;
    private boolean securityGuard;
    private boolean parkingGarage;
    private boolean lift;
    private boolean generator;
    private boolean wellFurnished;
    private boolean kitchenCabinet;

    public int getBedRoom() {
        return bedRoom;
    }

    public void setBedRoom(int bedRoom) {
        this.bedRoom = bedRoom;
    }

    public int getBathroom() {
        return bathroom;
    }

    public void setBathroom(int bathroom) {
        this.bathroom = bathroom;
    }

    public int getBalcony() {
        return balcony;
    }

    public void setBalcony(int balcony) {
        this.balcony = balcony;
    }

    public boolean isItDuplexLay() {
        return isItDuplexLay;
    }

    public void setItDuplexLay(boolean itDuplexLay) {
        isItDuplexLay = itDuplexLay;
    }

    public boolean isHasDrawingDining() {
        return hasDrawingDining;
    }

    public void setHasDrawingDining(boolean hasDrawingDining) {
        this.hasDrawingDining = hasDrawingDining;
    }

    public boolean isWellFurnished() {
        return wellFurnished;
    }

    public void setWellFurnished(boolean wellFurnished) {
        this.wellFurnished = wellFurnished;
    }

    public boolean isGasSupply() {
        return gasSupply;
    }

    public void setGasSupply(boolean gasSupply) {
        this.gasSupply = gasSupply;
    }

    public boolean isTwentyFourWater() {
        return twentyFourWater;
    }

    public void setTwentyFourWater(boolean twentyFourWater) {
        this.twentyFourWater = twentyFourWater;
    }

    public boolean isSecurityGuard() {
        return securityGuard;
    }

    public void setSecurityGuard(boolean securityGuard) {
        this.securityGuard = securityGuard;
    }

    public boolean isLift() {
        return lift;
    }

    public void setLift(boolean lift) {
        this.lift = lift;
    }

    public boolean isGenerator() {
        return generator;
    }

    public void setGenerator(boolean generator) {
        this.generator = generator;
    }

    public boolean isParkingGarage() {
        return parkingGarage;
    }

    public void setParkingGarage(boolean parkingGarage) {
        this.parkingGarage = parkingGarage;
    }

    public boolean isKitchenCabinet() {
        return kitchenCabinet;
    }

    public void setKitchenCabinet(boolean kitchenCabinet) {
        this.kitchenCabinet = kitchenCabinet;
    }
}
