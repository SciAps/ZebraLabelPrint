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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sciaps.android.zebralabelprint.zebraprint.utils.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.ArrayList;
import java.util.Map;

public abstract class DiscoveryResultList extends ListActivity implements DiscoveryHandler {

     protected DevicesListAdapter mListAdapter;

    public DiscoveryResultList() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.discovery_results);

        setProgressBarIndeterminateVisibility(true);

        mListAdapter = new DevicesListAdapter(getApplicationContext(),0);
        setListAdapter(mListAdapter);
    }

    public void foundPrinter(final DiscoveredPrinter printer) {
        runOnUiThread(new Runnable() {
            public void run() {
                mListAdapter.addPrinterItem(printer);
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void discoveryFinished() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(DiscoveryResultList.this, " Discovered " + mListAdapter.getCount() + " printers", Toast.LENGTH_SHORT).show();
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    public void discoveryError(String message) {
        new UIHelper(this).showErrorDialogOnGuiThread(message);
    }

    public class DevicesListAdapter extends ArrayAdapter {

        private ArrayList<DiscoveredPrinter> printerItems;
        private ArrayList<Map<String, String>> printerSettings;

        public DevicesListAdapter(Context context, int resource) {

            super(context, resource);
            this.printerItems = new ArrayList<DiscoveredPrinter>();
            this.printerSettings = new ArrayList<Map<String, String>>();
        }

        @Override
        public int getCount() {
            return printerItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            DiscoveredPrinter item = printerItems.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (inflater.inflate(android.R.layout.simple_list_item_activated_2, null));
            } else {
                itemView = convertView;
            }
            TextView txt1 = (TextView) itemView.findViewById(android.R.id.text1);
            TextView txt2 = (TextView) itemView.findViewById(android.R.id.text2);
            if (printerItems.get(position).getDiscoveryDataMap().containsKey("DARKNESS"))
                itemView.setBackgroundColor(0xff4477ff);
            if (printerItems.get(position) instanceof DiscoveredPrinterNetwork) {
                txt2.setText(((DiscoveredPrinterNetwork) item).address);
                txt1.setText(( item).getDiscoveryDataMap().get("DNS_NAME"));
            } else if (printerItems.get(position) instanceof DiscoveredPrinterBluetooth) {
                txt2.setText(((DiscoveredPrinterBluetooth) item).address);
                txt1.setText(((DiscoveredPrinterBluetooth)item).friendlyName);

             }

            return itemView;
        }


        public void addPrinterItem(DiscoveredPrinter p) {
            printerItems.add(p);
            printerSettings.add(p.getDiscoveryDataMap());
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView txt1 = (TextView) v.findViewById(android.R.id.text1);
        TextView txt2 = (TextView) v.findViewById(android.R.id.text2);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("name",txt1.getText());
        returnIntent.putExtra("mac",txt2.getText());
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}
