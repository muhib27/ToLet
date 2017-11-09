package com.to.let.bd.model;

/**
 * Created by MAKINUL on 11/5/17.
 */

public class SubletInfo {
    private int subletType;
    private String subletTypeOthers;
    private int bathroomType;
    private boolean twentyFourWater;
    private boolean gasSupply;
    private boolean kitchenShare;
    private boolean wellFurnished;
    private boolean lift;
    private boolean generator;

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

    public boolean isTwentyFourWater() {
        return twentyFourWater;
    }

    public void setTwentyFourWater(boolean twentyFourWater) {
        this.twentyFourWater = twentyFourWater;
    }

    public boolean isGasSupply() {
        return gasSupply;
    }

    public void setGasSupply(boolean gasSupply) {
        this.gasSupply = gasSupply;
    }

    public boolean isKitchenShare() {
        return kitchenShare;
    }

    public void setKitchenShare(boolean kitchenShare) {
        this.kitchenShare = kitchenShare;
    }

    public boolean isWellFurnished() {
        return wellFurnished;
    }

    public void setWellFurnished(boolean wellFurnished) {
        this.wellFurnished = wellFurnished;
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
}
