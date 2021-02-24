package com.enliple.pudding.shoppingcaster.screen;

import java.nio.ByteBuffer;

public interface OnAudioPCMListener {
    void onAudioPCMAvailable(ByteBuffer data, long timestamp, int channels, int samplerate, int samplefmt);
}
