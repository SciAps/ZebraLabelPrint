package com.sciaps.android.zebralabelprint.zebraprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by MonkeyFish on 4/2/14.
 */
public class DeviceListDialogFragment extends DialogFragment {
    private static final String TAG = "CustomeDialogFragment";
    private static final boolean D = true;

    // Return Intent extra

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private static String title;
    private static String message;
    private DevicesListAdapter mDevicesArrayAdapter;

    private ArrayList<DeviceListItem> items = new ArrayList<DeviceListItem>();
    private ListView pairedListView;
    private Activity activity;

    public DeviceListDialogFragment(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public DeviceListDialogFragment(int title, int message) {
        this.title = getResources().getString(title);
        this.message = getResources().getString(message);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View v = inflater.inflate(R.layout.custome_progress_dialog, null);
        ((TextView) v.findViewById(R.id.header)).setText(title);
        //((TextView) v.findViewById(R.id.text)).setText(message);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

//        new Thread(new Runnable() {
//            public void run() {
//                Looper.prepare();
//                try {
//                    BluetoothDiscoverer.findPrinters(BluetoothDiscovery.this, BluetoothDiscovery.this);
//                } catch (ConnectionException e) {
//
//                    new UIHelper(activity).showErrorDialogOnGuiThread(e.getMessage());
//                } finally {
//                    Looper.myLooper().quit();
//                }
//            }
//        }).start();


        builder.setView(v)
                .setPositiveButton("Scan", null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        //mNewDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);

        // Find and set up the ListView for paired devices

//        // Find and set up the ListView for newly discovered devices
//        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
//        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();


        items.add(new DeviceListItem("Paired Devices", R.layout.device_list_header));

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            // findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {

                items.add(new DeviceListItem(device.getName(), device.getAddress(), R.layout.device_name));
            }
        } else {
            items.add(new DeviceListItem(getResources().getText(R.string.none_found).toString(), R.layout.device_name));

        }

        items.add(new DeviceListItem("New Devices", R.layout.device_list_header));

        mDevicesArrayAdapter = new DevicesListAdapter(getActivity(), R.layout.device_name, items);

        pairedListView = (ListView) v.findViewById(R.id.devices);
        pairedListView.setAdapter(mDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);


        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doDiscovery();


                }
            });
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        //setProgressBarIndeterminateVisibility(true);
        //setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                //     Toast.makeText(context, device.getName(), Toast.LENGTH_LONG).show();

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    items.add(new DeviceListItem(device.getName(), device.getAddress(), R.layout.device_name));
                    //mDevicesArrayAdapter.add(new DeviceListItem(device.getName(),device.getAddress(),R.layout.device_name));

                }
                mDevicesArrayAdapter = new DevicesListAdapter(getActivity(), R.layout.device_name, items);

                pairedListView.setAdapter(mDevicesArrayAdapter);

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                if (mDevicesArrayAdapter.getCount() == 0) {
                    items.add(new DeviceListItem(getResources().getText(R.string.none_found).toString(), R.layout.device_name));
                    //mDevicesArrayAdapter.add(new DeviceListItem(getResources().getText(R.string.none_found).toString(),R.layout.device_name));

//                    String noDevices = getResources().getText(R.string.none_found).toString();
//                    mDevicesArrayAdapter.add(noDevices);


                    mDevicesArrayAdapter = new DevicesListAdapter(getActivity(), R.layout.device_name, items);

                    pairedListView.setAdapter(mDevicesArrayAdapter);

                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {


            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {


            }
        }
    };


    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            TextView txtAddress = (TextView) v.findViewById(R.id.txt2);
            if (txtAddress == null) {
                return;
            }

            String address = txtAddress.getText().toString();

            //save Device
            SharedPreferences prfs = getActivity().getSharedPreferences("DEFAULT_SCIAPPS_DEVICE",
                    Context.MODE_PRIVATE);
//TODO: FIX THIS
//            SharedPreferences.Editor editor = prfs.edit();
//            editor.putString(BluetoothCommandService.DEVICE_ADDRESS, address);
//            editor.commit();


//            // Create the result Intent and include the MAC address
//            Intent intent = new Intent();
//            intent.putExtra(BluetoothCommandService.DEVICE_ADDRESS, address);


            DeviceListDialogFragment.this.getDialog().cancel();
        }
    };
}
