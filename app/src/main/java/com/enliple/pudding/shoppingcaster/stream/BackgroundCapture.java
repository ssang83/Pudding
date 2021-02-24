package com.enliple.pudding.shoppingcaster.stream;

import android.content.Context;
import android.graphics.Bitmap;

import com.ksyun.media.streamer.capture.ImgTexSrcPin;
import com.ksyun.media.streamer.util.BitmapLoader;
import com.ksyun.media.streamer.util.gles.GLRender;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Capture texture from picture.
 */

public class BackgroundCapture {
    private final static int MAX_PIC_LEN = 2048;

    public ImgTexSrcPin imgTexSrcPin;

    private Timer mTimer;

    public BackgroundCapture(GLRender glRender) {
        imgTexSrcPin = new ImgTexSrcPin(glRender);
    }

    public void start(Context context, String uri) {
        Bitmap bitmap = BitmapLoader.loadBitmap(context, uri, MAX_PIC_LEN, MAX_PIC_LEN);
        imgTexSrcPin.updateFrame(bitmap, true);

        long delay = (long) (1000.0F / 10.0F);
        mTimer = new Timer("ImageRepeat");
        mTimer.schedule(new TimerTask() {
            public void run() {
                imgTexSrcPin.repeatFrame();
            }
        }, delay, delay);
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        imgTexSrcPin.updateFrame(null, false);
    }
}
