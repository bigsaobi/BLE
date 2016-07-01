package com.qing.blemanager.ble.listener;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by liuqing on 16/6/16.
 */
public interface ConnectCallBack {
    void onConnectSuccess();
    void onConnectting();
    void onConnectFail();
    void onFindServices(List<BluetoothGattService> services);
    void onFindServicesError();
}
