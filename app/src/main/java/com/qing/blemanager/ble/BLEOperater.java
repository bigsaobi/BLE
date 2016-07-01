package com.qing.blemanager.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.qing.blemanager.ble.listener.CharNotifyCallBack;
import com.qing.blemanager.ble.listener.ConnectCallBack;
import com.qing.blemanager.ble.listener.OrderCallBack;

import java.util.List;
import java.util.UUID;


/**
 * Created by liuqing on 16/6/7.
 */
public interface BLEOperater {
    /**
     * 链接蓝牙设备
     * @param address
     * @param name
     * */
    boolean connect(String address, String name, ConnectCallBack connectCallBack);

    /**
     * 设置是否自动重连
     */
    public boolean shouldAutoConnect();
    /**
     * 关闭当前连接 gatt
     * */
    void close();
    /**
     * 关闭连接且释放资源
     * */
    void recycle();

    /**
     * 发送16进制指令
     * @param tagUUID 写入目标特征UUID
     * @param order byte[]16进制
     * */
    boolean sendOrder(UUID tagUUID, byte[] order, OrderCallBack orderCallBack);

    boolean isExistService(UUID serviceUUID);

    /**
     * 读取所有可用服务
     * */
    List<BluetoothGattService> loadServices();

    /**
     * 读取当前服务下的全部特征
     * */
    List<BluetoothGattCharacteristic> loadCurServiceCharacteristic();
    /**
     * 读取特定服务下的全部特征
     * @param serviceUUID 指定服务的UUID
     * */
    List<BluetoothGattCharacteristic> loadServiceCharacteristic(UUID serviceUUID);

    /**
     * 设置莫一特征通知
     * @param charNotifyCallBack 特征通知Notification设置回调
     * */
    boolean setCharacteristicNotifyNotification(CharNotifyCallBack charNotifyCallBack);
    /**
     * 设置莫一特征通知
     * @param charNotifyCallBack 特征通知Indication设置回调
     * */
    boolean setCharacteristicNotifyIndication(CharNotifyCallBack charNotifyCallBack);

    /**
     * 获取当前服务的UUID
     * */
    UUID getServiceUUID();
    /**
     * 获取写操作特征的UUID
     * */
    UUID getWriteCharacteristicUUID();
    /**
     * 获取读操作特征的UUID
     * */
    UUID getReadCharacteristicUUID();

    /**
     * 对特征发起读操作
     * 结果会在回调中返回
     * @param characteristic 目标特征
     * */
    boolean readCharacteristic(BluetoothGattCharacteristic characteristic);

    /**
     * 对特征发起写操作，写的值请在调用之前set
     * 结果会在回调中返回
     * @param characteristic 目标特征
     * */
    boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, OrderCallBack callBack);




}
