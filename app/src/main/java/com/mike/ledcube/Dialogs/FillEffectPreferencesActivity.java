package com.mike.ledcube.Dialogs;

import static com.mike.ledcube.CubeCommunication.BluetoothCommands.getColorComponent;
import static com.mike.ledcube.MainActivity.BLHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mike.ledcube.CubeCommunication.BluetoothCommands;
import com.mike.ledcube.CubeCommunication.EffectTypes;
import com.mike.ledcube.R;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

public class FillEffectPreferencesActivity extends AppCompatActivity {
    Button fillButton;
    Button cancelButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fill_dialog_layout);

        fillButton = findViewById(R.id.fill_button);

        Setup();
        cancelButton = findViewById(R.id.fill_cancel_button);
        cancelButton.setOnClickListener((view) -> finish());
        startButton = findViewById(R.id.fill_ok_button);
        startButton.setOnClickListener((view) ->{
            SharedPreferences pref = getSharedPreferences(getString(R.string.fill_preferences), MODE_PRIVATE);
            Color fill = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.fill_color), "#FFFFFF")));
            String[] params = new String[3];
            params[0] = getColorComponent(fill.red());
            params[1] = getColorComponent(fill.green());
            params[2] = getColorComponent(fill.blue());
            if (!BLHelper.send(BluetoothCommands.getEffectCommand(EffectTypes.Fill, params))) {
                Toast.makeText(this, R.string.error_not_send, Toast.LENGTH_LONG).show();
            }
            finish();
        });
    }

    private void Setup() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.fill_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String fillColor = sharedPref.getString(getString(R.string.fill_color), "#FFFFFF");

        fillButton.setBackgroundColor(Color.parseColor(fillColor));
        fillButton.setOnClickListener((view -> new ColorPickerDialog()
                .withColor(Color.parseColor(fillColor))
                .withAlphaEnabled(false)
                .withCornerRadius(20)
                .withListener((dialog, color) -> {
                    editor.putString(getString(R.string.fill_color), String.format("#%06X", (0xFFFFFF & color)));
                    editor.apply();
                    fillButton.setBackgroundColor(color);
                })
                .show(getSupportFragmentManager(), "fillColorPicker")));
    }
}
