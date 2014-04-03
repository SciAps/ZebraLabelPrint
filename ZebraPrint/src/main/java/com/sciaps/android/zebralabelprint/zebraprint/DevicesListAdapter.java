package com.sciaps.android.zebralabelprint.zebraprint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MonkeyFish on 3/6/14.
 */
public class DevicesListAdapter extends ArrayAdapter {

    private final ArrayList<DeviceListItem> items;

    public DevicesListAdapter(Context context, int resource, ArrayList<DeviceListItem> items) {

        super(context, resource);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        DeviceListItem item = items.get(position);

       // if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(item.getResId(), parent, false);
        //}else{
        //    v = convertView;
        //}


        TextView text1 = (TextView) v.findViewById(R.id.txt1);
        text1.setText(item.getText());

        if (item.getType()!=DeviceListItem.TYPE_HEADER){
            TextView text2= (TextView) v.findViewById(R.id.txt2);
            text2.setText(item.getAddress());
        }

        return v;
    }
}
