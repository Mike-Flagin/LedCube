package com.mike.ledcube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView connectionTextView;

    private Button connectButton;
    public static BluetoothConnectionHelper BLHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView))).getNavController();
        NavigationUI.setupWithNavController(navigationView, navController);

        connectionTextView = findViewById(R.id.statusTextView);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener((vw) -> BLHelper.connect());
        Button disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener((vw) -> {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.bluetooth_device_name), getString(R.string.string_null));
            editor.putString(getString(R.string.bluetooth_device_address), getString(R.string.string_null));
            editor.apply();
            Intent intent = new Intent(this, ChooseDeviceActivity.class);
            startActivity(intent);
            finish();
        });

        BLHelper = new BluetoothConnectionHelper(this, getIntent().getStringExtra("device_name"), getIntent().getStringExtra("device_mac"));

        BLHelper.getConnectionStatus().observe(this, this::onConnectionStatus);
        BLHelper.connect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLHelper.disconnect();
    }

    private void onConnectionStatus(BluetoothConnectionHelper.ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:
                connectionTextView.setText(R.string.status_connected);
                connectionTextView.setTextColor(getColor(R.color.green));
                connectButton.setEnabled(false);
                break;

            case CONNECTING:
                connectionTextView.setText(R.string.status_connecting);
                connectionTextView.setTextColor(getColor(R.color.yellow));
                break;

            case DISCONNECTED:
                connectionTextView.setText(R.string.status_disconnected);
                connectionTextView.setTextColor(getColor(R.color.red));
                connectButton.setEnabled(true);
                break;
        }
    }
}