package com.github.fflexo.usb_display;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DisplayActivity extends Activity {
    FrameSource defaultSource = null;

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

        defaultSource = new DisconnectedSource(getResources());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        DisplayView view = new DisplayView(this, /*displaySize.x*/ 2560, /*displaySize.y*/ 1440);
        FrameSource source = new UsbWorker(getIntent(), (UsbManager)getSystemService(Context.USB_SERVICE), view);

        view.setFrameSource(source);

        //view.setFrameSource(defaultSource);

        setContentView(view);



    }
}