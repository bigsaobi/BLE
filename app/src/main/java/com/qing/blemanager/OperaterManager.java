package com.qing.blemanager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.qing.blemanager.ble.BLEManager;
import com.qing.blemanager.ble.listener.BoxNotifyCallBack;
import com.qing.blemanager.ble.listener.StatusCallBack;
import com.qing.blemanager.command.ConnectCommand;
import com.qing.blemanager.command.DingBoxBaseCommand;
import com.qing.blemanager.command.GetBoxInfoOrderCommand;
import com.qing.blemanager.command.ScanDevicesCommand;

/**
 * Created by liuqing on 16/6/15.
 */
public class OperaterManager {
    private static final String TAG = OperaterManager.class.getSimpleName();
    Context ctx;

    private static OperaterManager instance;
    private DingBoxStatusListener dingBoxStatusListener;

    private OperaterManager(Context ctx) {
        this.ctx = ctx;
        setReceivedNotifyListener();
        setConnectStatusListener();
    }

    public static OperaterManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new OperaterManager(ctx.getApplicationContext());
        }
        return instance;
    }

    public void setDingBoxStatusListener(DingBoxStatusListener listener) {
        this.dingBoxStatusListener = listener;
    }

    /**
     * 设置设备链接回掉，断开连接，正在连接等状态会回调在这儿
     */
    public void setConnectStatusListener() {
        BLEManager.getInstance(ctx).setConnectStatusListener(new StatusCallBack() {
            @Override
            public void onDisconnect(int code) {
                /**
                 * 丢失连接
                 * 正常调用close或者recy等 断开回调code 是0
                 * 如果异常断开回调code 参考package cn.healthdoc.bledemo.dingbox.ble.utils下的GattError
                 * */
                if (dingBoxStatusListener != null) {
                    dingBoxStatusListener.onDisconnect();
                }
            }

            @Override
            public void onConnectSuccess() {
                /**
                 * 连接成功
                 * */
                if (dingBoxStatusListener != null) {
                    dingBoxStatusListener.onConnected();
                }
            }

            @Override
            public void onConnectting() {
                /**
                 * 正在连接
                 * */
            }
        });
    }

    /**
     * 设置app接收到设备通知信息之后的回调
     *
     * 这块设置的设备通知也是与硬件约定的好的 当收到以1106开的数据均是推送信息这块要修改，需要修改blemananger中的相关代码
     *
     */
    public void setReceivedNotifyListener() {

        BLEManager.getInstance(ctx).setDeviceNotifyCallBack(new BoxNotifyCallBack() {
            @Override
            public void onReceiveNotify(String msg) {

            }
        });
    }

    /**
     * 开始搜索设备，能搜到的基本是设备设备
     */
    public void startScanDevices(ScanDevicesCommand.ScanDevicesCommandListenr<BluetoothDevice> listener) {
        ScanDevicesCommand command = new ScanDevicesCommand(ctx, listener);
        command.execute();
    }

    public void stopScanDevices() {
        BLEManager.getInstance(ctx).stopScanDevices();
    }

    /**
     * 开始连接设备，连接进度状态会在回调中出现
     */
    public void connect(BluetoothDevice device, DingBoxBaseCommand.CommandListener<String> listener) {
        ConnectCommand command = new ConnectCommand(ctx, device, listener);
        command.execute();
    }

    /**
     * 获取设备固件信息
     * 获取设备固件信息命令发送之后需要敲击设备
     * 然后设备才会回应  回应在listener中出来
     * 如没有 重复以上步骤
     */
    public void getBoxInfo(DingBoxBaseCommand.CommandListener<DingBoxInfo> listener) {
        GetBoxInfoOrderCommand command = new GetBoxInfoOrderCommand(ctx, listener);
        command.execute();
    }


    public BLEManager getBLEManager() {
        return BLEManager.getInstance(ctx);
    }

    /**
     * 释放所有连接
     */
    public void recy() {
        BLEManager.getInstance(ctx).recycle();
    }

    /**
     * 关闭当前连接
     */
    public void close() {
        BLEManager.getInstance(ctx).close();
    }

    public interface DingBoxStatusListener {
        void onDisconnect();

        void onConnected();
    }
}
