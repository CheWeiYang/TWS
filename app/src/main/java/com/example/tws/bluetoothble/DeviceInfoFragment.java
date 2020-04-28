package com.example.tws.bluetoothble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tws.R;

public class DeviceInfoFragment extends Fragment {

    private final static String TAG = DeviceInfoFragment.class.getSimpleName();
    private Context mContext;
    private BleDevice mBleDevice;
    private BleServiceConnection mBleServiceConnection = null;
    private ImageView imageViewStatus;
    private TextView textViewName, textViewMac, textViewVersion;

    public static DeviceInfoFragment newInstance(BleDevice device) {
        DeviceInfoFragment fragment = new DeviceInfoFragment();
        if (device != null) {
            fragment.mBleDevice = device;
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (mBleServiceConnection == null) {
            Log.d(TAG, "new BleServiceConnection");
            mContext = getActivity();
            mBleServiceConnection = new BleServiceConnection(mContext, mBleDevice);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mContext.registerReceiver(mGattUpdateReceiver, gattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbindBleService();
    }

    private void findViews(View view) {
        textViewVersion = view.findViewById(R.id.app_version);
        textViewName = view.findViewById(R.id.fragmentDevice_deviceName);
        textViewMac = view.findViewById(R.id.fragmentDevice_address);
        imageViewStatus = view.findViewById(R.id.fragmentDevice_status);
    }

    private void initViews() {
        textViewVersion.setText(BleUtil.getAppVersion(mContext));
        if (mBleDevice == null) {
            return;
        }
        textViewName.setText(mBleDevice.getDevice().getName());
        textViewMac.setText(mBleDevice.getDevice().getAddress());
        updateConnectionState();
        imageViewStatus.setOnClickListener(v -> {
            Log.d(TAG, "bleConnectStatus : " + BleUtil.bleConnectStatus);
            if (BleUtil.bleConnectStatus == BleUtil.BLE_CONNECTED) {
                if (mBleServiceConnection != null) {
                    mBleServiceConnection.disconnectBle();
                }
                BleUtil.bleConnectStatus = BleUtil.BLE_NOT_CONNECT;
                updateConnectionState();
            } else if (BleUtil.bleConnectStatus == BleUtil.BLE_NOT_CONNECT) {
                if (mBleServiceConnection != null) {
                    mBleServiceConnection.connectBle(mBleDevice);
                }
            }
        });
    }

    private static IntentFilter gattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_WAIT_CONNECTION);
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_CHARACTERISTICS_OK);
        intentFilter.addAction(BleService.CHARACTERISTIC_CHANGE);
        intentFilter.addAction(BleService.CHARACTERISTIC_READ);
        intentFilter.addAction(BleService.CHARACTERISTIC_WRITE);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "mGattUpdateReceiver action: " + action);

            switch (action) {
                case BleService.ACTION_GATT_CONNECTED:
                    BleUtil.bleConnectStatus = BleUtil.BLE_CONNECTING;
                    updateConnectionState();
                    break;
                case BleService.ACTION_GATT_DISCONNECTED:
                    BleUtil.bleConnectStatus = BleUtil.BLE_NOT_CONNECT;
                    updateConnectionState();
                    break;
                case BleService.ACTION_GATT_SERVICES_DISCOVERED:
                    BleUtil.bleConnectStatus = BleUtil.BLE_CONNECTED;
                    updateConnectionState();
                    break;
                case BleService.CHARACTERISTIC_READ:

                default:
            }
        }
    };

    private void unbindBleService() {

        if (mBleServiceConnection != null) {

            mBleServiceConnection.unbindBleService();
        }
    }

    private void updateConnectionState() {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(() -> {
            int resId = R.mipmap.ic_connect_not;
            switch (BleUtil.bleConnectStatus) {
                case BleUtil.BLE_CONNECTED:
                    resId = R.mipmap.ic_connected;
                    break;
                case BleUtil.BLE_CONNECTING:
                    resId = R.mipmap.ic_connecting;
                    break;
                default:
                    break;
            }
            imageViewStatus.setImageResource(resId);
        });
    }


}
