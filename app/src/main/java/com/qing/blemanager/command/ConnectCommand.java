package com.qing.blemanager.command;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qing.blemanager.ConnectError;
import com.qing.blemanager.DingOrderSupport;
import com.qing.blemanager.ble.listener.ConnectCallBack;

import java.util.List;


/**
 * Created by liuqing on 16/6/16.
 */
public class ConnectCommand extends DingBoxBaseCommand {
    BluetoothDevice device;

    public ConnectCommand(Context ctx, @NonNull BluetoothDevice device, CommandListener<String> commandListener) {
        super(ctx,commandListener);
        this.device = device;
    }

    @Override
    public void execute() {
        boolean result = getBleManager().connect(device.getName(), device.getAddress(), new ConnectCallBack() {
            @Override
            public void onConnectSuccess() {
                Log.d("111", "onConnectSuccess: ");
            }

            @Override
            public void onConnectting() {
                Log.d("111", "onConnectting: ");
            }

            @Override
            public void onConnectFail() {
                getCommandListener().onFail(ConnectError.CONNECT_ERRORCODE_CONNECTFAIL);
            }

            @Override
            public void onFindServices(List<BluetoothGattService> services) {
                boolean isExist = getBleManager().isExistService(DingOrderSupport.DingOrderServiceUUID);
                if (isExist){
                    getBleManager().setServiceUUID(DingOrderSupport.DingOrderServiceUUID);
                    getBleManager().setWriteUUID(DingOrderSupport.DingOrderService_write_UUID);
                    getBleManager().setReadUUID(DingOrderSupport.DingOrderService_read_UUID);
                    getCommandListener().onSuccess("");
                }else{
                    getCommandListener().onFail(ConnectError.CONNECT_ERRORCODE_SERVICENOTFOUND);
                }
                getBleManager().removeConnectCallBack();
            }

            @Override
            public void onFindServicesError() {
                getCommandListener().onFail(ConnectError.CONNECT_ERRORCODE_SERVICENOTFOUND);
                getBleManager().removeConnectCallBack();
            }
        });
        if (result) {

        } else {
            getCommandListener().onFail(ConnectError.CONNECT_ERRORCODE_CONNECTFAIL);
        }
    }

}
