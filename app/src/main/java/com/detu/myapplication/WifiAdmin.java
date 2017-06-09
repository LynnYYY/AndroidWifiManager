package com.detu.myapplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 连接到开放网络
     *
     * @param ssid 热点名
     * @return 配置是否成功
     */
    public static boolean connectOpenNetwork(@NonNull String ssid) {
        // 获取networkId
        int networkId = setOpenNetwork(ssid);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WEP网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public static boolean connectWEPNetwork(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWEPNetwork(ssid, password);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WPA2网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public static boolean connectWPA2Network(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWPA2Network(ssid, password);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 添加开放网络配置
     *
     * @param ssid SSID
     * @return NetworkId
     */
    static int setOpenNetwork(@NonNull String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 生成配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 返回NetworkID
            return wifiConfiguration.networkId;
        }
    }

    /**
     * 添加WEP网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    static int setWEPNetwork(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 添加配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.wepKeys[0] = "\"" + password + "\"";
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 添加WPA网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    static int setWPA2Network(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.preSharedKey = "\"" + password + "\"";
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 通过热点名获取热点配置
     *
     * @param ssid 热点名
     * @return 配置信息
     */
    public static WifiConfiguration getConfigFromConfiguredNetworksBySsid(@NonNull String ssid) {
        ssid = addDoubleQuotation(ssid);
        List<WifiConfiguration> existingConfigs = getConfiguredNetworks();
        if (null != existingConfigs) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssid)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    /**
     * 获取WIFI的开关状态
     *
     * @return WIFI的可用状态
     */
    public static boolean isWifiEnabled() {
        return null != wifiManager && wifiManager.isWifiEnabled();
    }

    /**
     * 获取当前正在连接的WIFI信息
     *
     * @return 当前正在连接的WIFI信息
     */
    public static WifiInfo getConnectionInfo() {
        if (null != wifiManager) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }

    /**
     * 扫描附近的WIFI
     */
    public static void startScan() {
        if (null != wifiManager) {
            wifiManager.startScan();
        }
    }

    /**
     * 获取最近扫描的WIFI热点
     *
     * @return WIFI热点列表
     */
    public static List<ScanResult> getScanResults() {
        // 得到扫描结果
        if (null != wifiManager) {
            return wifiManager.getScanResults();
        }
        return null;
    }

    /**
     * 排除重复
     *
     * @param scanResults 带处理的数据
     * @return 去重数据
     */
    public static ArrayList<ScanResult> excludeRepetition(List<ScanResult> scanResults) {
        HashMap<String, ScanResult> hashMap = new HashMap<>();

        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;

            if (TextUtils.isEmpty(ssid)) {
                continue;
            }

            ScanResult tempResult = hashMap.get(ssid);
            if (null == tempResult) {
                hashMap.put(ssid, scanResult);
                continue;
            }

            if (WifiManager.calculateSignalLevel(tempResult.level, 100) < WifiManager.calculateSignalLevel(scanResult.level, 100)) {
                hashMap.put(ssid, scanResult);
            }
        }

        ArrayList<ScanResult> results = new ArrayList<>();
        for (Map.Entry<String, ScanResult> entry : hashMap.entrySet()) {
            results.add(entry.getValue());
        }

        return results;
    }

    /**
     * 获取配置过的WIFI信息
     *
     * @return 配置信息
     */
    private static List<WifiConfiguration> getConfiguredNetworks() {
        if (null != wifiManager) {
            return wifiManager.getConfiguredNetworks();
        }
        return null;
    }

    /**
     * 保持配置
     *
     * @return 保持是否成功
     */
    static boolean saveConfiguration() {
        return null != wifiManager && wifiManager.saveConfiguration();
    }

    /**
     * 连接到网络
     *
     * @param networkId NetworkId
     * @return 连接结果
     */
    static boolean enableNetwork(int networkId) {
        if (null != wifiManager) {
            boolean isDisconnect = wifiManager.disconnect();
            boolean isEnableNetwork = wifiManager.enableNetwork(networkId, true);
            boolean isSave = wifiManager.saveConfiguration();
            boolean isReconnect = wifiManager.reconnect();
            return isDisconnect && isEnableNetwork && isSave && isReconnect;
        }
        return false;
    }

    /**
     * 添加网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private static int addNetwork(WifiConfiguration wifiConfig) {
        if (null != wifiManager) {
            int networkId = wifiManager.addNetwork(wifiConfig);
            if (-1 != networkId) {
                boolean isSave = wifiManager.saveConfiguration();
                if (isSave) {
                    return networkId;
                }
            }
        }
        return -1;
    }

    /**
     * 更新网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private static int updateNetwork(WifiConfiguration wifiConfig) {
        if (null != wifiManager) {
            int networkId = wifiManager.updateNetwork(wifiConfig);
            if (-1 != networkId) {
                boolean isSave = wifiManager.saveConfiguration();
                if (isSave) {
                    return networkId;
                }
            }
        }
        return -1;
    }

    /**
     * 断开WIFI
     *
     * @param netId netId
     * @return 是否断开
     */
    public static boolean disconnectWifi(int netId) {
        if (null != wifiManager) {
            boolean isDisable = wifiManager.disableNetwork(netId);
            boolean isDisconnect = wifiManager.disconnect();
            return isDisable && isDisconnect;
        }
        return false;
    }

    /**
     * 删除配置
     *
     * @param netId netId
     * @return 是否删除成功
     */
    public static boolean deleteConfig(int netId) {
        if (null != wifiManager) {
            boolean isDisable = wifiManager.disableNetwork(netId);
            boolean isRemove = wifiManager.removeNetwork(netId);
            boolean isSave = wifiManager.saveConfiguration();
            return isDisable && isRemove && isSave;
        }
        return false;
    }

    /**
     * 计算WIFI信号强度
     *
     * @param rssi WIFI信号
     * @return 强度
     */
    public static int calculateSignalLevel(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    /**
     * 获取WIFI的加密方式
     *
     * @param scanResult WIFI信息
     * @return 加密方式
     */
    public static SecurityModeEnum getSecurityMode(@NonNull ScanResult scanResult) {
        String capabilities = scanResult.capabilities;

        if (capabilities.toUpperCase().contains("WPA")) {
            return SecurityModeEnum.WPA;
        } else if (capabilities.toUpperCase().contains("WEP")) {
            return SecurityModeEnum.WEP;
            //        } else if (capabilities.contains("EAP")) {
            //            return SecurityMode.WEP;
        } else {
            // 没有加密
            return SecurityModeEnum.OPEN;
        }
    }

    /**
     * 添加双引号
     *
     * @param text 待处理字符串
     * @return 处理后字符串
     */
    public static String addDoubleQuotation(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return "\"" + text + "\"";
    }
}
