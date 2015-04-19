package com.sciaps.android.zebralabelprint.zebraprint;

import android.app.Application;
import android.os.Environment;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sciaps.common.serialize.JSONSerializerModule;

import java.io.File;

/**
 * Created by MonkeyFish on 7/25/14.
 */
public class ZebraPrintApplication extends Application {

    private Injector mInjector;
    private static ZebraPrintApplication instance;


    public static ZebraPrintApplication getInstance() {
        return instance;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public Injector getInjector() {
        File hardwareConfigDir = new File(Environment.getExternalStorageDirectory(), "sciaps");
        if (mInjector == null) {
            mInjector = Guice.createInjector(
                    new JSONSerializerModule()
            );
        }
        return mInjector;
    }

//    private class ApplicationModule extends AbstractModule {
//
//        @Override
//        protected void configure() {
//
//        }
//
//        @Provides
//        @Singleton
//        LIBZService providesLIBZService(){
//            LIBZService mLIBZService = new LIBZService();
//            return mLIBZService;
//        }
//    }

}
