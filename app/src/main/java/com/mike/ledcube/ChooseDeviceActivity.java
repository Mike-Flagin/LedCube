package com.mike.ledcube;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChooseDeviceActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.bluetooth_required, Toast.LENGTH_LONG).show();
                }
            });
    private final MutableLiveData<BluetoothPeripheral> availableDeviceList = new MutableLiveData<>();

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
            availableDeviceList.postValue(peripheral);
        }
    };
    BluetoothCentralManager central;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);

        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }
        central = new BluetoothCentralManager(getApplicationContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        String deviceAddress = sharedPref.getString(getString(R.string.bluetooth_device_address), getString(R.string.string_null));
        if (!deviceAddress.equals(getString(R.string.string_null))) {
            openMainActivity(deviceAddress);
        }

        RecyclerView deviceList = findViewById(R.id.devices_recyclerView);

        deviceList.setLayoutManager(new LinearLayoutManager(this));
        DeviceAdapter adapter = new DeviceAdapter();
        deviceList.setAdapter(adapter);

        getAvailableDeviceList().observe(this, adapter::updateList);

        central.scanForPeripheralsWithNames(new String[]{getString(R.string.bluetooth_device_name)});
    }

    private LiveData<BluetoothPeripheral> getAvailableDeviceList() {
        return availableDeviceList;
    }

    private void openMainActivity(String address) {
        central.stopScan();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("device_mac", address);
        startActivity(intent);
        finish();
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

        void setupView(BluetoothPeripheral device) {
            if (ActivityCompat.checkSelfPermission(ChooseDeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
            text1.setText(device.getName());
            text2.setText(device.getAddress());
            layout.setOnClickListener(view -> {
                String deviceAddress = device.getAddress();
                SharedPreferences sharedPref = ChooseDeviceActivity.this.getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.bluetooth_device_address), deviceAddress);
                editor.apply();
                openMainActivity(deviceAddress);
            });
        }
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
        private final ArrayList<BluetoothPeripheral> deviceList = new ArrayList<>();

        @NotNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new DeviceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull DeviceViewHolder holder, int position) {
            holder.setupView(deviceList.get(position));
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }

        @SuppressLint("NotifyDataSetChanged")
        void updateList(BluetoothPeripheral device) {
            deviceList.add(device);
            notifyDataSetChanged();
        }
    }
}