package com.example.tws.bluetoothble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tws.MainActivity;
import com.example.tws.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BleDeviceFragment extends Fragment {

    private static final String TAG = BleDeviceFragment.class.getSimpleName();
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;
    BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning = false;
    private static final int SCAN_TIME = 10000; //為幾秒後要執行此Runnable(關閉掃描)，目前設10秒
    private Handler mHandler; //該Handler用來搜尋Devices10秒後，自動停止搜尋
    private TextView mConnect;
    private TextView mStatus;
    private RecyclerView mRecyclerView;
    private BleDeviceAdapter mBleDeviceAdapter;
    ArrayList<BleDevice> mBleDevices = new ArrayList<>();
    private BleDevice tempDevice = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mContext = getActivity().getBaseContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mConnect = view.findViewById(R.id.search_device);
        mStatus = view.findViewById(R.id.search_status);
        mRecyclerView = view.findViewById(R.id.deviceRecyclerView);
        initView();
        checkBLEFeature(); //確認手機是否能夠使用BLE設備
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initView() {
        mRecyclerView.setHasFixedSize(true);
        mBleDeviceAdapter = new BleDeviceAdapter(mContext, new ArrayList<BleDevice>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mBleDeviceAdapter);

        /**
         * 點選RecyclerView裡面的item
         * */
        mBleDeviceAdapter.setupAdapterListener(device -> {
            if (device != null) {
                Log.d(TAG, "onSelect device: " + device.getDevice().getName());
                toDevicePage(device);
            }
        });

        mConnect.setOnClickListener(v -> {
            Log.d(TAG, "Click Start Scan Button");
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    //pairDevice(); // 從配對裝置進行連接
                    scanLeDevice(true);//掃描裝置
                }
            } else {
                Log.d(TAG, "mBluetoothAdapter is null");
            }
        });

    }

    private void toDevicePage(BleDevice device) {
        Log.d(TAG, "to Device Page");
        if (mScanning) {
            updateScanStatus(false);
        }
        Fragment fragment = DeviceInfoFragment.newInstance(device);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, fragment).commitNow();
    }

    /**
     * 確認手機是否能夠使用BLE設備
     */
    private void checkBLEFeature() {
        // 檢查手機是否支援藍芽5.0
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "手機無法支援BLE裝置", Toast.LENGTH_LONG).show();
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    private void getPairDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Find Pair Device");
                mBluetoothDevice = device;
                if (!isContainDevices(tempDevice)) {
                    mBleDevices.add(tempDevice);
                    mBleDeviceAdapter.refreshDevice(mBleDevices);
                }
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            mBleDevices = new ArrayList<>();
            mHandler.postDelayed(() -> {
                if (mScanning) {
                    updateScanStatus(false);
                }
            }, SCAN_TIME); //目前設置10秒
            updateScanStatus(true);
        } else {
            updateScanStatus(false);
        }
    }

    private void updateScanStatus(boolean status) {
        Log.d(TAG, "updateScanStatus status: " + status);
        mScanning = status;
        if (status) {
            mBluetoothLeScanner.startScan(scanCallback);//開始搜尋BLE設備
            mStatus.setText("搜尋狀態 : 搜尋中...");
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);//停止搜尋
            mStatus.setText("搜尋狀態 : 停止搜尋");
        }
    }

    private boolean isContainDevices(BleDevice bleDevice) {
        if (mBleDevices != null) {
            for (BleDevice device : mBleDevices) {
                if (bleDevice.getDevice().equals(device.getDevice())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            getActivity().runOnUiThread(() -> {
                if (mScanning) {
                    BluetoothDevice device = result.getDevice();
                    String deviceName = device.getName();
                    if (deviceName != null) {  //這邊可以加上我要的裝置名稱進行過濾
                        tempDevice = new BleDevice();
                        tempDevice.setDevice(result.getDevice());
                        tempDevice.setRssi(result.getRssi());
                        if (!isContainDevices(tempDevice)) {
                            mBleDevices.add(tempDevice);
                            mBleDeviceAdapter.refreshDevice(mBleDevices);
                        }
                    }
                }

//                BluetoothDevice device = result.getDevice();
//                Log.d(TAG, "ScanDevice Name : " + device.getName());
//                if (airPods.equals(device.getName())) {
//                    Log.d(TAG, "ScanDevice Name : " + device.getName());
//                    Log.d(TAG, "ScanDevice Address : " + device.getAddress());
//
//                    mBluetoothDevice = device;
//
//                    mBluetoothLeScanner.stopScan(scanCallback);
//                    // 當抓取到我要的Device時(這邊目前測試是抓我的AirPods)，開始進行連線
//                    mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
//                }
            });
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //連線狀態改變的回撥
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "status狀態: 成功");
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 連線成功後系統會非同步執行發現服務的過程，成功的話會有onServicesDiscovered的Call Back
                Log.d(TAG, "newState :  連線成功初步成功，等待onServicesDiscovered Call Back ");
                try {
                    Log.d(TAG, "開始發現服務: ");
                    gatt.discoverServices();
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "newState :  藍芽裝置斷開連線");
                mBluetoothGatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            getActivity().runOnUiThread(() -> {
                mStatus.setText("已連線");
            });
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "status :  正式連線!!!!! ");
                List<BluetoothGattService> gattServices = gatt.getServices();
                Log.d(TAG, " Services Count : " + gattServices.size());
                initCharacteristic();
                try {
                    Thread.sleep(200);//延遲傳送，否則第一次訊息會不成功
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                UUID uuid = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
//                BluetoothGattService service = gatt.getService(uuid);  //這邊理當要設定廠商提供的服務UUID 才可以根據裝置擁有的服務去抓取數據
//                if (service == null) {
//                    Log.d(TAG, "service :  沒有抓到Airpods Service ");
//                }

            } else {
                Log.d(TAG, "status :  服務沒有被抓到，GG ");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "newState : 資料讀取成功");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite : 寫入成功");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    /**
     * 當連線成功後，開始進行初始化後續操作
     * 1 檢視服務。或者便利查詢指定的（和目標硬體UUID符合的）服務。     *
     * 2 獲得指定服務的特徵 characteristic
     * 3 訂閱“特徵”發生變化的通知”
     */
    private void initCharacteristic() {
        if (mBluetoothGatt == null) throw new NullPointerException();
        Log.d(TAG, " Services : " + mBluetoothGatt.getServices().toString());

        //
        // ParcelUuid[] uuids = mBluetoothDevice.getUuids();
        //UUID uuid = uuids[0].getUuid();
        // Log.d(TAG, "Server UUID " + uuid);
//        BluetoothGattCharacteristic characteristic1 = mBluetoothGatt.getService(uuid).getCharacteristic(uuid);

//        for (BluetoothGattService gattService : mBluetoothGatt.getServices()) {
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                // 若要獲取電量資訊 應該需要有特定的Characteristic的UUID
//                if (gattCharacteristic.getUuid().toString().equals("0x180F")) {
//                    Log.d(TAG, "剛好有電量資訊?");
//                    mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
//                    mBluetoothGatt.readCharacteristic(gattCharacteristic);
//                }
//            }
//        }


    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            }
        }
    };


}
