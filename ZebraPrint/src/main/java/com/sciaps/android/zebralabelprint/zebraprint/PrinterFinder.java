package com.sciaps.android.zebralabelprint.zebraprint;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

/**
 * Created by MonkeyFish on 4/7/14.
 */
public class PrinterFinder implements DiscoveryHandler {

    private static final String TAG = "PrinterFinder";
    private final String mac;
    Callback matchFindCallback ;
    private boolean matchFound = false;
    private Thread discoverThread;


    public interface  Callback{
        void onMatchCallback(boolean b);
    }

    private final Context context;

    public PrinterFinder(Context ctx,PrinterFinder.Callback matchFindCallback, String mac){
        this.context = ctx;
        this.matchFindCallback = matchFindCallback;
        this.mac = mac;
        doDiscover();

    }
    public void cancelDiscovery(){
        discoverThread.interrupt();
    }

    private void doDiscover() {

       discoverThread =  new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    BluetoothDiscoverer.findPrinters(context, PrinterFinder.this);
                } catch (ConnectionException e) {

                    //new UIHelper(BluetoothDiscovery.this).showErrorDialogOnGuiThread(e.getMessage());
                } finally {

                    Looper.myLooper().quit();
                }
            }
        });
                discoverThread.start();
    }

    @Override
    public void foundPrinter(DiscoveredPrinter discoveredPrinter) {


        if (discoveredPrinter.address.matches(mac)){
            matchFound = true;
            matchFindCallback.onMatchCallback(true);
            matchFindCallback = null;
        }
    }

    @Override
    public void discoveryFinished() {
        Log.i(TAG, "Printer not found:");
        if (matchFindCallback!=null) {
            matchFindCallback.onMatchCallback(false);
        }
        cancelDiscovery();
    }

    @Override
    public void discoveryError(String s) {
        if (matchFindCallback!=null) {
            matchFindCallback.onMatchCallback(false);
        }
        cancelDiscovery();
    }
}
