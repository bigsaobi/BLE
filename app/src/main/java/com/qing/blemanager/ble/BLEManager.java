package com.qing.blemanager.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.CheckResult;
import android.text.TextUtils;
import android.util.Log;

import com.qing.blemanager.DingOrderSupport;
import com.qing.blemanager.ble.listener.BoxNotifyCallBack;
import com.qing.blemanager.ble.listener.CharNotifyCallBack;
import com.qing.blemanager.ble.listener.ConnectCallBack;
import com.qing.blemanager.ble.listener.OnDeviceScanListener;
import com.qing.blemanager.ble.listener.OrderCallBack;
import com.qing.blemanager.ble.listener.StatusCallBack;
import com.qing.blemanager.ble.utils.BLETools;

import java.util.List;
import java.util.UUID;


/**
 * Created by liuqing on 16/5/30.
 */
public class BLEManager implements BLEOperater, BLEScaner {
    private static final String TAG = BLEManager.class.getSimpleName();

    private final static UUID CHARACTERISTIC_NOTIFY_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static String DEVICENOTIFY_PREFIX = "1106";

    private UUID curServiceUUID; //= UUID.fromString("dba30001-b4ee-3a90-714e-7d0233123998");
    private UUID writeUUID;// = UUID.fromString("dba30002-b4ee-3a90-714e-7d0233123998");
    private UUID readUUID;// = UUID.fromString("dba30003-b4ee-3a90-714e-7d0233123998");

    //链接的设备ip和设备名称
//    private String curDeviceAddress, curDeviceName;
    private BluetoothGatt mBluetoothGatt;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private int curConnectionState = STATE_DISCONNECTED;

    private Context ctx;

    private Handler _handler = new Handler();

    private boolean mScanning = false;
    private static final long SCAN_PERIOD = 5000; //10 seconds
    private OnDeviceScanListener onDeviceScanListener;

    private static BLEManager instance;

    private ConnectCallBack connectCallBack;
    private OrderCallBack orderCallBack;
    private StatusCallBack statusCallBack;
    private BoxNotifyCallBack boxNotifyCallBack;
    private CharNotifyCallBack charNotifyCallBack;

    private BLEManager(Context ctx) {
        this.ctx = ctx;
    }

    public static BLEManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new BLEManager(ctx.getApplicationContext());
            instance.initialze();
        }
        return instance;
    }

    public void initialze() {
        mBluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    @Override
    public boolean isScanningDevices() {
        return mScanning;
    }


    @Override
    public void startScanDevices(final OnDeviceScanListener deviceScanListener) {
        this.onDeviceScanListener = deviceScanListener;
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(onDeviceScanListener.getCallback());
                onDeviceScanListener.onScanFinish();
            }
        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(onDeviceScanListener.getCallback());
        onDeviceScanListener.onScanStart();
    }

    public void startScanDevices(UUID[] uuids, OnDeviceScanListener deviceScanListener) {
        this.onDeviceScanListener = deviceScanListener;
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(onDeviceScanListener.getCallback());
                onDeviceScanListener.onScanFinish();
            }
        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(uuids, onDeviceScanListener.getCallback());
        onDeviceScanListener.onScanStart();
    }

    @Override
    public void stopScanDevices() {

        if (mScanning && onDeviceScanListener != null) {
            mBluetoothAdapter.stopLeScan(onDeviceScanListener.getCallback());
            onDeviceScanListener.onScanFinish();
        }
        mScanning = false;
    }

    public void removeConnectCallBack() {
        connectCallBack = null;
    }

    @Override
    @CheckResult
    public boolean connect(String deviceName, String address, ConnectCallBack callback) {
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothAdapter = ((BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        final boolean autoConnect = shouldAutoConnect();

        mBluetoothGatt = device.connectGatt(ctx, autoConnect, gattCallback);
        curConnectionState = STATE_CONNECTING;

        connectCallBack = callback;
        if (connectCallBack != null) {
            connectCallBack.onConnectting();
        }
        if (statusCallBack != null) {
            statusCallBack.onConnectting();
        }
        return true;
    }

    public void setConnectStatusListener(StatusCallBack statusCallBack) {
        this.statusCallBack = statusCallBack;
    }

    public void setDeviceNotifyCallBack(BoxNotifyCallBack boxNotifyCallBack) {
        this.boxNotifyCallBack = boxNotifyCallBack;
    }

    /**
     * 判断是否是药盒主动告知是时间服药
     */
    public boolean isNotifyMedTime(String response) {
        if (response.startsWith(DEVICENOTIFY_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldAutoConnect() {
        return false;
    }

    public boolean disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    @Override
    public void recycle() {
        disconnect();
        close();
        mBluetoothGatt = null;
    }

    public void removeOrderCallBack() {
        orderCallBack = null;
    }

    @Override
    public boolean sendOrder(UUID tagUUID, byte[] order, OrderCallBack orderCallBack) {//byte[] orderBytes
        if (curConnectionState != STATE_CONNECTED || mBluetoothGatt == null) {
            return false;
        }
        BluetoothGattCharacteristic orderCharacteristics = getCharactisticsByUUID(curServiceUUID, tagUUID);
        if (orderCharacteristics == null) {
            return false;
        }
        orderCharacteristics.setValue(order);
        return writeCharacteristic(orderCharacteristics, orderCallBack);//mBluetoothGatt.writeCharacteristic(orderCharacteristics);
    }

    public BluetoothGattCharacteristic getCharactisticsByUUID(UUID serviceUUID, UUID charUUID) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return null;
        }

        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            return null;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUUID);
        if (characteristic == null) {
            return null;
        }
        return characteristic;
    }

    @Override
    public List<BluetoothGattService> loadServices() {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return null;
        }
        return gatt.getServices();
    }

    @Override
    public List<BluetoothGattCharacteristic> loadCurServiceCharacteristic() {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return null;
        }

        BluetoothGattService service = gatt.getService(getServiceUUID());
        if (service == null) {
            return null;
        }

        return service.getCharacteristics();
    }

    @CheckResult
    @Override
    public boolean isExistService(UUID serviceUUID) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return false;
        }
        return gatt.getService(serviceUUID) != null;
    }

    @Override
    public List<BluetoothGattCharacteristic> loadServiceCharacteristic(UUID serviceUUID) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return null;
        }

        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            return null;
        }

        return service.getCharacteristics();
    }

    public boolean isNeedSetNotifyIndication() {
        boolean result = false;
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return result;
        }
        BluetoothGattService service = gatt.getService(getServiceUUID());
        if (service == null) {
            return result;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(getReadCharacteristicUUID());
        if (characteristic == null) {
            return result;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_NOTIFY_DESCRIPTOR_UUID);
        if (descriptor != null) {
            return descriptor.getValue() != BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        }
        return false;
    }

    public boolean isNeedSetNotifyNotification() {
        boolean result = false;
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return result;
        }
        BluetoothGattService service = gatt.getService(getServiceUUID());
        if (service == null) {
            return result;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(getReadCharacteristicUUID());
        if (characteristic == null) {
            return result;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_NOTIFY_DESCRIPTOR_UUID);
        if (descriptor != null) {
            return descriptor.getValue() != BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        }
        return false;
    }

    public void removceCharNotifyCallBack() {
        this.charNotifyCallBack = null;
    }

    @Override
    public boolean setCharacteristicNotifyNotification(CharNotifyCallBack charNotifyCallBack) {
        boolean result = false;
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return result;
        }
        BluetoothGattService service = gatt.getService(getServiceUUID());
        if (service == null) {
            return result;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(getReadCharacteristicUUID());
        if (characteristic == null) {
            return result;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_NOTIFY_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        result = gatt.setCharacteristicNotification(characteristic, true);
        if (result) {
            this.charNotifyCallBack = charNotifyCallBack;
            this.charNotifyCallBack.onStart();
        } else {
            charNotifyCallBack.onFail();
        }
        return result;
    }

    @Override
    public boolean setCharacteristicNotifyIndication(CharNotifyCallBack charNotifyCallBack) {
        boolean result = false;
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            return result;
        }
        BluetoothGattService service = gatt.getService(getServiceUUID());
        if (service == null) {
            return result;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(getReadCharacteristicUUID());
        if (characteristic == null) {
            return result;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_NOTIFY_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        result = gatt.setCharacteristicNotification(characteristic, true);
        if (result) {
            this.charNotifyCallBack = charNotifyCallBack;
            this.charNotifyCallBack.onStart();
        } else {
            charNotifyCallBack.onFail();
        }
        return result;
    }

    public void setServiceUUID(UUID curServiceUUID) {
        this.curServiceUUID = curServiceUUID;
    }

    public void setWriteUUID(UUID writeUUID) {
        this.writeUUID = writeUUID;
    }

    public void setReadUUID(UUID readUUID) {
        this.readUUID = readUUID;
    }

    @Override
    public UUID getServiceUUID() {
        return curServiceUUID;
    }

    @Override
    public UUID getWriteCharacteristicUUID() {
        return writeUUID;
    }

    @Override
    public UUID getReadCharacteristicUUID() {
        return readUUID;
    }

    @Override
    public final boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            return false;

        return gatt.readCharacteristic(characteristic);
    }

    @Override
    public final boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic, OrderCallBack callBack) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            return false;
        this.orderCallBack = callBack;
        return gatt.writeCharacteristic(characteristic);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                curConnectionState = STATE_CONNECTED;
                // Attempts to discover services after successful connection.
                if (connectCallBack != null) {
                    connectCallBack.onConnectSuccess();
                }
                if (statusCallBack != null) {
                    statusCallBack.onConnectSuccess();
                }
                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                            boolean result = mBluetoothGatt.discoverServices();
                        }
                    }
                }, 600);
            } else {
                if (curConnectionState == STATE_CONNECTING && newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (connectCallBack != null) {
                        connectCallBack.onConnectFail();
                    }
                }
                if (statusCallBack != null) {
                    statusCallBack.onDisconnect(status);
                }
                curConnectionState = STATE_DISCONNECTED;
                recycle();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (connectCallBack != null) {
                    connectCallBack.onFindServices(gatt.getServices());
                }
            } else {
                if (connectCallBack != null) {
                    connectCallBack.onFindServicesError();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String text = null;
            if (characteristic.getValue() != null) {
                text = BLETools.bytes2HexString(characteristic.getValue());
            }
            if (!TextUtils.isEmpty(text) && isNotifyMedTime(text)) {
                if (boxNotifyCallBack != null) {
                    boxNotifyCallBack.onReceiveNotify(text);
                }
            } else {
                if (orderCallBack != null) {
                    orderCallBack.onChange(characteristic);
                }
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (orderCallBack != null) {
                orderCallBack.onWrite(status, characteristic);
            }

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (charNotifyCallBack != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    charNotifyCallBack.onWriteDescriptorSuccess(descriptor);
                } else {
                    charNotifyCallBack.onWriteDescriptorFail(descriptor);
                }
            }
        }

    };



}
