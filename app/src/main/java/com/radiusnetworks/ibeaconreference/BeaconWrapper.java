package com.radiusnetworks.ibeaconreference;

import android.graphics.Color;

import org.altbeacon.beacon.Beacon;

/**
 * Created by nubor on 27/10/2016.
 */
public class BeaconWrapper {
    String beaconType;
    Beacon beacon;
    int beaconColorBack;

    public BeaconWrapper(String beaconType, Beacon beacon,int beaconColorBack) {
        this.beaconType = beaconType;
        this.beacon = beacon;
        this.beaconColorBack=beaconColorBack;
    }

    public int getBeaconColorBack() {
        return beaconColorBack;
    }

    public void setBeaconColorBack(int beaconColorBack) {
        this.beaconColorBack = beaconColorBack;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public String getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(String beaconType) {
        this.beaconType = beaconType;
    }
}
