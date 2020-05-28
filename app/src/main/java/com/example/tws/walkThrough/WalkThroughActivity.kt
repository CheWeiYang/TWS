package com.example.tws.walkThrough

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tws.R
import kotlinx.android.synthetic.main.activity_walkthrough.*

class WalkThroughActivity : AppCompatActivity(), WalkThroughFragment.PageCallback {

    private val viewList = ArrayList<WalkThroughFragment>()

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walkthrough)
        val list: List<Int> = listOf(1, 2, 3)
        when {
            list.isNotEmpty() -> {
                Log.d(TAG, "WalkThrough Url Size = ${list.size}")
                list.forEachIndexed { index, url ->
                    val image = when (index) {
                        0 -> R.mipmap.walkthrough_01
                        1 -> R.mipmap.walkthrough_02
                        2 -> R.mipmap.walkthrough_03
                        else -> R.mipmap.walkthrough_01
                    }
                    val walkThroughFragment = WalkThroughFragment.newInstance(index, image)
                    walkThroughFragment.setPageCallback(this)
                    viewList.add(walkThroughFragment)
                }
                pager.adapter = WalkThroughPagerAdapter(viewList, supportFragmentManager)
                pager.offscreenPageLimit = viewList.size
                dots_indicator.setViewPager(pager)
            }
        }

    }


    override fun onClickEvent() {
        if (pager != null && pager.adapter != null) {
            val pageNum = pager.adapter!!.count
            val currentItem = pager.currentItem
            if (currentItem < pageNum - 1) {
                pager.currentItem = currentItem + 1
            } else {
                onBackPressed()
            }
        }
    }

    companion object {
        private val TAG: String = WalkThroughActivity::class.java.simpleName
        private const val AUTO_PLAY_INTERVAL: Long = 8000  //自動播放-間隔時間:8秒
    }
}