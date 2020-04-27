package com.example.tws

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tws.bluetoothble.BleDeviceFragment
import com.example.tws.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            //確認是否有使用者有給予位置權限
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            when (permissionCheck) {
                0 -> {
                    //表示已給予權限
                    supportFragmentManager.beginTransaction().replace(
                        R.id.container,
                        BleDeviceFragment()
                    ).commitNow()
                }
                -1 -> {
                    //還未給予權限
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .commitNow()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MainFragment.MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission come in")
                    supportFragmentManager.beginTransaction().replace(
                        R.id.container,
                        BleDeviceFragment()
                    ).commit()
                } else {
                    return
                }
            }

        }
    }

}
