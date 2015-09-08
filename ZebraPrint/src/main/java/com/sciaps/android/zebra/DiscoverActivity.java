package com.sciaps.android.zebra;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.devsmart.TaskQueue;
import com.devsmart.ThreadUtils;
import com.devsmart.android.BackgroundTask;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class DiscoverActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(DiscoverActivity.class);
    private static final TaskQueue mDiscoverQueue = new TaskQueue(ThreadUtils.IOThreads);
    private Button mRefeshButton;
    private TextView mPrinterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.discoverlayout);
        mRefeshButton = (Button) findViewById(R.id.refresh);
        mRefeshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverPrinters();
            }
        });

        mPrinterTextView = (TextView) findViewById(R.id.printerlist);

    }

    boolean isDiscovering = false;

    private void discoverPrinters() {

        if(isDiscovering) {
            return;
        } else {
            isDiscovering = true;
        }

        mPrinterTextView.setText("");
        final StringBuilder builder = new StringBuilder();

        final List<Printer> printerList = new ArrayList<Printer>();

        try {
            logger.info("start printer discovery");
            BluetoothDiscoverer.findPrinters(this, new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    logger.info("found printer: {}", discoveredPrinter);

                    printerList.add(Printer.createPrinter(discoveredPrinter));

                    String printerinfo = String.format("printer: %s\n", discoveredPrinter);
                    builder.append(printerinfo);
                    mPrinterTextView.setText(builder.toString());
                }

                @Override
                public void discoveryFinished() {
                    logger.info("discovery finished");
                    isDiscovering = false;

                    builder.append("Done");
                    mPrinterTextView.setText(builder.toString());

                    getPrinterInfo(printerList);
                }

                @Override
                public void discoveryError(String s) {
                    logger.info("discovery error {}", s);
                    isDiscovering = false;

                    builder.append("error: " + s);
                    mPrinterTextView.setText(builder.toString());
                }
            });

        }catch (ConnectionException e) {

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
