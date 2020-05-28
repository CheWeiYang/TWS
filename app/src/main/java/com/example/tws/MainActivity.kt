package com.example.tws

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.tws.bluetoothble.BleDevice
import com.example.tws.bluetoothble.BleDeviceFragment
import com.example.tws.bluetoothble.DeviceInfoFragment
import com.example.tws.ui.main.MainFragment
import android.content.Intent
import android.view.animation.TranslateAnimation
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false
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

            var anim = AnimationUtils.loadAnimation(applicationContext,R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    // HomeActivity.class is the activity to go after showing the splash screen.
                    when (permissionCheck) {
                        0 -> {
                            //表示已給予權限
                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.from_right,R.anim.out_left)
                                .add(R.id.container, BleDeviceFragment())
                                .commit()
                        }
                        -1 -> {
                            //還未給予權限
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.container, MainFragment.newInstance())
                                .commit()
                        }
                    }
                }
                override fun onAnimationRepeat(animation: Animation) {}
            })
            animationView.startAnimation(anim)
        }
    }

    override fun onBackPressed() {
        val entryCount = supportFragmentManager.backStackEntryCount
        Log.d(TAG, "onBackPressed entryCount: $entryCount")
        if (entryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            exitHandel()
        }
    }

    private fun exitHandel() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "再按一次退出!", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
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
