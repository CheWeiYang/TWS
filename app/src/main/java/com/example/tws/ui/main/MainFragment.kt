package com.example.tws.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.example.tws.MainActivity
import com.example.tws.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private val manager by lazy {
        activity?.supportFragmentManager
    }

    companion object {
        val TAG = MainFragment::class.java.simpleName
        const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commit.setOnClickListener {
            ActivityCompat.requestPermissions(
                activity as MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )
        }
    }

}
