package com.radiusnetworks.ibeaconreference;

import android.content.Context;

import org.altbeacon.beacon.logging.LogManager;

/**
 * Created by nubor on 27/10/2016.
 */
public class BeaconType {
    static final String IBEACON = "IBEACON";
    static final String EDDY_STONE_TLM = "EDS_TLM";
    static final String EDDY_STONE_URL = "EDS_URL";
    static final String EDDY_STONE_UID = "EDS_UID";

    static final int IBEACON_HEX = 0x215; //533decimal
    static final int EDDY_STONE_TLM_HEX = 0x20; //32decimal
    static final int EDDY_STONE_URL_HEX = 0x10; //16decimal
    static final int EDDY_STONE_UID_HEX = 0x00; //0decimal

    public static String getBeaconType(int typeBeacon) {
        String returnBeaconType = "";
        switch (typeBeacon) {
            case EDDY_STONE_UID_HEX:
                returnBeaconType = EDDY_STONE_UID;
                break;
            case EDDY_STONE_URL_HEX:
                returnBeaconType = EDDY_STONE_URL;
                break;
            case EDDY_STONE_TLM_HEX:
                returnBeaconType = EDDY_STONE_TLM;
                break;
            default:
                returnBeaconType = IBEACON;
        }

        return returnBeaconType;

    }

    public static int getBckColorByBeaconType(Context mContext, int typeBeacon) {
        int color = 0;
        switch (typeBeacon) {
            case IBEACON_HEX:
                color = mContext.getResources().getColor(R.color.ibeacon);
                break;
            case EDDY_STONE_UID_HEX:
                color = mContext.getResources().getColor(R.color.uid);
                break;
            case EDDY_STONE_URL_HEX:
                color = mContext.getResources().getColor(R.color.url);
                break;
            case EDDY_STONE_TLM_HEX:
                color = mContext.getResources().getColor(R.color.tlm);
                break;
        }

        return color;
    }

    static final double edd_coefficient1 = 0.42093;
    static final double edd_coefficient2 = 6.9476;
    static final double edd_coefficient3 = 0.54992;

    public static double calculateDistanceWithRefRSSI0(int meassureRssiAt0M, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        meassureRssiAt0M = meassureRssiAt0M - 25; //round to refrrsi 4 1 metro

        double ratio = rssi * 1.0 / meassureRssiAt0M;
        double distance;
        if (ratio < 1.0) {
            distance = Math.pow(ratio, 10);
        } else {
            distance = (edd_coefficient1) * Math.pow(ratio, edd_coefficient2) + edd_coefficient3;
        }
        return distance;
    }

}
