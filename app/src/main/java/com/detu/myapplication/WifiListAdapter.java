package com.detu.myapplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangmint on 2017/6/5.
 */

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiListViewHolder> {
    private final static String TAG = WifiListAdapter.class.getSimpleName();
    private WeakReference<Context> contextWeakReference;
    private WifiManager wifiManager;
    private final List<WifiScanResult> wifiScanResults = new ArrayList<>();
    private AdapterView.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public WifiListAdapter(Context context){
        contextWeakReference = new WeakReference(context);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void refreshData(List<WifiScanResult> scanResults) {
        // 清空数据
        wifiScanResults.clear();
        wifiScanResults.addAll(scanResults);

        for(WifiScanResult wifiScanResult:wifiScanResults){
            if(WifiAdmin.getConnectingSsid().equals(wifiScanResult.getScanResult().SSID)){
                wifiScanResult.setSupplicantState(wifiManager.getConnectionInfo().getSupplicantState());
                break;
            }
        }

//        List<WifiScanResult> configuratedList = new ArrayList<>();
//        //将已保存的网络放到列表的前面
//        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
//        if(wifiConfigurations != null){
//            for(WifiScanResult wifiScanResult:scanResults){
//                wifiScanResults.add(wifiScanResult);
//                String configurationSsid;
//                for(WifiConfiguration wifiConfiguration:wifiConfigurations) {
//                    configurationSsid = subSYSSSIDStartAndEndDot(wifiConfiguration.SSID);
//                    if(wifiScanResult.getScanResult().SSID.equals(configurationSsid)){
//                        wifiScanResults.remove(wifiScanResult);
//                        configuratedList.add(0,wifiScanResult);
//                        break;
//                    }
//                }
//            }
//        }
//        wifiScanResults.addAll(0,configuratedList);

        // 更新显示
        notifyDataSetChanged();
    }

    public void updateSupplicantState(SupplicantState supplicantState){
        if(wifiScanResults.isEmpty()) return;
        WifiScanResult wifiScanResult;
        String connectingSsid = WifiAdmin.getConnectingSsid();
        for(int i = 0;i<wifiScanResults.size();i++){
            wifiScanResult = wifiScanResults.get(i);
            Log.d(TAG,connectingSsid + "---" + wifiScanResult.getScanResult().SSID);
            if(connectingSsid.equals(wifiScanResult.getScanResult().SSID)){
                wifiScanResult.setSupplicantState(supplicantState);
                notifyItemChanged(i,wifiScanResult);
            }else {
                wifiScanResult.setSupplicantState(null);
            }
        }
    }

    @Override
    public WifiListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.wifi_list_item,parent,false);
        WifiListViewHolder wifiListViewHolder = new WifiListViewHolder(itemView);
        return wifiListViewHolder;
    }

    @Override
    public void onBindViewHolder(WifiListViewHolder holder, int position) {
        WifiScanResult wifiScanResult = wifiScanResults.get(position);
        String ssid = wifiScanResult.getScanResult().SSID;
        String capabilities = wifiScanResult.getScanResult().capabilities;
        holder.tvName.setText(ssid);
        if(!TextUtils.isEmpty(capabilities)){
            if(capabilities.contains("WPA2-PSK")){
                capabilities = "WPA2-PSK";
            }else if(capabilities.contains("WPA-PSK")){
                capabilities = "WPA-PSK";
            }else if(capabilities.contains("WEP")){
                capabilities = "WEP";
            }else if(capabilities.contains("EAP")){
                capabilities = "EAP";
            }else{
                capabilities = "未加密";
            }
        }else{
            capabilities = "未知";
        }

        SupplicantState supplicantState = wifiScanResult.getSupplicantState();
        if(null != supplicantState){
            switch (supplicantState) {
                case INTERFACE_DISABLED: // 接口禁用
                    capabilities = "接口禁用";
                    break;
                case DISCONNECTED:// 断开连接
                    capabilities = "断开连接";
                    break;
                case INACTIVE: // 不活跃的
                    capabilities = "不活跃的";
                    break;
                case SCANNING: // 正在扫描
                    capabilities = "正在扫描";
                    break;
                case AUTHENTICATING: // 正在验证
                    capabilities = "正在验证";
                    break;
                case ASSOCIATING: // 正在关联
                    capabilities = "正在关联";
                    break;
                case ASSOCIATED: // 已经关联
                    capabilities = "已经关联";
                    break;
                case FOUR_WAY_HANDSHAKE:
                    break;
                case GROUP_HANDSHAKE:
                    break;
                case COMPLETED: // 完成
                    capabilities = "已连接";
                    break;
                case DORMANT:
                    break;
                case UNINITIALIZED: // 未初始化
                    break;
                case INVALID: // 无效的
                    capabilities = "无效的网络";
                    break;
                default:
                    break;
            }
        }
        holder.tvState.setText(capabilities);
    }

    public WifiScanResult getItem(int position) {
        return wifiScanResults.get(position);
    }

    @Override
    public int getItemCount() {
        return wifiScanResults.size();
    }

    public final class WifiListViewHolder extends RecyclerView.ViewHolder{
        public final TextView tvName;
        public final TextView tvState;

        public WifiListViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvState = (TextView) itemView.findViewById(R.id.tv_state);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        //passing the clicked position to the parent class
                        onItemClickListener.onItemClick(null, v, getAdapterPosition(), v.getId());
                    }
                }
            });
        }
    }
}
