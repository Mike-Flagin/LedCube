package com.mike.ledcube.Dialogs;

import static com.mike.ledcube.CubeCommunication.BluetoothCommands.getColorComponent;
import static com.mike.ledcube.MainActivity.BLHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mike.ledcube.CubeCommunication.BluetoothCommands;
import com.mike.ledcube.CubeCommunication.EffectTypes;
import com.mike.ledcube.R;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

public class FireEffectPreferencesActivity extends AppCompatActivity {
    Button startColorButton;
    Button endColorButton;
    SeekBar speedSeekbar;
    Button cancelButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fire_dialog_layout);

        startColorButton = findViewById(R.id.fire_start_color_button);
        endColorButton = findViewById(R.id.fire_end_color_button);
        speedSeekbar = findViewById(R.id.fire_speed);

        Setup();
        cancelButton = findViewById(R.id.fire_cancel_button);
        cancelButton.setOnClickListener((view) -> finish());
        startButton = findViewById(R.id.fire_ok_button);
        startButton.setOnClickListener((view) ->{
            SharedPreferences pref = getSharedPreferences(getString(R.string.fire_preferences), MODE_PRIVATE);
            Color start = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.fire_start_color), "#FFFF00")));
            Color end = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.fire_end_color), "#FF0000")));
            String[] params = new String[7];
            params[0] = getColorComponent(start.red());
            params[1] = getColorComponent(start.green());
            params[2] = getColorComponent(start.blue());
            params[3] = getColorComponent(end.red());
            params[4] = getColorComponent(end.green());
            params[5] = getColorComponent(end.blue());
            params[6] = String.valueOf(speedSeekbar.getProgress());
            if (!BLHelper.send(BluetoothCommands.getEffectCommand(EffectTypes.Fire, params))) {
                Toast.makeText(this, R.string.error_not_send, Toast.LENGTH_LONG).show();
            }
            finish();
        });
    }

    private void Setup() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.fire_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String startColor = sharedPref.getString(getString(R.string.fire_start_color), "#FFFF00");
        String endColor = sharedPref.getString(getString(R.string.fire_end_color), "#FF0000");
        int speed = sharedPref.getInt(getString(R.string.fire_speed), 127);

        startColorButton.setBackgroundColor(Color.parseColor(startColor));
        startColorButton.setOnClickListener((view -> new ColorPickerDialog()
                .withColor(Color.parseColor(startColor))
                .withAlphaEnabled(false)
                .withCornerRadius(20)
                .withListener((dialog, color) -> {
                    editor.putString(getString(R.string.fire_start_color), String.format("#%06X", (0xFFFFFF & color)));
                    editor.apply();
                    startColorButton.setBackgroundColor(color);
                })
                .show(getSupportFragmentManager(), "startColorPicker")));

        endColorButton.setBackgroundColor(Color.parseColor(endColor));
        endColorButton.setOnClickListener((view -> new ColorPickerDialog()
                .withColor(Color.parseColor(endColor))
                .withAlphaEnabled(false)
                .withCornerRadius(20)
                .withListener((dialog, color) -> {
                    editor.putString(getString(R.string.fire_end_color), String.format("#%06X", (0xFFFFFF & color)));
                    editor.apply();
                    endColorButton.setBackgroundColor(color);
                })
                .show(getSupportFragmentManager(), "endColorPicker")));
        speedSeekbar.setProgress(speed);
        speedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editor.putInt(getString(R.string.fire_speed), seekBar.getProgress());
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}