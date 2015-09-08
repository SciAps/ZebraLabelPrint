package com.sciaps.android.zebra;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.devsmart.TaskQueue;
import com.devsmart.ThreadUtils;
import com.devsmart.android.BackgroundTask;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ChooseBluetoothPrinterDialog extends DialogFragment {

    public interface Callback {
        void onPrinterSelected(Printer printer);
    }

    private static final Logger logger = LoggerFactory.getLogger(ChooseBluetoothPrinterDialog.class);
    private static final TaskQueue mDiscoverQueue = new TaskQueue(ThreadUtils.IOThreads);

    private Callback mCallback;
    private Button mRefeshButton;
    private ListView mPrinterList;
    private PrinterListAdapter mPrinterAdapter;

    static ChooseBluetoothPrinterDialog newInstance() {
        ChooseBluetoothPrinterDialog retval = new ChooseBluetoothPrinterDialog();
        return retval;
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.discoverprinter, container, false);

        mPrinterList = (ListView) retval.findViewById(R.id.printerlist);
        mPrinterAdapter = new PrinterListAdapter();
        mPrinterList.setAdapter(mPrinterAdapter);
        mPrinterList.setOnItemClickListener(mOnPrinterSelected);

        mRefeshButton = (Button) retval.findViewById(R.id.refresh);
        mRefeshButton.setOnClickListener(mOnRefresh);

        return retval;

    }

    @Override
    public void onResume() {
        super.onResume();
        discoverPrinters();
    }

    private View.OnClickListener mOnRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mRefeshButton.setEnabled(false);
            discoverPrinters();
        }
    };

    private AdapterView.OnItemClickListener mOnPrinterSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(mCallback != null) {
                mCallback.onPrinterSelected((Printer) mPrinterAdapter.getItem(i));
            }
            dismiss();
        }
    };

    private class PrinterListAdapter extends BaseAdapter {

        final List<Printer> mPrinterItems = new ArrayList<Printer>();

        void clear(){
            mPrinterItems.clear();
            notifyDataSetChanged();
        }

        void addPrinter(Printer printer) {
            mPrinterItems.add(printer);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mPrinterItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mPrinterItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            final Printer item = mPrinterItems.get(i);
            View retval = convertView;
            if(retval == null) {
                retval = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            TextView text1 = (TextView) retval.findViewById(android.R.id.text1);
            text1.setText(item.toString());

            retval.setTag(item);
            return retval;
        }
    }

    static boolean isDiscovering = false;

    private void discoverPrinters() {

        if(isDiscovering) {
            return;
        } else {
            isDiscovering = true;
        }

        mPrinterAdapter.clear();

        try {
            logger.info("start printer discovery");
            mRefeshButton.setEnabled(false);
            BluetoothDiscoverer.findPrinters(getActivity(), new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    logger.info("found printer: {}", discoveredPrinter);
                    if(discoveredPrinter instanceof DiscoveredPrinterBluetooth) {
                        mPrinterAdapter.addPrinter(Printer.createPrinter((DiscoveredPrinterBluetooth)discoveredPrinter));
                    }

                }

                @Override
                public void discoveryFinished() {
                    logger.info("discovery finished");
                    isDiscovering = false;
                    mRefeshButton.setEnabled(true);
                }

                @Override
                public void discoveryError(String s) {
                    logger.info("discovery error {}", s);
                    isDiscovering = false;
                    mRefeshButton.setEnabled(true);
                }
            });

        } catch (ConnectionException e) {

        }

    }

    private void getPrinterInfo(List<Printer> printerList) {

        for(final Printer printer : printerList) {
            BackgroundTask.runBackgroundTask(new BackgroundTask() {
                @Override
                public void onBackground() {
                    try {
                        printer.getPrinterInfo();
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }, mDiscoverQueue);

        }


    }
}
