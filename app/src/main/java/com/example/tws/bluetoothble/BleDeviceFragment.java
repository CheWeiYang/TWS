package com.example.tws.bluetoothble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tws.AboutFragment;
import com.example.tws.MainActivity;
import com.example.tws.R;
import com.example.tws.walkThrough.WalkThroughActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceFragment extends Fragment {

    private static final String TAG = BleDeviceFragment.class.getSimpleName();
    private MainActivity mActivity;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning = false;
    private static final int SCAN_TIME = 10000; //為幾秒後要執行此Runnable(關閉掃描)，目前設10秒
    private Handler mHandler; //該Handler用來搜尋Devices10秒後，自動停止搜尋
    private TextView mConnect;
    private TextView mStatus;
    private RecyclerView mRecyclerView;
    private Toolbar mToolBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private BleDeviceAdapter mBleDeviceAdapter;
    ArrayList<BleDevice> mBleDevices = new ArrayList<>();
    private BleDevice tempDevice = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
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
        mToolBar = view.findViewById(R.id.toolBar);
        mDrawerLayout = view.findViewById(R.id.drawerLayout);
        mNavigationView = view.findViewById(R.id.nav_view);
        initView();
        checkBLEFeature(); //確認手機是否能夠使用BLE設備
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initView() {
        mRecyclerView.setHasFixedSize(true);
        mBleDeviceAdapter = new BleDeviceAdapter(mActivity, new ArrayList<BleDevice>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mBleDeviceAdapter);
        mToolBar.setNavigationIcon(R.mipmap.ic_action_edit_songlist);
        // Navigation Drawer Open
        mToolBar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(Gravity.LEFT));

        mActivity.getResources().getDimension(R.dimen.design_navigation_icon_padding);
        mNavigationView.getBackground().setAlpha(150);

        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.add_tws:
                    Log.d(TAG, "add_tws");
                    break;
                case R.id.setting:
                    Log.d(TAG, "setting");
                    break;
                case R.id.discover:
                    Log.d(TAG, "discover");
                    Intent intent = new Intent(getActivity(), WalkThroughActivity.class);
                    startActivity(intent);
                    break;
                case R.id.about:
                    Log.d(TAG, "about");
                    mActivity.getSupportFragmentManager().beginTransaction().
                            replace(R.id.container, AboutFragment.Companion.newInstance()).
                            addToBackStack(TAG).
                            commit();
                    break;
                default:
                    Log.d(TAG, "nothing");
            }
            return false;
        });

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

        mActivity.getSupportFragmentManager().beginTransaction().
                //setCustomAnimations(R.anim.from_right, R.anim.out_left).
                hide(BleDeviceFragment.this).add(R.id.container, DeviceInfoFragment.newInstance(device)).
                addToBackStack(null).
                commit();
    }

    /**
     * 確認手機是否能夠使用BLE設備
     */
    private void checkBLEFeature() {
        // 檢查手機是否支援藍芽5.0
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "手機無法支援BLE裝置", Toast.LENGTH_LONG).show();
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

//    private void getPairDevice() {
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                Log.d(TAG, "Find Pair Device");
//                mBluetoothDevice = device;
//                if (!isContainDevices(tempDevice)) {
//                    mBleDevices.add(tempDevice);
//                    mBleDeviceAdapter.refreshDevice(mBleDevices);
//                }
//            }
//        }
//    }

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


}
