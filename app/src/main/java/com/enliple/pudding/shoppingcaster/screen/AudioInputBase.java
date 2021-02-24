package com.enliple.pudding.shoppingcaster.screen;

import com.ksyun.media.streamer.framework.AudioBufFormat;
import com.ksyun.media.streamer.framework.AudioBufFrame;
import com.ksyun.media.streamer.framework.SrcPin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 */
public class AudioInputBase implements OnAudioPCMListener {
    private final static String TAG = "AudioInputBase";
    private SrcPin<AudioBufFrame> mSrcPin;
    private AudioBufFormat mOutFormat;
    private ByteBuffer mOutBuffer;

    public AudioInputBase() {
        mSrcPin = new SrcPin<>();
    }

    public SrcPin<AudioBufFrame> getSrcPin() {
        return mSrcPin;
    }

    @Override
    public void onAudioPCMAvailable(ByteBuffer byteBuffer, long timestamp, int channels, int samplerate, int samplefmt) {
        if (mOutFormat == null) {
            mOutFormat = new AudioBufFormat(samplefmt, samplerate, channels);
            mSrcPin.onFormatChanged(mOutFormat);
        }
        if (byteBuffer == null) {
            return;
        }
        ByteBuffer pcmBuffer = byteBuffer;
        if (!byteBuffer.isDirect()) {
            int len = byteBuffer.limit();
            if (mOutBuffer == null || mOutBuffer.capacity() < len) {
                mOutBuffer = ByteBuffer.allocateDirect(len);
                mOutBuffer.order(ByteOrder.nativeOrder());
            }
            mOutBuffer.clear();
            mOutBuffer.put(byteBuffer);
            mOutBuffer.flip();
            pcmBuffer = mOutBuffer;
        }
        AudioBufFrame frame = new AudioBufFrame(mOutFormat, pcmBuffer, timestamp);
        mSrcPin.onFrameAvailable(frame);
    }
}
