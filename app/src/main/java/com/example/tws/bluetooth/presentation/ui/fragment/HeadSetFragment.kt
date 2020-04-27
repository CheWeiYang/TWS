package com.example.tws.bluetooth.presentation.ui.fragment

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.tws.R
import com.example.tws.bluetooth.presentation.HeadSetViewModel
import kotlinx.android.synthetic.main.headset_fragment.*
import java.io.IOException

class HeadSetFragment : Fragment() {

    var bluetoothHeadset: BluetoothHeadset? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "")
        val filter = IntentFilter()
        filter.addCategory(
            BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY
                    + "." + BluetoothAssignedNumbers.APPLE
        )
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        activity!!.registerReceiver(receiver, filter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.headset_fragment, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        headsetViewModel = ViewModelProviders.of(this).get(HeadSetViewModel::class.java)

        headsetViewModel.getToast().observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        headsetViewModel.getHeadSetStatus().observe(viewLifecycleOwner, Observer {
            status.text = it
        })

        headsetViewModel.getHeadSetName().observe(viewLifecycleOwner, Observer {
            headset.text = "$it"
        })

        headsetViewModel.getBluetoothAdapterEnable().observe(viewLifecycleOwner, Observer {
            when (it) {
                false -> {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, 1)
                }
            }
        })

        headsetViewModel.getNeedFindDevice().observe(viewLifecycleOwner, Observer {
            findDevice()
        })

        headsetViewModel.getHeadsetAddress().observe(viewLifecycleOwner, Observer {
            val connect = ConnectBluetoothTask()
            blueAddress = it
            Log.d(TAG, "getHeadset Address call back : $it")
            connect.execute()
        })
        // 測試連線，還不確定有沒有用
        openBluetoothHeadsetConnect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "")
        connect.setOnClickListener {
            headsetViewModel.setBlueToothData()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }

    private fun findDevice() {
        Log.d(TAG, "Start Find Device")
        val isStartDiscovery = bluetoothAdapter?.startDiscovery()
        Log.d(TAG, "isStartDiscovery : $isStartDiscovery")


    }

    private fun openBluetoothHeadsetConnect() {

        val profileListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile == BluetoothProfile.HEADSET) {
                    bluetoothHeadset = proxy as BluetoothHeadset
                    Log.d(TAG, "ProfileListener Call Back")
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HEADSET) {
                    bluetoothHeadset = null
                }
            }
        }
        bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)

    }


    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, "Start onReceive ")
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "ACTION_FOUND")
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    headset.text = "$deviceName 藍芽耳機"
                }
                BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT -> {
                    Log.d(TAG, "BluetoothHeadset進來了")
                    val command =
                        intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)
                    Log.d(TAG, "Headset Command :$command")
                    if ("+IPHONEACCEV" == command) {
                        Log.d(TAG, "Battery Information Get")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "ACTION_DISCOVERY_FINISHED")
                }
            }
        }
    }

    private class ConnectBluetoothTask : AsyncTask<Void, Void, Boolean>() {


        override fun doInBackground(vararg p0: Void?): Boolean {
            var result = false

            Log.d(TAG, "doInBackground Address :$blueAddress")
            try {
//                val tDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(blueAddress)
//                val socket =
//                    tDevice.createInsecureRfcommSocketToServiceRecord(tDevice.uuids[0].uuid)
//                socket.connect()
//
//                Log.d(TAG, "UUID : " + tDevice.uuids[0].uuid)
//                val inputStream = socket?.inputStream
//                val outputStream = socket?.outputStream
//
//                if (inputStream != null && outputStream != null) {
//                    //代表連線已經成功
//                    bluetoothSocket = socket
//                    result = true
//                    //socket.close()
//                }
                val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(blueAddress)!!

                val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                    device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                }
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                mmSocket?.use { socket ->
                    socket.connect()
                }

                val inputStream = mmSocket?.inputStream
                val outputStream = mmSocket?.outputStream

                if (inputStream != null && outputStream != null) {
                    //代表連線已經成功
                    bluetoothSocket = mmSocket
                    result = true
                    //socket.close()
                }


            } catch (e: IOException) {
                Log.d(TAG, "IOException :　" + e.message)
            }
            return result
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Log.d(TAG, "Connect is Success")
                headsetViewModel.changeHeadSetStatus("已連線")
            } else {
                Log.d(TAG, "Connect is Fail")
                headsetViewModel.changeHeadSetStatus("連線失敗")
            }
        }

    }


    companion object {
        val TAG = HeadSetFragment::class.java.simpleName
        var blueAddress: String? = null
        var bluetoothSocket: BluetoothSocket? = null

        private lateinit var headsetViewModel: HeadSetViewModel

        fun newInstance() = HeadSetFragment()

    }

}