package com.sciaps.android.zebralabelprint.zebraprint;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class PrintTypeDialog extends DialogFragment {

    protected enum Types {
        Landscape_2X3,
        Portrait3X2
    }

    public static final String CUSTOM_INTENT = "sciaps.intent.action.PRINT";

    private static String title;
    private static String message;
     private int negativeLabel;
    private int positiveLabel;
     private Activity activity;
    private ArrayList<TypeOption> typeOptions;

    private CallBack dialogCallback;


    public interface CallBack {
        void onItemSelected(Types type);

     }


    public void setDialogCallback(CallBack dialogCallback) {
        this.dialogCallback = dialogCallback;
    }

    public PrintTypeDialog(String title) {
        this.title = title;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        addTypeOptions();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.print_type_dialog, null);
        ((TextView) v.findViewById(R.id.header)).setText(title);

        ListView lv = (ListView) v.findViewById(R.id.lv_type);

        lv.setAdapter(new PrintTypesListAdapter(getActivity().getApplicationContext(), R.layout.print_type_list_item, typeOptions));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                dialogCallback.onItemSelected(typeOptions.get(i).type);


                dismiss();
            }

        });

        builder.setView(v);

        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


    private void addTypeOptions() {

        typeOptions = new ArrayList<TypeOption>();

        for (Types type : Types.values()) {

            typeOptions.add(new TypeOption(type.name(), R.drawable.ic_launcher, type));

        }
    }

    private class TypeOption {

        private Types type;
        private String title;
        private int ic;

        public TypeOption(String title, int ic, Types type) {
            this.title = title;
            this.ic = ic;
            this.type = type;

        }

        public String getTitle() {
            return title;
        }

        public int getIc() {
            return ic;
        }

     }


    public class PrintTypesListAdapter extends ArrayAdapter<TypeOption> {


        private ArrayList<TypeOption> items;

        public PrintTypesListAdapter(Context context, int textViewResourceId, ArrayList<TypeOption> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.print_type_list_item, null);
            }
            TypeOption shareOption = items.get(position);


            TextView t1 = (TextView) v.findViewById(R.id.txt_title);
            if (t1 != null) {
                t1.setText(shareOption.getTitle());


            }
            ImageView icon = (ImageView) v.findViewById(R.id.img_ic);
            icon.setImageResource(shareOption.getIc());
            return v;
        }

        @Override
        public int getCount() {
            return items.size();
        }

    }
}
