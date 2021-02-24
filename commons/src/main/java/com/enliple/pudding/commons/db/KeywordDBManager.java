package com.enliple.pudding.commons.db;

import android.content.Context;
import android.text.TextUtils;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.enliple.pudding.commons.log.Logger;

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
public class KeywordDBManager {
    public static final String KEY = "KeywordDb";

    private static KeywordDBManager sSingleton;

    private Database mDatabase;

    public static synchronized KeywordDBManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new KeywordDBManager(context);
        }

        return sSingleton;
    }

    public KeywordDBManager(Context context) {
        if (mDatabase == null) {
            try {
                DatabaseConfiguration config = new DatabaseConfiguration(context);
                mDatabase = new Database(KEY, config);
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

    public Document getDoc(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        try {
            Document document = mDatabase.getDocument(key);
            if (document != null) {
                return document;
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.p(e);
        }

        return null;
    }

    public Document load(String key) {
        try {
            return mDatabase.getDocument(key);
        } catch (Exception e) {
            Logger.p(e);
        }

        return null;
    }

    public ResultSet loadAllByOrder() {
        Query query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(mDatabase))
                .orderBy(Ordering.property("reg_date").descending());

        ResultSet result = null;
        try {
            result = query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ResultSet loadAllExt() {
        ResultSet result = null;
        try {
            result = QueryBuilder.select(SelectResult.all())
                    .from(DataSource.database(mDatabase)).execute();
        } catch (CouchbaseLiteException e) {
            Logger.p(e);
        }

        return result;
    }

    public Query loadQuery() {
        Query query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(mDatabase));

        return query;
    }

    public Dictionary getDictionary(Result row) {
        return row.getDictionary(mDatabase.getName());
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
