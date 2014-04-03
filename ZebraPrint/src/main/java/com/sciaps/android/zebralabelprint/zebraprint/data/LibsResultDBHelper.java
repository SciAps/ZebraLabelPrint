package com.sciaps.android.zebralabelprint.zebraprint.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.devsmart.StringUtils;
import com.sciaps.android.zebralabelprint.zebraprint.R;

import java.io.IOException;


public class LibsResultDBHelper extends SQLiteOpenHelper {

    private final Context mContext;

    public LibsResultDBHelper(Context context) {
        super(context, "results.db", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String createStmt = StringUtils.loadRawResourceString(mContext.getResources(), R.raw.createresultstable);
            db.execSQL(createStmt);
        } catch (IOException e) {
            Log.e("", "", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }
}
