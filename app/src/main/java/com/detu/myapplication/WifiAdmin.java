package com.detu.myapplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by zhangmint on 2017/6/7.
 */

public class WifiAdmin {
    private static Context context;
    private static WifiManager wifiManager;

    public static void init(Context context){
        WifiAdmin.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 获取当前正在连接的ssid
     * @return
     */
    public static String getConnectingSsid(){
        String ssid = "";
        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiConfigurations != null){
            for(WifiConfiguration wifiConfiguration:wifiConfigurations){
                if (wifiConfiguration.networkId == wifiInfo.getNetworkId()){
                    ssid = wifiInfo.getSSID();
                }
            }
        }
        return subSYSSSIDStartAndEndDot(ssid);
    }

    /**
     * 去掉ssid起始和末尾的"
     * @param ssid
     * @return
     */
    public static String subSYSSSIDStartAndEndDot(String ssid) {
        if (TextUtils.isEmpty(ssid) || ssid.length() < 1 || !ssid.contains("\"")) {
            return ssid;
        }
        if (ssid.substring(0, 1).equals("\"")) {
            ssid = ssid.substring(1, ssid.length());
        }
        if (ssid.subSequence(ssid.length() - 1, ssid.length()).equals("\"")) {
            ssid = ssid.substring(0, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * 是否为已配置过的网络
     * @param scanResult
     * @return
     */
    public static boolean isConfigured(ScanResult scanResult){
        boolean ret = false;
        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        if(wifiConfigurations != null) {
            String configurationSsid;
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                configurationSsid = subSYSSSIDStartAndEndDot(wifiConfiguration.SSID);
                if (scanResult.SSID.equals(configurationSsid)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 连接wifi
     * @param scanResult
     * @param password
     */
    public static void connectWiFi(ScanResult scanResult,String password) {
        try {
            Log.v("rht", "Item clicked, SSID " + scanResult.SSID + " Security : " + scanResult.capabilities);
            String networkSSID = scanResult.SSID;
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;
            if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                Log.v("rht", "Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (password.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = password;
                } else {
                    conf.wepKeys[0] = "\"".concat(password).concat("\"");
                }
                conf.wepTxKeyIndex = 0;
            } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                Log.v("rht", "Configuring WPA");
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.preSharedKey = "\"" + password + "\"";
            } else {
                Log.v("rht", "Configuring OPEN network");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }

            int networkId = wifiManager.addNetwork(conf);
            Log.v("rht", "Add result " + networkId);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    Log.v("rht", "WifiConfiguration SSID " + i.SSID);
                    boolean isDisconnected = wifiManager.disconnect();
                    Log.v("rht", "isDisconnected : " + isDisconnected);
                    boolean isEnabled = wifiManager.enableNetwork(i.networkId, true);
                    Log.v("rht", "isEnabled : " + isEnabled);
                    boolean isReconnected = wifiManager.reconnect();
                    Log.v("rht", "isReconnected : " + isReconnected);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Capabilities{
        WEP,WPA,OPEN
    }
}
