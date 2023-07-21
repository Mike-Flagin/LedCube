package com.mike.ledcube;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

public class BluetoothConnectionHelper {
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

    private final MutableLiveData<Event<char[]>> messagesData = new MutableLiveData<>();
    private final MutableLiveData<ConnectionStatus> connectionStatusData = new MutableLiveData<>();

    private final String deviceName;
    private final String deviceAddress;

    private InputStream inputStream;
    private OutputStream outputStream;

    enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public BluetoothConnectionHelper(Context context, String deviceName, String deviceAddress) {
        bluetoothAdapter = Objects.requireNonNull(getSystemService(context, BluetoothManager.class)).getAdapter();

        // Remember the configuration
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;

        // Tell the activity we are disconnected.
        connectionStatusData.postValue(ConnectionStatus.DISCONNECTED);
    }

    private void onConnected(){
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Error occurred when creating output stream", e);
            return;
        }
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Error occurred when creating input stream", e);
            return;
        }

        new Thread(() -> {
            while (true) {
                try {
                    byte[] mBuffer = new byte[64];
                    int numBytes = inputStream.read(mBuffer);
                    if(numBytes > 0) messagesData.postValue(new Event<>(new String(mBuffer).toCharArray()));
                } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), "Input stream was disconnected", e);
                    break;
                }
            }
        }).start();
    }

    public boolean send(char[] data) {
        try {
            if (outputStream == null) return false;
            outputStream.write(new String(data).getBytes());
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Error occurred when sending data", e);
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void connect() {
        try {
            socket = bluetoothAdapter.getRemoteDevice(deviceAddress).createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Socket's create() method failed", e);
        }
        new Thread(() -> {
            bluetoothAdapter.cancelDiscovery();

            try {
                connectionStatusData.postValue(ConnectionStatus.CONNECTING);
                socket.connect();
            } catch (IOException connectException) {
                try {
                    socket.close();
                    connectionStatusData.postValue(ConnectionStatus.DISCONNECTED);
                } catch (IOException closeException) {
                    Log.e(getClass().getSimpleName(), "Could not close the client socket", closeException);
                }
                return;
            }

            connectionStatusData.postValue(ConnectionStatus.CONNECTED);
            onConnected();
        }).start();
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Could not close the client socket", e);
        }
    }

    public LiveData<Event<char[]>> getMessages() {
        return messagesData;
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatusData;
    }
}

