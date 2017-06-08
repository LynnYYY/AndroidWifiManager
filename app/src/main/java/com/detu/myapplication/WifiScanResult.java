package com.detu.myapplication;

import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;

/**
 * Created by zhangmint on 2017/6/6.
 */

public class WifiScanResult {
    private ScanResult scanResult;
    private SupplicantState supplicantState;

    public WifiScanResult(ScanResult scanResult){
        this.scanResult = scanResult;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setSupplicantState(SupplicantState supplicantState){
        this.supplicantState = supplicantState;
    }

    public SupplicantState getSupplicantState() {
        return supplicantState;
    }
}
