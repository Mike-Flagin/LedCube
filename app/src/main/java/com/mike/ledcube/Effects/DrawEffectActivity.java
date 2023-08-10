package com.mike.ledcube.Effects;

import static com.mike.ledcube.CubeCommunication.BluetoothCommands.getColorComponent;
import static com.mike.ledcube.MainActivity.BLHelper;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mike.ledcube.CubeCommunication.BluetoothCommands;
import com.mike.ledcube.CubeCommunication.EffectTypes;
import com.mike.ledcube.R;

public class DrawEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_effect);

        CubeDrawView view = findViewById(R.id.draw_field);
        view.setDimensions(8, 8, 8, getSupportFragmentManager());
        view.cellChangedListener = (layer, row, column, field) -> {
            String[] params = new String[6];
            params[0] = String.valueOf(layer);
            params[1] = String.valueOf(row);
            params[2] = String.valueOf(column);
            params[3] = getColorComponent(Color.valueOf(field.getCell(row, column)).red());
            params[4] = getColorComponent(Color.valueOf(field.getCell(row, column)).green());
            params[5] = getColorComponent(Color.valueOf(field.getCell(row, column)).blue());
            if (!BLHelper.send(BluetoothCommands.getEffectCommand(EffectTypes.Draw, params))) {
                Toast.makeText(this, R.string.error_not_send, Toast.LENGTH_LONG).show();
                finish();
            }
        };
        view.fieldClearedListener = (layer) -> {
            String[] params = new String[2];
            params[0] = String.valueOf(9);
            params[1] = String.valueOf(layer);
            if (!BLHelper.send(BluetoothCommands.getEffectCommand(EffectTypes.Draw, params))) {
                Toast.makeText(this, R.string.error_not_send, Toast.LENGTH_LONG).show();
                finish();
            }
        };
    }
}