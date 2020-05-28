package com.example.tws

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_about, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_view_version.text = String.format(getString(R.string.current_version),context?.packageManager?.getPackageInfo(context?.packageName,0)?.versionName)
    }

    companion object {

        private val TAG = AboutFragment::class.java.simpleName

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

}