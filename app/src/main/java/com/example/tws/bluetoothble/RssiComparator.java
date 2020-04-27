package com.example.tws.bluetoothble;

import java.util.Comparator;

public class RssiComparator implements Comparator<BleDevice> {

    @Override
    public int compare(BleDevice device1, BleDevice device2) {
        return device2.getRssi() - device1.getRssi();
    }

}
