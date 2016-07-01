package com.qing.blemanager.ble.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by liuqing on 16/6/7.
 */
public class BLESupportChecker {
    public static final int BLE_BLUETOOTHENABLE_REQCODE = 11;
    public static final int BLE_PERMISSION_REQCODE = 12;

    //检查权限
    @CheckResult
    public static boolean checkPermission(Context ctx) {
        /**
         * 6.0权限需要
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ctx,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //请求权限
    public static void startRequestBlueToothEnable(Activity activity, int requestCode) {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableIntent, requestCode);
    }

    public static void startQuestPermissions(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        return;
    }


    //检查手机是否支持BLE
    @CheckResult
    public static boolean checkDeviceSupport(Context ctx) {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            return false;
        }

        BluetoothManager mBluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            return false;
        }
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    public static boolean checkBlueToothEnable(){
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter!=null){
            return mBtAdapter.isEnabled();
        }
        return false;
    }
}
