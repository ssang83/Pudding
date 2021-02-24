package com.enliple.pudding.commons.network;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.enliple.pudding.commons.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private static final String LOG_TAG = ProgressRequestBody.class.getSimpleName();

    public interface ProgressCallback {
        public void onProgress(long progress, long total);
    }

    public static class UploadInfo {
        public Uri contentUri;                  // Content uri for the file
        public long contentLength;              // File size in bytes
        public MediaType mediaType;             // Content MIME Type
    }

    private WeakReference<Context> mContextRef;
    private UploadInfo mUploadInfo;
    private ProgressCallback mListener;

    private static final int UPLOAD_PROGRESS_BUFFER_SIZE = 8192;

    public ProgressRequestBody(Context context, UploadInfo uploadInfo, ProgressCallback listener) {
        mContextRef = new WeakReference<>(context);
        mUploadInfo = uploadInfo;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mUploadInfo.mediaType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mUploadInfo.contentLength;
        byte[] buffer = new byte[UPLOAD_PROGRESS_BUFFER_SIZE];
        InputStream in = null;
        long uploaded = 0;

        try {
            in = in();

            int read;
            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);

                if (mListener != null) {
                    mListener.onProgress(uploaded, fileLength);
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * WARNING: You must override this function and return the file size or you will get errors
     */
    @Override
    public long contentLength() throws IOException {
        return mUploadInfo.contentLength;
    }

    private InputStream in() throws IOException {
        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(mUploadInfo.contentUri);
        } catch (Exception ex) {
            Logger.e(LOG_TAG, "Error getting input stream for upload" + ex);
        }

        return stream;
    }

    private ContentResolver getContentResolver() {
        if (mContextRef.get() != null) {
            return mContextRef.get().getContentResolver();
        }
        return null;
    }
}