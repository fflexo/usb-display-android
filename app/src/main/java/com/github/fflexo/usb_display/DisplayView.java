package com.github.fflexo.usb_display;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class DisplayView extends View {
    private final Bitmap bitmap;
    private FrameSource source;

    void setFrameSource(FrameSource source) {
        this.source = source;
    }

    public DisplayView(DisplayActivity displayActivity, int w, int h) {
        super(displayActivity);
        // Temporary hack until EDID done:
        bitmap = Bitmap.createBitmap(2560,1440, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("usb-display-view", "onDraw");
        if (source != null) {
            byte[] newFrame = source.takePendingRawFrame();
            if (newFrame == null) {
                Log.w("usb-display-view", "Got NULL frame");
            }
            else {
                renderDisplay(bitmap, newFrame);
            }
        }
        else {
            Log.w("usb-display-view", "source is NULL");
        }

        canvas.drawBitmap(bitmap, null, canvas.getClipBounds(), null);
    }

    private static native void renderDisplay(Bitmap bitmap, byte[] rawFrame);
}
