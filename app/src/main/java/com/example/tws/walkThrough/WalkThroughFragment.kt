package com.example.tws.walkThrough

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tws.R
import kotlinx.android.synthetic.main.fragment_walkthrough.*

/**
 * Created by DavidYang on 2020/05/27
 */
class WalkThroughFragment : Fragment() {

    private var pageIndex: Int = 0
    private var pageCallBack: PageCallback? = null
    private var defaultImage: Int = R.mipmap.walkthrough_01

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pageIndex = arguments?.getInt(BUNDLE_INDEX) ?: 0
        defaultImage = arguments?.getInt(BUNDLE_IMAGE) ?: R.mipmap.walkthrough_01
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_walkthrough, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView.setImageResource(defaultImage)

        imageView.setOnClickListener {
            pageCallBack?.onClickEvent()
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    interface PageCallback {
        fun onClickEvent()
    }

    fun setPageCallback(callback: PageCallback) {
        pageCallBack = callback
    }


    companion object {

        private val TAG: String = WalkThroughFragment::class.java.simpleName
        private const val BUNDLE_INDEX = "index"
        private const val BUNDLE_URL = "url"
        private const val BUNDLE_IMAGE = "image"

        /**
         * @param pageIndex 頁面順序
         * @param imageUrl 圖片連結
         * @param defaultImage 預設圖片
         * */
        fun newInstance(pageIndex: Int, defaultImage: Int): WalkThroughFragment {
            Log.d(TAG, "newInstance")
            val walkTroughFragment = WalkThroughFragment()
            val args = Bundle()
            args.putInt(BUNDLE_INDEX, pageIndex)
            args.putInt(BUNDLE_IMAGE, defaultImage)
            walkTroughFragment.arguments = args

            return walkTroughFragment
        }
    }

}