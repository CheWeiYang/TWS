package com.example.tws.bluetoothble;

import android.bluetooth.BluetoothDevice;

public class BleDevice {

    private BluetoothDevice device;
    private int rssi;

    public void setDevice(BluetoothDevice device) {this.device = device;}
    public BluetoothDevice getDevice() {return device;}
    public void setRssi(int rssi) {this.rssi = rssi;}
    public int getRssi() {return rssi;}

}
