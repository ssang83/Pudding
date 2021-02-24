package com.enliple.pudding.commons.log;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogController {
    private static final String DEFAULT_FILE_NAME = "Log.txt";

    private String mFilePath = null;
    private String mFileName = null;

    private static final int MAX_LENGTH = 4000;

    public void e(String tag, String message) {
        Log.e(tag, getCaller(message));
    }

    public void w(String tag, String message) {
        Log.w(tag, getCaller(message));
    }

    public void i(String tag, String message) {
        Log.i(tag, getCaller(message));
    }

    public void d(String tag, String message) {
        Log.d(tag, getCaller(message));
    }

    public void v(String tag, String message) {
        Log.v(tag, getCaller(message));
    }

    public void c(String tag, String message) {
        String msg;

        Exception e = new Exception();
        if (e.getStackTrace() != null && e.getStackTrace().length > 3) {
            StackTraceElement el;
            if (e.getStackTrace()[4].toString().contains(".handleMessage")) {
                el = e.getStackTrace()[4];
            } else {
                el = e.getStackTrace()[3];
            }

            StringBuilder sb = new StringBuilder();
            sb.append(getCaller(message));
            sb.append("   [ Called From : ");
            sb.append(el.getFileName() + " ");
            sb.append(el.getLineNumber() + " ] ");

            msg = sb.toString();
        } else {
            msg = message;
        }

        Log.i(tag, msg);
    }

    public void l(String tag, String message) {
        int len = message.length();
        if (len > MAX_LENGTH) {
            int count = (len / MAX_LENGTH) + 1;
            for (int i = 0; i < count; i++) {
                int start = MAX_LENGTH * i;
                int end = start + MAX_LENGTH;
                if (end > len) {
                    end = len;
                }

                Log.i(tag, message.substring(start, end));
            }
        } else {
            Log.i(tag, message);
        }
    }

    public void h(String tag, String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(getCaller(""));
        sb.append("\n=====================================================");
        sb.append("\n║  " + message);
        sb.append("\n=====================================================");

        Log.e(tag, sb.toString());
    }

    private String getCaller(String message) {
        Exception e = new Exception();
        if (e.getStackTrace() != null && e.getStackTrace().length > 2) {
            StackTraceElement el = e.getStackTrace()[3];

            StringBuilder sb = new StringBuilder("(");
            sb.append(el.getFileName() + " ");
            sb.append(el.getLineNumber() + ") ");
            sb.append(message);

            return sb.toString();
        } else {
            return message;
        }
    }

    public void json(String tag, String message) {
        final int JSON_INDENT = 4;

        try {
            if (message.startsWith("{")) {
                JSONObject object = new JSONObject(message);
                Log.d(tag, getCaller(object.toString(JSON_INDENT)));
            } else if (message.startsWith("[")) {
                JSONArray jArray = new JSONArray(message);
                Log.d(tag, getCaller(jArray.toString(JSON_INDENT)));
            }
        } catch (JSONException e) {
            p(tag, e);
        }
    }

    public void json2(String tag, String message) {
        final int JSON_INDENT = 4;

        try {
            if (message.startsWith("{")) {
                JSONObject object = new JSONObject(message);
                String msg = object.toString(JSON_INDENT);

                String[] lines = msg.split(System.getProperty("line.separator"));
                for (String line : lines) {
                    Log.d(tag, getCaller(line));
                }

            } else if (message.startsWith("[")) {
                JSONArray jArray = new JSONArray(message);
                Log.d(tag, getCaller(jArray.toString(JSON_INDENT)));
            }
        } catch (JSONException e) {
            p(tag, e);
        }
    }

    public void p(String tag, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        e(tag, sw.getBuffer().toString());
    }

    public void setFilePath(String path) {
        mFilePath = path;
    }

    public void setFileName(String name) {
        mFileName = name;
    }

    public void s(String tag, String log) {
        log = getCaller(log);

        Log.d(tag, log);

        saveLog(tag, log, 0);
    }

    private void saveLog(String tag, String message, int state) {
        if (TextUtils.isEmpty(mFilePath)) {
            return;
        }

        String status = Environment.getExternalStorageState();
        if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED) == false) {
            e(tag, "SDCard Status:" + status);
            return;
        }

        File dir = new File(mFilePath);
        if (dir.exists() == false) {
            dir.mkdirs();
        }

        if (TextUtils.isEmpty(mFileName)) {
            mFileName = DEFAULT_FILE_NAME;
        }

        try {
            File f = new File(mFilePath, mFileName);

            FileOutputStream fos;
            if (state == 1) {
                fos = new FileOutputStream(f, false);
            } else {
                fos = new FileOutputStream(f, true);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss:SSS");
            String logTime = sdf.format(new Date());
            String p = logTime + "\t" + message + "\n\r";

            fos.write(p.getBytes());
            fos.close();
        } catch (Exception e) {
            p(tag, e);
        }
    }
}