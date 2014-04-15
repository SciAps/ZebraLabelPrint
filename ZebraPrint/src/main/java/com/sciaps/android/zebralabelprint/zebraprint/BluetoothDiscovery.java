/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.sciaps.android.zebralabelprint.zebraprint;

import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import com.sciaps.android.zebralabelprint.zebraprint.utils.UIHelper;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;

public class BluetoothDiscovery extends DiscoveryResultList {


     private MenuItem action_discover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doDiscover();


        getActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.bluetooth_discovery, menu);
        action_discover = menu.findItem(R.id.action_discover).setVisible(false);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                finish();
                break;
            case R.id.action_discover:
                mListAdapter = new DevicesListAdapter(getApplicationContext(),0);

                setListAdapter(mListAdapter);
                doDiscover();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void discoveryFinished() {
        if (action_discover!=null) {

            action_discover.setVisible(true);
        }
        super.discoveryFinished();
    }

    @Override
    public void discoveryError(String message) {
        if (action_discover!=null) {

            action_discover.setVisible(true);
        }
        super.discoveryError(message);
    }

    private void doDiscover() {

        if (action_discover!=null) {
            action_discover.setVisible(false);

        }
        setProgressBarIndeterminateVisibility(true);

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    BluetoothDiscoverer.findPrinters(BluetoothDiscovery.this, BluetoothDiscovery.this);
                } catch (ConnectionException e) {

                    new UIHelper(BluetoothDiscovery.this).showErrorDialogOnGuiThread(e.getMessage());
                } finally {

                    Looper.myLooper().quit();

                }
            }
        }).start();
    }

}
