package com.mike.ledcube;

import static com.mike.ledcube.MainActivity.BLHelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mike.ledcube.CubeCommunication.BluetoothCommands;

public class MainFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private SeekBar brightnessSeekbar;
    private TextView brightnessTextView;
    private SwitchMaterial onOffSwitch;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        brightnessSeekbar = requireView().findViewById(R.id.brightness_seekBar);
        brightnessTextView = requireView().findViewById(R.id.brightness_textView);
        brightnessSeekbar.setOnSeekBarChangeListener(this);
        onOffSwitch = requireView().findViewById(R.id.on_off_switch);
        onOffSwitch.setOnCheckedChangeListener(this);

        brightnessTextView.setText(getString(R.string.brightness, 0));

        BLHelper.getConnectionStatus().observe(getViewLifecycleOwner(), this::onConnectionStatus);
        BLHelper.getMessages().observe(getViewLifecycleOwner(), this::onMessage);
    }

    private void GetAllPreferences() {
        BLHelper.send(BluetoothCommands.getStateCommand());
        BLHelper.send(BluetoothCommands.getBrightnessCommand());
    }

    private void onMessage(Event<char[]> message) {
        String[] msg = BluetoothCommands.parseCommand(message.getContentIfNotHandled());
        if (msg != null) {
            //pref
            if (msg[0].equals("0")) {
                //state
                if (msg[1].equals("0")) {
                    onOffSwitch.setChecked(msg[2].equals("1"));
                }
                //brightness
                else if (msg[1].equals("1")) {
                    int brightness = Integer.parseInt(msg[2]);
                    brightnessTextView.setText(getString(R.string.brightness, brightness));
                    brightnessSeekbar.setProgress(brightness, true);
                }
            }
        }
    }

    private void onConnectionStatus(BluetoothConnectionHelper.ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:
                if (brightnessSeekbar.getProgress() == 0) {
                    GetAllPreferences();
                }
                break;
            case CONNECTING:
                break;
            case DISCONNECTED:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        brightnessTextView.setText(getString(R.string.brightness, progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (!BLHelper.send(BluetoothCommands.setBrightnessCommand(seekBar.getProgress()))) {
            Toast.makeText(requireContext(), R.string.error_not_send, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!BLHelper.send(b ? BluetoothCommands.onCommand() : BluetoothCommands.offCommand())) {
            Toast.makeText(requireContext(), R.string.error_not_send, Toast.LENGTH_LONG).show();
        }
    }
}