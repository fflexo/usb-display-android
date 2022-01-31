package com.github.fflexo.usb_display;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

import com.github.fflexo.usb_display.databinding.ActivityMainBinding;

public class DisplayActivity extends AppCompatActivity {

    // Used to load the 'usb_display' library on application startup.
    static {
        System.loadLibrary("usb_display");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());*/
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        setContentView(new DisplayView(this, displaySize.x, displaySize.y));
    }

    /**
     * A native method that is implemented by the 'usb_display' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}