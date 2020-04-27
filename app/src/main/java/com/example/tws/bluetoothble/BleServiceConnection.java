package com.example.tws.bluetoothble;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class BleServiceConnection implements ServiceConnection {

    private final static String TAG = BleServiceConnection.class.getSimpleName();
    private boolean isServiceConnected = false;
    private Context mContext;
    private BleService mBleService;
    private BleDevice mBleDevice;

    public BleServiceConnection(Context context, BleDevice device) {
        mContext = context;
        mBleDevice = device;
        bindBleService();
    }

    private void bindBleService() {
        boolean bindResult = mContext.bindService(
                new Intent(mContext, BleService.class),
                this,
                Activity.BIND_AUTO_CREATE);
        Log.d(TAG, "bindBleService result>> " + bindResult);
    }

    public void unbindBleService() {
        Log.d(TAG, "unbindBleService");
        try {
            mContext.unbindService(this);
            mBleService = null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void connectBle(BleDevice device) {
        Log.e(TAG, "connectBle mBleService: " + mBleService + ", mBleDevice>> " + mBleDevice + ", newDevice: " + device);
        if (device != null) {
            mBleDevice = device;
            if (mBleService != null && mBleDevice != null) {
                mBleService.connect(mBleDevice);
            }
        }
    }

    public void disconnectBle() {
        Log.d(TAG, "disconnectBle");
        if (mBleService != null) {
            mBleService.disconnect();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service Connection established with " + name.getShortClassName());
        isServiceConnected = true;
        mBleService = ((BleService.LocalBinder) service).getService();
        if (mBleService != null && mBleDevice != null) {
            Log.d(TAG, "onServiceConnected connect>> " + mBleDevice);
            mBleService.connect(mBleDevice);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service Connection destroyed with " + name.getShortClassName());
        isServiceConnected = false;
        mBleService = null;
    }

    public boolean isServiceConnected() {
        Log.d(TAG, "Connection: " + this + ", isServiceConnected " + isServiceConnected);
        return isServiceConnected;
    }

}
