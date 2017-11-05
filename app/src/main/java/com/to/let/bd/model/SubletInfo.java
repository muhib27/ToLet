package com.to.let.bd.model;

/**
 * Created by MAKINUL on 11/5/17.
 */

public class SubletInfo {
    private int subletType;
    private String subletTypeOthers;
    private int bathroomType;
    private int twentyFourWaterFacility;
    private int gasSupply;
    private int kitchenShare;
    private int wellFurnished;
    private int lift;
    private int generator;

    public int getSubletType() {
        return subletType;
    }

    public void setSubletType(int subletType) {
        this.subletType = subletType;
    }

    public String getSubletTypeOthers() {
        return subletTypeOthers;
    }

    public void setSubletTypeOthers(String subletTypeOthers) {
        this.subletTypeOthers = subletTypeOthers;
    }

    public int getBathroomType() {
        return bathroomType;
    }

    public void setBathroomType(int bathroomType) {
        this.bathroomType = bathroomType;
    }

    public int getTwentyFourWaterFacility() {
        return twentyFourWaterFacility;
    }

    public void setTwentyFourWaterFacility(int twentyFourWaterFacility) {
        this.twentyFourWaterFacility = twentyFourWaterFacility;
    }

    public int getGasSupply() {
        return gasSupply;
    }

    public void setGasSupply(int gasSupply) {
        this.gasSupply = gasSupply;
    }

    public int getKitchenShare() {
        return kitchenShare;
    }

    public void setKitchenShare(int kitchenShare) {
        this.kitchenShare = kitchenShare;
    }

    public int getWellFurnished() {
        return wellFurnished;
    }

    public void setWellFurnished(int wellFurnished) {
        this.wellFurnished = wellFurnished;
    }

    public int getLift() {
        return lift;
    }

    public void setLift(int lift) {
        this.lift = lift;
    }

    public int getGenerator() {
        return generator;
    }

    public void setGenerator(int generator) {
        this.generator = generator;
    }
}
