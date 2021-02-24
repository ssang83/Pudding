package com.enliple.pudding.commons.app;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.enliple.pudding.commons.log.Logger;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static int ConvertDpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float ConvertDpToPx(Context context, float dp) {
        return (float) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static String ToNumFormat(int num) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(num);
    }

    public static String ToNumFormat(double num) {
        int number = (int) num;
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(num);
    }

    public static int GetStatusBarHeight(Context context) {
        int height;

        Resources myResources = context.getResources();
        int idStatusBarHeight = myResources.getIdentifier("status_bar_height", "dimen", "android");
        if (idStatusBarHeight > 0)
            height = context.getResources().getDimensionPixelSize(idStatusBarHeight);
        else
            height = 0;

        return height;
    }

    public static String getUniqueID(Context context) {
        try {
            String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
            return deviceUuid.toString();
        } catch (Exception e) {
            Logger.p(e);
        }

        return "uuid" + System.currentTimeMillis();
    }

    public static void checkResolution(WindowManager wm, Context context) {
        // 해상도
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        float den = dm.density * 160;

        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            h += resources.getDimensionPixelSize(resourceId);
        }

        String dct;
        if (den <= 120) {
            dct = "ldpi";
        } else if (den <= 160) {
            dct = "mdpi";
        } else if (den <= 240) {
            dct = "hdpi";
        } else if (den <= 320) {
            dct = "xhdpi";
        } else if (den <= 480) {
            dct = "xxhdpi";
        } else {
            dct = "xxxhdpi";
        }

        int dpw = (int) (w / (den / 160));
        int dph = (int) (h / (den / 160));

        String size = "";
        if (dph >= 960 && dpw >= 720) {
            size = "xlarge";
        } else if (dph >= 640 && dpw >= 480) {
            size = "large";
        } else if (dph >= 470 && dpw >= 320) {
            size = "normal";
        } else {
            size = "small";
        }

        Logger.h("해상도  ( " + dph + "dp X " + dpw + "dp )");
        Logger.h("Density:" + (int) den + "   " + size + "-" + dct + "-" + h + "X" + w);
    }

    public static int GetDiscountRate(int originPrice, int sellPrice) {
        int rate = 0;
        if ( originPrice != 0 ) {
            try {
                rate = ((originPrice - sellPrice) / originPrice) * 100;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return rate;
    }

    public static long dateParseRegExp(String period) {
        Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
        Matcher matcher = pattern.matcher(period);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1)) * 3600000L
                    + Long.parseLong(matcher.group(2)) * 60000
                    + Long.parseLong(matcher.group(3)) * 1000;
        } else {
            return 0;
        }
    }

    public static long getDurationFromHls(String hlsUrl) {
        String url = hlsUrl.replace("http://cache.midibus.kinxcdn.com/hls/", "");
        String[] arr = url.split("/");
        String valueKey = arr[1];
        url = "https://cache.midibus.kinxcdn.com/direct/" + valueKey;

        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(url);
            String duration =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long lDuration = Long.valueOf(duration);
            return lDuration;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return 0;
    }

    public static ArrayList<String> getVideoSizeFromHls(String hlsUrl) {
        String url = hlsUrl.replace("http://cache.midibus.kinxcdn.com/hls/", "");
        String[] arr = url.split("/");
        String valueKey = arr[1];
        url = "https://cache.midibus.kinxcdn.com/direct/" + valueKey; // hls

        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(url);
            String videoWidth =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String videoHeight =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            ArrayList<String> array = new ArrayList<>();
            array.add(videoWidth);
            array.add(videoHeight);
            return array;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return null;
    }

//    public static String getUniqueID(Context context) {
//        String deviceId = "";
//
//        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//
//        try {
//            final String tmDevice, tmSerial, androidId;
//
//            if (tm != null) {
//                tmDevice = "" + tm.getDeviceId();
//                tmSerial = "" + tm.getSimSerialNumber();
//                androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//
//                UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
//                deviceId = deviceUuid.toString();
//                return deviceId;
//            }
//
//            return deviceId;
//        } catch (SecurityException e) {
//            return deviceId;
//        }
//    }
}
