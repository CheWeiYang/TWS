package com.example.tws.bluetoothble;

import android.app.NotificationManager;
import android.app.Service;
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
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BleService extends Service {

    private final static String TAG = BleService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private List<BluetoothGattCharacteristic> characteristicList;

    public final static String ACTION_WAIT_CONNECTION = "ACTION_WAIT_CONNECTION";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String ACTION_CHARACTERISTICS_OK = "ACTION_CHARACTERISTICS_OK";

    public final static String CHARACTERISTIC_CHANGE = "CHARACTERISTIC_CHANGE";
    public final static String CHARACTERISTIC_READ = "CHARACTERISTIC_READ";
    public final static String CHARACTERISTIC_WRITE = "CHARACTERISTIC_WRITE";

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        initialize();
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public void stopNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    void stopService() {
        Log.d(TAG, "stopService");
        stopNotification();
        this.stopSelf();
    }

    public boolean connect(BleDevice device) {
        if (device == null || mBluetoothAdapter == null) {
            stopService();
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            SystemClock.sleep(1000);
            prepareConnect(device);
            return true;
        }
    }

    public void disconnect() {
        Log.e(TAG, "disconnectBle: " + mBluetoothGatt);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void prepareConnect(BleDevice bleDevice) {

        BluetoothDevice device = bleDevice.getDevice();
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Log.d(TAG, "requestConnectionPriority CONNECTION_PRIORITY_HIGH.");
            mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = BluetoothProfile.STATE_CONNECTING;
        broadcastUpdate(ACTION_WAIT_CONNECTION);
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange " + status + " -> " + newState + "(" + connectionState(newState) + ")");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = BluetoothProfile.STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.d(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                gatt.disconnect();
                gatt.close();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                gatt.close();
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: 連線成功");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                refreshCharacteristic();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(CHARACTERISTIC_READ, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        /**
         *連線狀態顯示文字使用
         **/
        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    private void refreshCharacteristic() {
        List<BluetoothGattService> gattServices = getSupportedGattServices();
        if (gattServices != null) {
            // 開始做取得Services裡面的資訊
            checkTempCharacteristics();
        }
    }

    private void tempCharacteristics(BluetoothGattCharacteristic characteristic) {
        if (characteristicList == null) {
            characteristicList = new ArrayList<>();
        }
        characteristicList.add(characteristic);
    }

    public boolean checkCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "checkCharacteristic uuid: " + characteristic.getUuid());
        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return true;
        }
        return false;
    }

    private void checkTempCharacteristics() {
        if (characteristicList != null && characteristicList.size() > 0) {
            Log.d(TAG, "checkTempCharacteristics size: " + characteristicList.size());
            BluetoothGattCharacteristic characteristic = characteristicList.get(0);
            if (characteristic != null) {
                turnOnNotification(characteristic);
                characteristicList.remove(0);
            } else {
                Log.d(TAG, "checkTempCharacteristics characteristic null!");
            }
        } else {
            Log.d(TAG, "checkTempCharacteristics done!");
            broadcastUpdate(ACTION_CHARACTERISTICS_OK);
        }
    }

    public void turnOnNotification(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "turnOnNotification (" + characteristic.getUuid() + ")");
        if (mBluetoothGatt != null) {
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            if (descriptors == null || descriptors.size() <= 0) {
                return;
            }
            int properties = characteristic.getProperties();
            byte[] descriptorValue = ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0 ?
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            BluetoothGattDescriptor descriptor = descriptors.get(0);
            if (descriptor != null) {
                Log.i(TAG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
                if (descriptorValue == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    Log.i(TAG, "enabling notifications for " + characteristic.getUuid());
                    //Log.i(TAG, "gatt.writeDescriptor(" + CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x01-00)");
                }
                if (descriptorValue == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    Log.i(TAG, "enabling indications for " + characteristic.getUuid());
                    //Log.i(TAG, "gatt.writeDescriptor(" + CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x02-00)");
                }
                descriptor.setValue(descriptorValue);
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {

        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdateWithData(String action, String data) {
        Intent intent = new Intent(action);
        intent.putExtra(action, data);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        UUID updateUuid = characteristic.getUuid();
        Log.d(TAG, "broadcastUpdate action: " + action + ", uuid: " + updateUuid + ", data: " + BleUtil.byteArrayToHexString(data));
//        if (UUID_CDTS_FEATURE.equals(updateUuid)) {
//
//        } else if (UUID_CDTS_RESPONSE.equals(updateUuid)) {
//            parseResponse(action, data);
//        }
    }

}
