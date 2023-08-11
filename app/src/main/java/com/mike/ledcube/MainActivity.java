package com.mike.ledcube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TextView connectionTextView;

    private Button connectButton;
    public static BluetoothConnectionHelper BLHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager2 viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        TabLayout tabLayout = findViewById(R.id.main_tablayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.main));
                    break;
                case 1:
                    tab.setText(getString(R.string.effects));
                    break;
                case 2:
                    tab.setText(getString(R.string.games));
                    break;
            }
        }).attach();

        connectionTextView = findViewById(R.id.statusTextView);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener((vw) -> BLHelper.connect());
        Button disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener((vw) -> {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.bluetooth_device_address), getString(R.string.string_null));
            editor.apply();
            Intent intent = new Intent(this, ChooseDeviceActivity.class);
            startActivity(intent);
            finish();
        });

        BLHelper = new BluetoothConnectionHelper(this, getIntent().getStringExtra("device_mac"));

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

    public static class MainViewPagerAdapter extends FragmentStateAdapter {
        private final Fragment[] fragments = new Fragment[3];

        public MainViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
            fragments[0] = new MainFragment();
            fragments[1] = new EffectsFragment();
            fragments[2] = new GamesFragment();
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }
    }
}