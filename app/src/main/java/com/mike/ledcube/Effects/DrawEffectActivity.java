package com.mike.ledcube.Effects;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mike.ledcube.R;

public class DrawEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_effect);

        CubeDrawView view = findViewById(R.id.draw_field);
        view.setDimensions(8, 8, 8, getSupportFragmentManager());
        view.cellChangedListener = (layer, row, column, field) -> {
            //send
        };

    }
}