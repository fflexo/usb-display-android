package com.github.fflexo.usb_display;

public interface FrameSource {
    public byte[] takePendingRawFrame();
}
