package com.qing.blemanager.command;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.qing.blemanager.DingBoxInfo;
import com.qing.blemanager.DingOrderSupport;
import com.qing.blemanager.ble.listener.OrderCallBack;
import com.qing.blemanager.ble.utils.BLETools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by liuqing on 16/6/16.
 */
public class GetBoxInfoOrderCommand extends DingBoxBaseCommand {
    private static final String TAG = GetBoxInfoOrderCommand.class.getSimpleName();
    DingOrderSupport.Order order;
    UUID tagUUID = DingOrderSupport.DingOrderService_write_UUID;
    UUID responseUUID = DingOrderSupport.DingOrderService_read_UUID;
    ArrayList<byte[]> dataPkgs = new ArrayList<byte[]>();

    public GetBoxInfoOrderCommand(Context ctx, CommandListener<DingBoxInfo> listener) {
        super(ctx,listener);
        this.order = DingOrderSupport.getBoxInfoOrder();
    }

    @Override
    public void execute() {
        startSendOrder();
    }

    private void startSendOrder(){
        dataPkgs.clear();
        dataPkgs.addAll(order.getValues());
        sendOrderPkg(dataPkgs.remove(0));
    }
    private void sendOrderPkg(final byte[] dataPkg){
        boolean result = getBleManager().sendOrder(tagUUID, dataPkg, orderCallBack);
        if (!result){
            result = getBleManager().sendOrder(tagUUID, dataPkg, orderCallBack);
            if (!result){
                getCommandListener().onFail(0);
            }
        }
    }

    private final OrderCallBack orderCallBack = new OrderCallBack() {
        @Override
        public void onStart() {
            Log.d(TAG, "onStart: 正在发送命令"+order.getOrderName());
        }

        @Override
        public void onFail() {
            getCommandListener().onFail(0);
            getBleManager().removeOrderCallBack();
        }

        @Override
        public void onWrite(int status, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(tagUUID)){
                Log.d(TAG, "onWrite: 成功写入");
            }
        }

        @Override
        public void onChange(BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(getBleManager().getReadCharacteristicUUID())){
                String responseString = BLETools.bytes2HexString(characteristic.getValue());
                DingBoxInfo result = getBoxInfo(responseString);
                if (result == null){
                    getCommandListener().onFail(0);
                }else{
                    getCommandListener().onSuccess(result);
                }
            }
        }
    };
    //这个识与硬件约定返回数据来判断，这个是只是我现在与硬件这样约定返回
    private DingBoxInfo getBoxInfo(String response){
        DingBoxInfo result = null;
        if (response.length() == 16 && response.startsWith("1101")){
            String firmwareVersion = response.substring(6,12);
            String hardwareVersion = response.substring(12,14);
            result = new DingBoxInfo();
            result.setFirmwareVersion(firmwareVersion);
            result.setHardwareVersion(hardwareVersion);
        }
        return result;
    }
}
