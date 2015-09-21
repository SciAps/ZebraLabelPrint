package com.sciaps.android.zebra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.Iterables;

import java.util.ArrayList;

public class BluetoothPrinterSpinnerAdapter extends BaseAdapter {

    private final ArrayList<Printer> mItems = new ArrayList<Printer>();

    @Override
    public int getCount() {
        return mItems.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        if(i < mItems.size()) {
            return mItems.get(i);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View retval = view;
        if(retval == null) {
            retval = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
        }

        TextView textView1 = (TextView) retval.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) retval.findViewById(android.R.id.text2);

        if(i < mItems.size()) {
            final Printer item = mItems.get(i);
            textView1.setText(item.displayName);
            textView2.setText(item.connectionString);
        } else {

            textView1.setText("No Printer");
            textView2.setText("Touch to Discover");
        }

        return retval;
    }

    public void setPrinters(Iterable<Printer> printers) {
        mItems.clear();
        if(printers != null) {
            Iterables.addAll(mItems, printers);
        }
        notifyDataSetChanged();
    }

    public int addPrinter(Printer printer) {
        mItems.add(0, printer);
        notifyDataSetChanged();
        return 0;
    }
}
