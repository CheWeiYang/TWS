package com.example.tws.bluetooth.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HeadSetViewModel : ViewModel() {

    private val toast = MutableLiveData<String?>()

    private val headSetStatus = MutableLiveData<String?>()

    private val headSetName = MutableLiveData<String?>()

    private val headsetAddress = MutableLiveData<String?>()

    private val blueToothEnable = MutableLiveData<Boolean?>()

    private val needFindDevice = MutableLiveData<Boolean?>()

    fun getToast() = toast

    fun getHeadSetStatus() = headSetStatus

    fun getHeadSetName() = headSetName

    fun getHeadsetAddress() = headsetAddress

    fun getBluetoothAdapterEnable() = blueToothEnable

    fun getNeedFindDevice() = needFindDevice

    fun changeHeadSetStatus(status: String) {
        headSetStatus.postValue(status)
    }

    fun setBlueToothData() {

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.d(TAG, "bluetoothAdapter: Device doesn't support Bluetooth")
        }

        if (bluetoothAdapter?.isEnabled == false) {
            blueToothEnable.postValue(false)
        } else {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            var isGetDevice = false
            pairedDevices?.forEach { device ->
                if (device.name == "T200" || device.name == "AirPods") {
                    headSetName.postValue(device.name)
                    headSetStatus.postValue("正在連接")
                    Log.d(TAG, "MAC Address" + device.address)
                    isGetDevice = true
                    headsetAddress.postValue(device.address)
                }
            }
            if (isGetDevice) {
                //若有抓取成功這邊無須動作
            } else {
                //若無抓取任何以配對裝置，代表沒有配對過 則需進行裝置搜索
                needFindDevice.postValue(true)
            }

        }

    }

    companion object {
        private val TAG = HeadSetViewModel::class.java.simpleName


    }


}