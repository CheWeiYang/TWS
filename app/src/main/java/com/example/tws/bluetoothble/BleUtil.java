package com.example.tws.bluetoothble;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class BleUtil {

    // 取得目前藍芽連線狀態
    public static int bleConnectStatus = BleUtil.BLE_NOT_CONNECT;
    public final static int BLE_NOT_CONNECT = 0;
    public final static int BLE_CONNECTING = 1;
    public final static int BLE_CONNECTED = 2;


    public static String getAppVersion(Context context) {
        String version = "0";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
               // hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }


}
