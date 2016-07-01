package com.qing.blemanager.ble.listener;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by liuqing on 16/6/16.
 */
public interface OrderCallBack {
    void onStart();
    void onFail();
    void onWrite(int status, BluetoothGattCharacteristic characteristic);
    void onChange(BluetoothGattCharacteristic characteristic);
}
