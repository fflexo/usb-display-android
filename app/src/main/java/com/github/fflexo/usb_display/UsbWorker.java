package com.github.fflexo.usb_display;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

public class UsbWorker implements Runnable, FrameSource {
    private final UsbManager manager;
    private Thread worker;
    private UsbAccessory usbAccessory;
    private final View view;

    UsbWorker(Intent intent, UsbManager manager, View view) {
        this.manager = manager;

        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
            usbAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        }

        this.view = view;
        openAccessory();

        if (usbAccessory != null) {
            worker = new Thread(null, this, "AccessoryThread");
            worker.start();
            Log.i("usb-worker", "Started USB thread");
        }
        else {
            Log.w("usb-worker", "Usb worker created, but not in accessory mode correctly!");
        }

    }

    private byte[] pendingRawFrame;
    private int droppedFrameCounter = 0;

    public synchronized byte[] takePendingRawFrame(byte[] replacement) {
        // This is both a get and a set. It's dual purpose for synchronized keyword.
        byte[] old = pendingRawFrame;
        pendingRawFrame = replacement;
        return old;
    }

    public byte[] takePendingRawFrame() {
        // Replace it with NULL, call our peer which is the single, synchronised method
        return takePendingRawFrame(null);
    }

    @Override
    public void run() {
        DataInputStream dis = new DataInputStream(mInput);
        try {
            // Send EDID, protocol start etc.
            //mOutput.write("Hello world\n".getBytes());

            // TODO: don't keep working too hard if we're backgrounded
            while (true) {
                mOutput.write("Ready for frame\n".getBytes());
                Log.i("usb-worker","sent frame request");
                // N.B. This is where our USB stream's endianness gets defined
                byte[] frame = null;
                try {
                    int frameSize = dis.readInt();
                    Log.i("usb-worker", "frame header: " + frameSize);

                    // TODO: recycle frame byte buffers if performance benchmarking suggests it's worthwhile
                    frame = new byte[frameSize];
                    dis.readFully(frame, 0, frame.length);
                }
                catch (InterruptedIOException ioe) {
                    Log.w("usb-worker", "Interrupted IO exception");
                }
                // Next job is to decode frame. It's a little unclear right now where's the right
                // right place to actually do the bitmap decode, this thread or another thread.
                // For now we expect the main thread to do that, but longer term we should
                // probably think about this some more. (OFC h264 might just win everything anyway)

                // Depending on relative speeds of loops we can end up silently dropping a frame here
                // I'm down with that for now, but we do keep score
                if (takePendingRawFrame(frame) != null) {
                    ++droppedFrameCounter;
                }
                view.postInvalidate();
            }
        }
        catch (IOException e) {
            // TODO: recovery?
            Log.e("usb-display", "Exception during read");
            e.printStackTrace();
        }
        Log.e("usb-worker", "usb thread exiting");
    }

    private FileInputStream mInput;
    private FileOutputStream mOutput;
    private ParcelFileDescriptor pfd;
    private FileDescriptor fd;

    private void openAccessory() {
        //UsbAccessory[] accessoryList = manager.getAccessoryList();
        //ParcelFileDescriptor pfd = manager.openAccessory(accessoryList[0]);
        //FileDescriptor fd;
        try  {
            pfd = manager.openAccessory(usbAccessory);
            fd = pfd.getFileDescriptor();
            mInput = new FileInputStream(fd);
            mOutput = new FileOutputStream(fd);
        }
        catch (IllegalArgumentException e) {
            Log.w("usb-display", "No accessory attached?");
        }

    }

}
