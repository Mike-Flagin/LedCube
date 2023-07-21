package com.mike.ledcube;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class LedCube extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
