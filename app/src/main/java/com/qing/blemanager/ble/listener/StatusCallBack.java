package com.qing.blemanager.ble.listener;

/**
 * Created by liuqing on 16/6/16.
 */
public interface StatusCallBack {
    void onDisconnect(int code);
    void onConnectSuccess();
    void onConnectting();
}
