package com.github.fflexo.usb_display;

import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DisconnectedSource implements FrameSource {
    private final byte[] rawFrame;
    DisconnectedSource(Resources resources) {
        //resources.getsi
        //AssetFileDescriptor afd = resources.openRawResourceFd(R.raw.usb);
        InputStream inputStream = resources.openRawResource(R.raw.usb);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Wouldn't java 9's readAllBytes() be nice...
        try {
            byte buffer[] = new byte[4096];
            int size;

            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rawFrame = outputStream.toByteArray();
    }

    @Override
    public byte[] takePendingRawFrame() {
        return rawFrame;
    }
}
