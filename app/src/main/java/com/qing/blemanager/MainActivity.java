package com.qing.blemanager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.qing.blemanager.ble.utils.BLESupportChecker;
import com.qing.blemanager.command.ScanDevicesCommand;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();


    boolean permissionSupport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        if (!checkDeviceSupport()) {
            toastMsg("设备不支持，请确认.");
            finish();
        }

        if (!checkBlueToothStatus()) {
            startRequestBlueToothEnable();
        }
        permissionSupport = checkPermission();
        if (!permissionSupport) {
            startQuestPermissions();
        } else {
                    startScanDevices();
        }
    }

    private void initViews() {
    }


    private boolean checkPermission() {
        return BLESupportChecker.checkPermission(this);
    }

    public boolean checkDeviceSupport() {
        return BLESupportChecker.checkDeviceSupport(this);
    }

    public boolean checkBlueToothStatus() {
        return BLESupportChecker.checkBlueToothEnable();
    }

    public void startRequestBlueToothEnable() {
        BLESupportChecker.startRequestBlueToothEnable(this, BLESupportChecker.BLE_BLUETOOTHENABLE_REQCODE);
    }

    public void startQuestPermissions() {
        BLESupportChecker.startQuestPermissions(this, BLESupportChecker.BLE_PERMISSION_REQCODE);
    }



    private void startScanDevices() {
        OperaterManager.getInstance(this).startScanDevices(new ScanDevicesCommand.ScanDevicesCommandListenr<BluetoothDevice>() {
            @Override
            public void onSuccess(List<BluetoothDevice> data) {
            }

            @Override
            public void progress(BluetoothDevice itemData) {

            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFail(int code) {
            }

            @Override
            public void onSuccess(Object data) {

            }
        });

    }

    private void stopScanDevices() {
        OperaterManager.getInstance(this).stopScanDevices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        permissionSupport = false;
        if (requestCode == BLESupportChecker.BLE_PERMISSION_REQCODE) {
            if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionSupport = true;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BLESupportChecker.BLE_BLUETOOTHENABLE_REQCODE) {
            if (resultCode == Activity.RESULT_OK) {
                toastMsg("开启蓝牙成功");
            } else {
                // User did not enable Bluetooth or an error occurred
                toastMsg("开启蓝牙失败");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void logMsg(String msg) {
        Log.d(TAG, msg);
    }
}
