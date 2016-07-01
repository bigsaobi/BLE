package com.qing.blemanager.command;

import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import com.qing.blemanager.ble.listener.CharNotifyCallBack;


/**
 * Created by liuqing on 16/6/16.
 */
public class NotifyCommand extends DingBoxBaseCommand {

    public NotifyCommand(Context ctx, CommandListener<Boolean> commandListener) {
        super(ctx, commandListener);
    }

    @Override
    public void execute() {
        //两种通知方式，一般使用的是notification通知
        getBleManager().setCharacteristicNotifyNotification(charNotifyCallBack);
//        getBleManager().setCharacteristicNotifyIndication(charNotifyCallBack);
    }

    private final CharNotifyCallBack charNotifyCallBack = new CharNotifyCallBack() {
        @Override
        public void onStart() {
            Log.d("CharNotifyCallBack", "onStart: ");
        }

        @Override
        public void onFail() {
            Log.d("CharNotifyCallBack", "onFail: ");
            getCommandListener().onFail(0);
        }

        @Override
        public void onWriteDescriptorFail(BluetoothGattDescriptor descriptor) {
            Log.d("CharNotifyCallBack", "onWriteDescriptorFail: ");
            getCommandListener().onFail(0);
            getBleManager().removceCharNotifyCallBack();
        }

        @Override
        public void onWriteDescriptorSuccess(BluetoothGattDescriptor descriptor) {
            Log.d("CharNotifyCallBack", "onWriteDescriptorSuccess: ");
            getCommandListener().onSuccess(true);
            getBleManager().removceCharNotifyCallBack();
        }
    };
}
