package com.enliple.pudding.shoppingcaster.stream;

import android.content.Context;
import android.graphics.Bitmap;

import com.ksyun.media.streamer.capture.ImgTexSrcPin;
import com.ksyun.media.streamer.util.BitmapLoader;
import com.ksyun.media.streamer.util.gles.GLRender;

/**
 * Capture texture from picture.
 */

public class PictureCapture {
    private final static int MAX_PIC_LEN = 2048;

    public ImgTexSrcPin imgTexSrcPin;

    public PictureCapture(GLRender glRender) {
        imgTexSrcPin = new ImgTexSrcPin(glRender);
    }

    public void start(Context context, String uri) {
        Bitmap bitmap = BitmapLoader.loadBitmap(context, uri, MAX_PIC_LEN, MAX_PIC_LEN);
        imgTexSrcPin.updateFrame(bitmap, true);
    }

    public void stop() {
        imgTexSrcPin.updateFrame(null, false);
    }
}
