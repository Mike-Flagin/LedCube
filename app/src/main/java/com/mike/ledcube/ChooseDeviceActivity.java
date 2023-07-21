package com.mike.ledcube;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ChooseDeviceActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.bluetooth_required, Toast.LENGTH_LONG).show();
                }
            });

    private BluetoothAdapter bluetoothAdapter;
    private final MutableLiveData<Collection<BluetoothDevice>> pairedDeviceList = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);

        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }

        bluetoothAdapter = getSystemService(BluetoothManager.class).getAdapter();
        RecyclerView deviceList = findViewById(R.id.devices_recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        deviceList.setLayoutManager(new LinearLayoutManager(this));
        DeviceAdapter adapter = new DeviceAdapter();
        deviceList.setAdapter(adapter);

        // Setup the SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Collection<BluetoothDevice> pairedDev = new ArrayList<>(bluetoothAdapter.getBondedDevices());
            pairedDeviceList.postValue(pairedDev);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Start observing the data sent to us by the ViewModel
        getPairedDeviceList().observe(this, adapter::updateList);

        // Immediately refresh the paired devices list
        refreshPairedDevices();

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        String deviceName = sharedPref.getString(getString(R.string.bluetooth_device_name),  getString(R.string.string_null));
        String deviceAddress = sharedPref.getString(getString(R.string.bluetooth_device_address),  getString(R.string.string_null));
        if(!deviceName.equals(getString(R.string.string_null)) && !deviceAddress.equals(getString(R.string.string_null)) && bluetoothAdapter.getBondedDevices().stream().anyMatch((el) -> el.getAddress().equals(deviceAddress))){
            openMainActivity(deviceName, deviceAddress);
        }
    }

    private void refreshPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
        Collection<BluetoothDevice> pairedDev = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        pairedDeviceList.postValue(pairedDev);
    }

    private LiveData<Collection<BluetoothDevice>> getPairedDeviceList() {
        return pairedDeviceList;
    }

    private class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout layout;
        private final TextView text1;
        private final TextView text2;

        DeviceViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.list_item);
            text1 = view.findViewById(R.id.list_item_text1);
            text2 = view.findViewById(R.id.list_item_text2);
        }

        void setupView(BluetoothDevice device) {
            if (ActivityCompat.checkSelfPermission(ChooseDeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
            text1.setText(device.getName());
            text2.setText(device.getAddress());
            layout.setOnClickListener(view -> {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                SharedPreferences sharedPref = ChooseDeviceActivity.this.getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.bluetooth_device_name), deviceName);
                editor.putString(getString(R.string.bluetooth_device_address), deviceAddress);
                editor.apply();
                openMainActivity(deviceName, deviceAddress);
            });
        }
    }

    private void openMainActivity(String deviceName, String address) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("device_name", deviceName);
        intent.putExtra("device_mac", address);
        startActivity(intent);
        finish();
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
        private BluetoothDevice[] deviceList = new BluetoothDevice[0];

        @NotNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new DeviceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull DeviceViewHolder holder, int position) {
            holder.setupView(deviceList[position]);
        }

        @Override
        public int getItemCount() {
            return deviceList.length;
        }

        void updateList(Collection<BluetoothDevice> deviceList) {
            this.deviceList = deviceList.toArray(new BluetoothDevice[0]);
            notifyDataSetChanged();
        }
    }
}