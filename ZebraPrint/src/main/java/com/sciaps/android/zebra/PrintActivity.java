package com.sciaps.android.zebra;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.devsmart.android.BackgroundTask;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class PrintActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(PrintActivity.class);

    public static final String KEY_BITMAP = "bitmap";
    private Bitmap mBitmap;
    private Button mPrintButton;
    private Spinner mChoosePrinterButton;
    private ImageView mPrintPreview;
    private BluetoothPrinterSpinnerAdapter mPrinterAdapter;
    private SavedPrintersSettings mSavedPrinters;
    private Printer mSelectedPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        mPrinterAdapter = new BluetoothPrinterSpinnerAdapter();
        setContentView(R.layout.printpreview);
        mChoosePrinterButton = (Spinner) findViewById(R.id.chooseprinter);
        mChoosePrinterButton.setAdapter(mPrinterAdapter);
        mChoosePrinterButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                logger.info("");
                Printer printer = (Printer)mPrinterAdapter.getItem(i);
                if(printer != null) {
                    mSelectedPrinter = printer;
                } else {
                    selectPrinter();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                logger.info("");
                mSelectedPrinter = null;
            }
        });
        mPrintButton = (Button) findViewById(R.id.print);
        mPrintButton.setOnClickListener(mOnPrintClicked);
        mPrintPreview = (ImageView) findViewById(R.id.preview);
        mPrintPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

        useIntent(getIntent());

        mSavedPrinters = new SavedPrintersSettings(PreferenceManager.getDefaultSharedPreferences(this));
        Collection<Printer> savedPrinters = mSavedPrinters.getSavedPrinters();
        mPrinterAdapter.setPrinters(savedPrinters);

        if(!savedPrinters.isEmpty()) {
            mChoosePrinterButton.setSelection(0);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        useIntent(intent);
    }

    private void useIntent(Intent intent) {
        final Uri dataUri = intent.getData();
        if(dataUri == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Error");
            builder.setMessage("Error");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            return;
        }

        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            public ProgressDialog mDialog;
            private boolean mSuccess = false;

            @Override
            public void onBefore() {
                super.onBefore();
                mDialog = new ProgressDialog(PrintActivity.this);
                mDialog.setIndeterminate(true);
                mDialog.setMessage("Loading...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            public void onBackground() {
                try {
                    InputStream in = getContentResolver().openInputStream(dataUri);
                    mBitmap = BitmapFactory.decodeStream(in);
                    mSuccess = true;
                } catch (IOException e) {
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                mDialog.dismiss();
                mPrintPreview.setImageBitmap(mBitmap);
                if (!mSuccess) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Error");
                    builder.setMessage("Error");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                }
            }
        });

    }

    private View.OnClickListener mOnPrintClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(mSelectedPrinter == null) {
                selectPrinter();
            } else {
                print(mSelectedPrinter, mBitmap);

            }

        }
    };

    private void print(final Printer printer, final Bitmap image) {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            public boolean mSuccess = false;
            public ProgressDialog mDialog;

            private int mXOffset = 0;
            private int mYOffset = 0;

            @Override
            public void onBefore() {
                mDialog = new ProgressDialog(PrintActivity.this);
                mDialog.setIndeterminate(true);
                mDialog.setMessage("Connecting...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            public void onBackground() {

                try {
                    Connection connection = printer.getConnection();
                    if (!connection.isConnected()) {
                        connection.open();
                    }

                    if (connection.isConnected()) {
                        ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(connection);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.setMessage("Printing...");
                            }
                        });

                        mXOffset = (printer.getPrinterWidth() - image.getWidth()) / 2;
                        mXOffset = Math.max(0, mXOffset);

                        genericPrinter.printImage(new ZebraImageAndroid(image),
                                mXOffset, mYOffset, //the labels start a little offset
                                image.getWidth(),
                                image.getHeight(),
                                false);
                        mSuccess = true;
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }

            }

            @Override
            public void onAfter() {
                mDialog.dismiss();
                if(mSuccess) {
                    mSavedPrinters.setFirstPrinter(printer);
                    mSavedPrinters.savePrinters();
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Error");
                    builder.setMessage("Error printing document");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }
        });
    }

    private void selectPrinter() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ChooseBluetoothPrinterDialog dialog = ChooseBluetoothPrinterDialog.newInstance();
        dialog.setCallback(new ChooseBluetoothPrinterDialog.Callback() {
            @Override
            public void onPrinterSelected(Printer printer) {
                int index = mPrinterAdapter.addPrinter(printer);
                mChoosePrinterButton.setSelection(index);
                mSelectedPrinter = printer;
                print(mSelectedPrinter, mBitmap);
            }
        });
        dialog.show(ft, "dialog");
    }

}
