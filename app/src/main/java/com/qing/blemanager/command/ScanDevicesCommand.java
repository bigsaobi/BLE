package com.qing.blemanager.command;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.qing.blemanager.DingOrderSupport;
import com.qing.blemanager.ble.listener.OnDeviceScanListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by liuqing on 16/6/16.
 */
public class ScanDevicesCommand extends DingBoxBaseCommand {

    public ScanDevicesCommand(Context ctx, ScanDevicesCommandListenr<BluetoothDevice> listener) {
        super(ctx,listener);
    }

    @Override
    public void execute() {
        if (!getBleManager().isScanningDevices()){
            final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();//,
            getBleManager().startScanDevices(new UUID[]{DingOrderSupport.DingOrderServiceUUID},new OnDeviceScanListener() {
                @Override
                public void onDeviceFind(BluetoothDevice bluetoothDevice, byte[] bytes) {
                    if (!devices.contains(bluetoothDevice)){
                        devices.add(bluetoothDevice);
                        getScanDeviceCommandListener().progress(bluetoothDevice);
                    }

                }

                @Override
                public void onScanStart() {
                    getScanDeviceCommandListener().onStart();
                }

                @Override
                public void onScanFinish() {
                    getScanDeviceCommandListener().onSuccess(devices);
                }
            });
        }else{
            getCommandListener().onFail(0);
        }

    }

    public ScanDevicesCommandListenr<BluetoothDevice> getScanDeviceCommandListener(){
        return (ScanDevicesCommandListenr<BluetoothDevice>)getCommandListener();
    }

    public interface ScanDevicesCommandListenr<BluetoothDevice> extends CommandListener{
        void onSuccess(List<android.bluetooth.BluetoothDevice> data);

        void progress(android.bluetooth.BluetoothDevice itemData);
    }

}
