package com.example.tws.bluetoothble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tws.R;

import java.util.ArrayList;
import java.util.Collections;

public class BleDeviceAdapter extends RecyclerView.Adapter {

    private final static String TAG = BleDeviceAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<BleDevice> mList;
    private AdapterListener adapterListener = null;

    public void setupAdapterListener(AdapterListener listener) {
        this.adapterListener = listener;
    }

    public interface AdapterListener {
        void onSelect(BleDevice device);
    }

    public BleDeviceAdapter(Context context, ArrayList<BleDevice> devices) {
        mContext = context;
        mList = devices;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(mInflater.inflate(R.layout.adapter_device_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof DeviceViewHolder) {
            DeviceViewHolder viewHolder = (DeviceViewHolder) holder;
            final BleDevice bleDevice = mList.get(position);
            BluetoothDevice device = bleDevice.getDevice();
            String deviceAddress = device.getAddress();
            viewHolder.textViewName.setText(device.getName());
            viewHolder.textViewAddress.setText(deviceAddress);
            viewHolder.textViewRssi.setText("" + bleDevice.getRssi());
            viewHolder.layoutFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "layoutFrame click, bleDevice: " + bleDevice);
                    if (adapterListener != null) {
                        adapterListener.onSelect(bleDevice);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public void refreshDevice(ArrayList<BleDevice> devices) {
        mList = devices;
        Collections.sort(mList, new RssiComparator());
        notifyDataSetChanged();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout layoutFrame;
        protected TextView textViewName;
        protected TextView textViewAddress;
        protected TextView textViewRssi;

        public DeviceViewHolder(View itemView) {
            super(itemView);

            this.layoutFrame = itemView.findViewById(R.id.itemBleDevice);
            this.textViewName = itemView.findViewById(R.id.itemBleDevice_name);
            this.textViewAddress = itemView.findViewById(R.id.itemBleDevice_address);
            this.textViewRssi = itemView.findViewById(R.id.itemBleDevice_rssi);
        }

    }




}
