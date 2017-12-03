package com.to.let.bd.model;

import java.io.Serializable;

/**
 * Created by MAKINUL on 11/5/17.
 */

public class OthersInfo implements Serializable {
    private String rentType;
    private boolean lift;
    private boolean generator;
    private boolean securityGuard;
    private boolean parkingGarage;
    private boolean fullyDecorated;
    private boolean wellFurnished;

    public String getRentType() {
        return rentType;
    }

    public void setRentType(String rentType) {
        this.rentType = rentType;
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

    public boolean isSecurityGuard() {
        return securityGuard;
    }

    public void setSecurityGuard(boolean securityGuard) {
        this.securityGuard = securityGuard;
    }

    public boolean isParkingGarage() {
        return parkingGarage;
    }

    public void setParkingGarage(boolean parkingGarage) {
        this.parkingGarage = parkingGarage;
    }

    public boolean isFullyDecorated() {
        return fullyDecorated;
    }

    public void setFullyDecorated(boolean fullyDecorated) {
        this.fullyDecorated = fullyDecorated;
    }

    public boolean isWellFurnished() {
        return wellFurnished;
    }

    public void setWellFurnished(boolean wellFurnished) {
        this.wellFurnished = wellFurnished;
    }
}
