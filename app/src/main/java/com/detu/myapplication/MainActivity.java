package com.detu.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import wisefy.WiseFy;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private final static String TAG = "MainActivity";
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView wifiList;
    private TextView emptyView;
    private WifiListAdapter wifiListAdapter;
    private WifiManager wifiManager;
    private SwitchCompat switchToggleWifi;
    private WiseFy mWiseFy;
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
//            ,
//            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(onRefreshListener);

        switchToggleWifi = (SwitchCompat) findViewById(R.id.switch_toggle);
        switchToggleWifi.setOnCheckedChangeListener(onCheckedChangeListener);

        mWiseFy = new WiseFy.withContext(MainActivity.this).logging(true).getSmarts();

        wifiList = (RecyclerView) findViewById(R.id.wifi_list);
        wifiListAdapter = new WifiListAdapter(getApplicationContext());
        wifiListAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
                new ConnectWifiDialog(MainActivity.this) {
                    @Override
                    public void connect(String password) {
                        ScanResult scanResult = wifiListAdapter.getItem(position).getScanResult();
                        int networkId = -1;
                        switch (WifiAdmin.getSecurityMode(scanResult)){
                            case WPA:
                            case WPA2:
//                                WifiAdmin.connectWPA2Network(scanResult.SSID,password);
                                networkId = mWiseFy.addWPA2Network(scanResult.SSID,password);
                                break;
                            case WEP:
//                                WifiAdmin.connectWEPNetwork(scanResult.SSID,password);
                                networkId = mWiseFy.addWEPNetwork(scanResult.SSID,password);
                                break;
                            case OPEN:
//                                WifiAdmin.connectOpenNetwork(scanResult.SSID);
                                networkId = mWiseFy.addOpenNetwork(scanResult.SSID);
                                break;
                            default:
                                break;
                        }
//                        mWiseFy.connectToNetwork(scanResult.SSID,10000);
                        boolean ret1 = wifiManager.disconnect();
                        boolean ret2 = wifiManager.enableNetwork(networkId, true);
                        boolean ret3 = wifiManager.reconnect();
                        Log.d(TAG,networkId + " - " + ret1 + " - " + ret2 + " - " + ret3);
                    }
                }.setSsid(wifiListAdapter.getItem(position).getScanResult().SSID).show();
            }
        });
        wifiList.setAdapter(wifiListAdapter);
        wifiList.setLayoutManager(new LinearLayoutManager(this));
        wifiList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        emptyView = (TextView) findViewById(R.id.empty_view);


        if(EasyPermissions.hasPermissions(this, PERMISSIONS)){
            refreshList();
        }else{
            EasyPermissions.requestPermissions(this,"需要WiFi权限",1000,PERMISSIONS);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver,intentFilter);
    }


    private void setRefreshing(boolean refreshing){
        if(refreshing){
            swipeLayout.setOnRefreshListener(null);
            swipeLayout.setRefreshing(true);
            swipeLayout.setOnRefreshListener(onRefreshListener);
        }else{
            swipeLayout.setRefreshing(false);
        }
    }
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            wifiManager.startScan();
        }
    };

    private void setChecked(boolean checked){
        switchToggleWifi.setOnCheckedChangeListener(null);
        switchToggleWifi.setChecked(checked);
        switchToggleWifi.setOnCheckedChangeListener(onCheckedChangeListener);
    }
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG,"onCheckedChanged " + isChecked);
            if(isChecked){
                wifiManager.setWifiEnabled(true);
            }else {
                wifiManager.setWifiEnabled(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiBroadcastReceiver);
    }

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"wifiBroadcastReceiver " + intent.getAction());
            switch (intent.getAction()){
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Toast.makeText(MainActivity.this,"刷新列表完成",Toast.LENGTH_SHORT).show();
                    refreshList();
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WIFI_STATE_UNKNOWN)){
                        case WIFI_STATE_DISABLED:
                            Log.d(TAG,"wifi已关闭");
                            switchToggleWifi.setEnabled(true);
                            emptyView.setVisibility(View.VISIBLE);
                            wifiList.setVisibility(View.GONE);
                            setChecked(false);
                            break;
                        case WIFI_STATE_DISABLING:
                            Log.d(TAG,"wifi正在关闭");
                            switchToggleWifi.setEnabled(false);
                            setRefreshing(false);
                            break;
                        case WIFI_STATE_ENABLED:
                            Log.d(TAG,"wifi已打开");
                            switchToggleWifi.setEnabled(true);
                            setChecked(true);
                            emptyView.setVisibility(View.GONE);
                            wifiList.setVisibility(View.VISIBLE);
                            break;
                        case WIFI_STATE_ENABLING:
                            Log.d(TAG,"wifi正在打开");
                            switchToggleWifi.setEnabled(false);
                            setRefreshing(true);
                            break;
                        case WIFI_STATE_UNKNOWN:
                            Log.d(TAG,"wifi状态未知");
                            break;
                        default:
                            break;
                    }
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    // 获取连接状态
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//                    wifiListAdapter.updateSupplicantState(supplicantState);

                    switch (supplicantState) {
                        case INTERFACE_DISABLED: // 接口禁用
                            Log.i(TAG, "onReceive: INTERFACE_DISABLED 接口禁用");
                            break;
                        case DISCONNECTED:// 断开连接
                            Log.i(TAG, "onReceive: DISCONNECTED:// 断开连接");
                            wifiListAdapter.updateSupplicantState(supplicantState);
                            break;
                        case INACTIVE: // 不活跃的
                            WifiInfo connectFailureInfo = wifiManager.getConnectionInfo();
                            Log.i(TAG, "onReceive: INACTIVE 不活跃的  connectFailureInfo = " + connectFailureInfo);
//                            refreshList();

                            break;
                        case SCANNING: // 正在扫描
                            Log.i(TAG, "onReceive: SCANNING 正在扫描");
                            break;
                        case AUTHENTICATING: // 正在验证
                            Log.i(TAG, "onReceive: AUTHENTICATING: // 正在验证");
                            break;
                        case ASSOCIATING: // 正在关联
                            Log.i(TAG, "onReceive: ASSOCIATING: // 正在关联");
                            break;
                        case ASSOCIATED: // 已经关联
                            Log.i(TAG, "onReceive: ASSOCIATED: // 已经关联");
                            break;
                        case FOUR_WAY_HANDSHAKE:
                            Log.i(TAG, "onReceive: FOUR_WAY_HANDSHAKE:");
                            break;
                        case GROUP_HANDSHAKE:
                            Log.i(TAG, "onReceive: GROUP_HANDSHAKE:");
                            break;
                        case COMPLETED: // 完成
                            Log.i(TAG, "onReceive: WIFI_CONNECT_SUCCESS: // 完成");
//                            refreshList();
                            wifiListAdapter.updateSupplicantState(supplicantState);
                            break;
                        case DORMANT:
                            Log.i(TAG, "onReceive: DORMANT:");
                            break;
                        case UNINITIALIZED: // 未初始化
                            Log.i(TAG, "onReceive: UNINITIALIZED: // 未初始化");
                            break;
                        case INVALID: // 无效的
                            Log.i(TAG, "onReceive: INVALID: // 无效的");
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        refreshList();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this,"需要访问WiFi的权限",Toast.LENGTH_SHORT).show();
    }

    private void refreshList(){
        List<ScanResult> scanResults = wifiManager.getScanResults();
        List<WifiScanResult> wifiScanResults = new ArrayList<>();
        for(ScanResult scanResult:scanResults){
            WifiScanResult wifiScanResult = new WifiScanResult(scanResult);
            wifiScanResults.add(wifiScanResult);
        }
        wifiListAdapter.refreshData(wifiScanResults);
        setRefreshing(false);
    }
}
