package com.sciaps.android.zebra;


import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class SavedPrintersSettings {

    private final SharedPreferences mSharedPrefs;
    private static final String KEY_PRINTERS = "printers";

    private static final TypeToken<List<Printer>> PRINTER_LIST = new TypeToken<List<Printer>>(){};

    private ArrayList<Printer> mCache;

    public SavedPrintersSettings(SharedPreferences sharedPreferences) {
        mSharedPrefs = sharedPreferences;
    }

    public List<Printer> getSavedPrinters() {
        if(mCache == null) {
            String printersStr = mSharedPrefs.getString(KEY_PRINTERS, null);
            Gson gson = new GsonBuilder().create();

            mCache = gson.fromJson(printersStr, PRINTER_LIST.getType());
            if(mCache == null) {
                mCache = new ArrayList<Printer>();
            }
        }

        return mCache;
    }

    public void savePrinters() {
        Gson gson = new GsonBuilder().create();
        String printersStr = gson.toJson(mCache);
        SharedPreferences.Editor editer = mSharedPrefs.edit();
        editer.putString(KEY_PRINTERS, printersStr);
        editer.apply();

    }

    public void setFirstPrinter(Printer printer) {
        mCache.remove(printer);
        mCache.add(0, printer);
    }
}
