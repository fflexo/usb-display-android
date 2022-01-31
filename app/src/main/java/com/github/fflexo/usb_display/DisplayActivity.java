package com.github.fflexo.usb_display;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

import com.github.fflexo.usb_display.databinding.ActivityMainBinding;

public class DisplayActivity extends /*AppCompatActivity*/ Activity {

    // Used to load the 'usb_display' library on application startup.
    static {
        System.loadLibrary("usb_display");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        setContentView(new DisplayView(this, /*displaySize.x*/ 2560, /*displaySize.y*/ 1440));
    }
}