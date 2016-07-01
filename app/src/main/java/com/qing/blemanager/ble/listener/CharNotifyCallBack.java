package com.qing.blemanager.ble.listener;

import android.bluetooth.BluetoothGattDescriptor;

/**
 * Created by liuqing on 16/6/16.
 */
public interface CharNotifyCallBack {
    void onStart();
    void onFail();
    void onWriteDescriptorSuccess(BluetoothGattDescriptor descriptor);
    void onWriteDescriptorFail(BluetoothGattDescriptor descriptor);
}
