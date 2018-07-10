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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.devsmart.android.BackgroundTask;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;

import static com.sciaps.android.zebra.Printer.PRINTER_WIDTH_FILE;

public class PrintActivity extends Activity {

    public static final String KEY_BITMAP = "bitmap";
    private static final Logger logger = LoggerFactory.getLogger(PrintActivity.class);
    private Bitmap mBitmap;
    private Button mPrintButton;
    private Spinner mChoosePrinterButton;
    private ImageView mPrintPreview;
    private BluetoothPrinterSpinnerAdapter mPrinterAdapter;
    private SavedPrintersSettings mSavedPrinters;
    private static Printer mSelectedPrinter;
    private static String DEFAULT_WIDTH = "2.84";
    BluetoothAdapter mBluetoothAdapter;

    private int mRetryCount = 0;
    private int REQUEST_DEVICE_DISCOVERABLE = 1;

    private View.OnClickListener mOnPrintClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (mSelectedPrinter == null) {
                selectPrinter();
            } else {
                mRetryCount = 0;
                print(mSelectedPrinter, mBitmap);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                Printer printer = (Printer) mPrinterAdapter.getItem(i);
                if (printer != null) {
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

        if (!savedPrinters.isEmpty()) {
            mChoosePrinterButton.setSelection(0);
        }

        createDefaultWidthFile();
    }

    private void createDefaultWidthFile() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/sciaps/" + PRINTER_WIDTH_FILE);
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(file, false);
                PrintWriter out = new PrintWriter(outputStream, true);
                out.println(DEFAULT_WIDTH);
                out.close();
                outputStream.close();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        useIntent(intent);
    }

    private void useIntent(Intent intent) {
        final Uri dataUri = intent.getData();
        final Bitmap bitmap = intent.getParcelableExtra("BitmapImage");
        final String printerMacAddress = intent.getStringExtra("PrinterMacAddress");
        final String printerName = intent.getStringExtra("PrinterName");

        mChoosePrinterButton.setVisibility(View.VISIBLE);
        mPrintButton.setVisibility(View.VISIBLE);
        mPrintPreview.setVisibility(View.VISIBLE);

        if (dataUri == null && bitmap == null) {
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

        if (dataUri != null) {
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
        } else if (bitmap != null) {
            mChoosePrinterButton.setVisibility(View.GONE);
            mPrintButton.setVisibility(View.GONE);
            mPrintPreview.setVisibility(View.GONE);

            mBitmap = bitmap;
            if (mSelectedPrinter == null || mSelectedPrinter.connectionString.contains(printerMacAddress) == false) {
                mSelectedPrinter = Printer.createPrinter(printerName, printerMacAddress);
            }

            mRetryCount = 0;
            print(mSelectedPrinter, mBitmap);
        }

    }

    private void print(final Printer printer, final Bitmap image) {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            public boolean mSuccess = false;
            public ProgressDialog mDialog;

            private int mXOffset = 0;
            private int mYOffset = 15;

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
                Connection connection = null;

                try {
                    connection = printer.getConnection();
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

                        if (genericPrinter.getPrinterControlLanguage() != PrinterLanguage.ZPL) {
                            com.zebra.sdk.printer.SGD.SET("device.languages", "zpl", connection);
                        }

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
                if (mSuccess) {
                    mSavedPrinters.setFirstPrinter(printer);
                    mSavedPrinters.savePrinters();
                    finish();
                } else {

                    if (mRetryCount >= 5) {
                        endRetryPrint(image);
                    } else {
                        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                            startActivityForResult(discoverableIntent, REQUEST_DEVICE_DISCOVERABLE);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRetryCount++;
                                print(printer, image);
                            }
                        }, 1000);
                    }


                }
            }
        });
    }

    private void endRetryPrint(Bitmap image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("Error");
        builder.setMessage("Error printing document");
        builder.setPositiveButton("OK", null);
        builder.show();

        // The following code that here mainly for error on autoprint
        mPrintPreview.setImageBitmap(image);
        mChoosePrinterButton.setVisibility(View.VISIBLE);
        mPrintButton.setVisibility(View.VISIBLE);
        mPrintPreview.setVisibility(View.VISIBLE);
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
                mRetryCount = 0;
                print(mSelectedPrinter, mBitmap);
            }
        });
        dialog.show(ft, "dialog");
    }

}
