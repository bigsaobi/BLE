package com.qing.blemanager.ble.listener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by liuqing on 16/6/12.
 */
public abstract class OnDeviceScanListener {
    BluetoothAdapter.LeScanCallback callback;

    public OnDeviceScanListener() {
        this.callback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                onDeviceFind(bluetoothDevice,bytes);
            }
        };
    }

    public BluetoothAdapter.LeScanCallback getCallback(){
        return callback;
    }

    public abstract void onDeviceFind(BluetoothDevice bluetoothDevice, byte[] bytes);

    public abstract void onScanStart();

    public abstract void onScanFinish();
}
