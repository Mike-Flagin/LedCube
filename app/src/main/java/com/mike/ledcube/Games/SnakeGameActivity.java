package com.mike.ledcube.Games;

import static com.mike.ledcube.MainActivity.BLHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mike.ledcube.CubeCommunication.BluetoothCommands;
import com.mike.ledcube.CubeCommunication.GameTypes;
import com.mike.ledcube.Event;
import com.mike.ledcube.R;

import java.util.Timer;
import java.util.TimerTask;

public class SnakeGameActivity extends AppCompatActivity {
    boolean backPressed = false;
    TextView scoreTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_game);

        if (savedInstanceState == null) {
            SharedPreferences pref = getSharedPreferences(getString(R.string.snake_preferences), MODE_PRIVATE);
            Color head = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.snake_head_color), "#FF0000")));
            Color body = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.snake_body_color), "#00FF00")));
            Color food = Color.valueOf(Color.parseColor(pref.getString(getString(R.string.snake_food_color), "#FFFF00")));
            int difficulty = pref.getInt(getString(R.string.snake_difficulty), 0);
            boolean mirror = pref.getBoolean(getString(R.string.snake_mirror), false);

            String[] params = new String[11];
            params[0] = getColorComponent(head.red());
            params[1] = getColorComponent(head.green());
            params[2] = getColorComponent(head.blue());
            params[3] = getColorComponent(body.red());
            params[4] = getColorComponent(body.green());
            params[5] = getColorComponent(body.blue());
            params[6] = getColorComponent(food.red());
            params[7] = getColorComponent(food.green());
            params[8] = getColorComponent(food.blue());
            params[9] = String.valueOf(difficulty);
            params[10] = mirror ? "1" : "0";

            if (!BLHelper.send(BluetoothCommands.getGameInitializationCommand(GameTypes.Snake, params))) {
                Toast.makeText(this, R.string.error_not_send, Toast.LENGTH_LONG).show();
                finish();
            }
            scoreTextView = findViewById(R.id.snake_score_textview);
            scoreTextView.setText(getString(R.string.score, 0));
        }
        findViewById(R.id.snake_game_layout).setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                BLHelper.send(BluetoothCommands.getCommand('3'));
            }

            public void onSwipeLeft() {
                BLHelper.send(BluetoothCommands.getCommand('2'));
            }

            public void onSwipeTop() {
                BLHelper.send(BluetoothCommands.getCommand('4'));
            }

            public void onSwipeBottom() {
                BLHelper.send(BluetoothCommands.getCommand('5'));
            }
        });
        BLHelper.getMessages().observe(this, this::onMessage);
    }

    private String getColorComponent(float value) {
        return String.valueOf((int)Math.floor(value == 1 ? 255 : value * 256));
    }

    private void onMessage(Event<char[]> message) {
        String[] msg = BluetoothCommands.parseCommand(message.getContentIfNotHandled());
        if(msg != null) {
            if (msg[0].equals("2") && msg[1].equals("0")) {
                if(msg[2].equals("0")) {
                    int score = Integer.parseInt(msg[3]);
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.snake_gameover, score))
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                finish();
                            })
                            .setCancelable(false).create().show();
                }
                if(msg[2].equals("1")){
                    int score = Integer.parseInt(msg[3]);
                    scoreTextView.setText(getString(R.string.score, score));
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (backPressed) {
            BLHelper.send(BluetoothCommands.getCommand('6'));
            finish();
        } else {
            backPressed = true;
            Toast.makeText(SnakeGameActivity.this, R.string.back_pressed, Toast.LENGTH_SHORT).show();
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    backPressed = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            BLHelper.send(BluetoothCommands.getCommand('1'));
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            BLHelper.send(BluetoothCommands.getCommand('0'));
        } else if(keyCode == KeyEvent.KEYCODE_BACK){
            if (backPressed) {
                BLHelper.send(BluetoothCommands.getCommand('6'));
                finish();
            } else {
                backPressed = true;
                Toast.makeText(SnakeGameActivity.this, R.string.back_pressed, Toast.LENGTH_SHORT).show();
                (new Timer()).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        backPressed = false;
                    }
                }, 2000);
            }
        }
        return true;
    }

    private static class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick();
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
