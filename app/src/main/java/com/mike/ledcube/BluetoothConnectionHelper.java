package com.mike.ledcube;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.WriteType;

import java.util.UUID;

public class BluetoothConnectionHelper {
    private final UUID SERVICE_UUID;

    private final MutableLiveData<Event<char[]>> messagesData = new MutableLiveData<>();
    private final MutableLiveData<ConnectionStatus> connectionStatusData = new MutableLiveData<>();

    private final String deviceAddress;
    private final UUID WRITE_CHARACTERISTIC_UUID;
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value,
                                           @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status) {
            super.onCharacteristicUpdate(peripheral, value, characteristic, status);
            if (status == GattStatus.SUCCESS) {
                messagesData.postValue(new Event<>(new String(value).toCharArray()));
            }
        }
    };
    BluetoothCentralManager central;

    enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public BluetoothConnectionHelper(Context context, String deviceAddress) {
        this.deviceAddress = deviceAddress;
        BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
            @Override
            public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral) {
                super.onConnectedPeripheral(peripheral);
                connectionStatusData.postValue(ConnectionStatus.CONNECTED);
                BluetoothGattCharacteristic readChar = peripheral.getCharacteristic(UUID.fromString(context.getString(R.string.SERVICE_UUID)),
                        UUID.fromString(context.getString(R.string.READ_CHARACTERISTIC_UUID)));
                if (readChar != null) {
                    peripheral.setNotify(readChar, true);
                }
            }

            @Override
            public void onConnectingPeripheral(@NonNull BluetoothPeripheral peripheral) {
                super.onConnectingPeripheral(peripheral);
                connectionStatusData.postValue(ConnectionStatus.CONNECTING);
            }

            @Override
            public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull HciStatus status) {
                super.onConnectionFailed(peripheral, status);
                connectionStatusData.postValue(ConnectionStatus.DISCONNECTED);
            }
        };
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
        SERVICE_UUID = UUID.fromString(context.getString(R.string.SERVICE_UUID));
        WRITE_CHARACTERISTIC_UUID = UUID.fromString(context.getString(R.string.WRITE_CHARACTERISTIC_UUID));
        connect();
        connectionStatusData.postValue(ConnectionStatus.DISCONNECTED);
    }


    public boolean send(char[] data) {
        return central.getPeripheral(deviceAddress).writeCharacteristic(SERVICE_UUID, WRITE_CHARACTERISTIC_UUID,
                new String(data).getBytes(), WriteType.WITH_RESPONSE);//maybe without_response
    }

    public void connect() {
        central.autoConnectPeripheral(central.getPeripheral(deviceAddress), peripheralCallback);
    }

    public void disconnect() {
        central.cancelConnection(central.getPeripheral(deviceAddress));
    }

    public LiveData<Event<char[]>> getMessages() {
        return messagesData;
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatusData;
    }
}

