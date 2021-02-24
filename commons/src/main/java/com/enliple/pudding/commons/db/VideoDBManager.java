package com.enliple.pudding.commons.db;

import android.content.Context;
import android.text.TextUtils;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.enliple.pudding.commons.log.Logger;

/**
 * jhs
 * 동영상 정보 저장 용, main화면에서 refresh 시에도 삭제됨
 */

public class VideoDBManager {
    private static VideoDBManager sSingleton;

    private Database mDatabase;

    public static synchronized VideoDBManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new VideoDBManager(context);
        }

        return sSingleton;
    }

    public VideoDBManager(Context context) {
        if (mDatabase == null) {
            try {
                DatabaseConfiguration config = new DatabaseConfiguration(context);
                mDatabase = new Database("videoDb", config);
            } catch (Exception e) {
                Logger.p(e);
            }
        }
    }

    public boolean put(MutableDocument document) {
        try {
            mDatabase.save(document);
        } catch (Exception e) {
            Logger.p(e);
            return false;
        } finally {
        }

        return true;
    }

    public String get(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }

        try {
            Document document = mDatabase.getDocument(key);
            if (document != null) {
                return document.getString(key);
            } else {
                return "";
            }
            //return URLEncoder.encode(data, "UTF-8");
        } catch (Exception e) {
            Logger.p(e);
        }

        return "";
    }

    public Document load(String key) {
        try {
            return mDatabase.getDocument(key);
        } catch (Exception e) {
            Logger.p(e);
        }

        return null;
    }

    public boolean delete(String key) {
        try {
            mDatabase.deleteIndex(key);
        } catch (Exception e) {
            Logger.p(e);
            return false;
        }

        return true;
    }

    public boolean delete(Document document) {
        try {
            mDatabase.delete(document);
        } catch (Exception e) {
            Logger.p(e);
            return false;
        }

        return true;
    }

    public synchronized void deleteAll() {
        try {
            mDatabase.delete();

            sSingleton = null;
        } catch (Exception e) {
            Logger.p(e);
        }
    }
}