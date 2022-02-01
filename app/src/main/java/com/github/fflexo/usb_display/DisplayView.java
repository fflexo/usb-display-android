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
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (source != null) {
            byte[] newFrame = source.takePendingRawFrame();
            
        }
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(R.raw.usb);
        int fd = afd.getParcelFileDescriptor().getFd();
        renderDisplay(bitmap, fd, afd.getLength(), afd.getStartOffset());
        try {
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
        Log.d("usb-display", "Frame draw " + fd);
        //invalidate(); or postInvalidate();
    }

    private static native void renderDisplay(Bitmap bitmap, int fd, long size, long offset);
}
