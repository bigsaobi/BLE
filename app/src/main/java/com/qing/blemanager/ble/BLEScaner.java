package com.qing.blemanager.ble;


import com.qing.blemanager.ble.listener.OnDeviceScanListener;

/**
 * Created by liuqing on 16/6/12.
 */
public interface BLEScaner {
    /**
     * 当前是否正在搜索
     * */
    boolean isScanningDevices();
    /**
     * 开始搜索设备
     * */
    void startScanDevices(OnDeviceScanListener listener);
    /**
     * 停止搜索设备
     * */
    void stopScanDevices();
}
