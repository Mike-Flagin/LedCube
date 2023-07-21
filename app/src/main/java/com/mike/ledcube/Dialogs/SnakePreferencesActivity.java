package com.mike.ledcube.Dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.mike.ledcube.Games.SnakeGameActivity;
import com.mike.ledcube.R;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

public class SnakePreferencesActivity extends AppCompatActivity {
    Spinner difficultySpinner;
    Button headButton;
    Button bodyButton;
    Button foodButton;
    SwitchCompat mirrorSwitch;
    Button cancelButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snake_dialog_layout);

        difficultySpinner = findViewById(R.id.snake_difficulty_spinner);
        headButton = findViewById(R.id.snake_head_button);
        bodyButton = findViewById(R.id.snake_body_button);
        foodButton = findViewById(R.id.snake_food_button);
        mirrorSwitch = findViewById(R.id.snake_mirror_switch);

        Setup();
        cancelButton = findViewById(R.id.snake_cancel_button);
        cancelButton.setOnClickListener((view) ->{
            finish();
        });
        startButton = findViewById(R.id.snake_ok_button);
        startButton.setOnClickListener((view) ->{
            startActivity(new Intent(this, SnakeGameActivity.class));
            finish();
        });
    }

    private void Setup() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.snake_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String headColor = sharedPref.getString(getString(R.string.snake_head_color), "#FF0000");
        String bodyColor = sharedPref.getString(getString(R.string.snake_body_color), "#00FF00");
        String foodColor = sharedPref.getString(getString(R.string.snake_food_color), "#FFFF00");
        int difficulty = sharedPref.getInt(getString(R.string.snake_difficulty), 0);
        boolean mirror = sharedPref.getBoolean(getString(R.string.snake_mirror), false);

        headButton.setBackgroundColor(Color.parseColor(headColor));
        bodyButton.setBackgroundColor(Color.parseColor(bodyColor));
        foodButton.setBackgroundColor(Color.parseColor(foodColor));
        difficultySpinner.setSelection(difficulty);
        mirrorSwitch.setChecked(mirror);

        //createListeners
        headButton.setOnClickListener((view -> {
            new ColorPickerDialog()
                    .withColor(Color.parseColor(headColor))
                    .withAlphaEnabled(false)
                    .withCornerRadius(20)
                    .withListener((dialog, color) -> {
                        editor.putString(getString(R.string.snake_head_color), String.format("#%06X", (0xFFFFFF & color)));
                        editor.apply();
                        headButton.setBackgroundColor(color);
                    })
                    .show(getSupportFragmentManager(), "snakeHeadColorPicker");
        }));
        bodyButton.setOnClickListener((view -> {
            new ColorPickerDialog()
                    .withColor(Color.parseColor(bodyColor))
                    .withAlphaEnabled(false)
                    .withCornerRadius(20)
                    .withListener((dialog, color) -> {
                        editor.putString(getString(R.string.snake_body_color), String.format("#%06X", (0xFFFFFF & color)));
                        editor.apply();
                        bodyButton.setBackgroundColor(color);
                    })
                    .show(getSupportFragmentManager(), "snakeBodyColorPicker");
        }));
        foodButton.setOnClickListener((view -> {
            new ColorPickerDialog()
                    .withColor(Color.parseColor(foodColor))
                    .withAlphaEnabled(false)
                    .withCornerRadius(20)
                    .withListener((dialog, color) -> {
                        editor.putString(getString(R.string.snake_food_color), String.format("#%06X", (0xFFFFFF & color)));
                        editor.apply();
                        foodButton.setBackgroundColor(color);
                    })
                    .show(getSupportFragmentManager(), "snakeFoodColorPicker");
        }));
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt(getString(R.string.snake_difficulty), position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mirrorSwitch.setOnClickListener(view -> {
            editor.putBoolean(getString(R.string.snake_mirror), ((SwitchCompat) view).isChecked());
            editor.apply();
        });
    }
}
