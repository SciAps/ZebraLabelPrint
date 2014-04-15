package com.sciaps.android.zebralabelprint.zebraprint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sciaps.android.zebralabelprint.zebraprint.utils.PrintUtils;
import com.sciaps.android.zebralabelprint.zebraprint.utils.SettingsHelper;
import com.sciaps.common.libs.LIBAnalysisResult;
import com.sciaps.common.serialize.JsonSerializerFactory;

import java.io.IOException;
import java.io.InputStream;


public class SplashStartActivity extends Activity {

    private static final String TAG = "SplashStartActivity";
    public static final String PRINT_INTENT = "sciaps.intent.action.PRINT";
    private static final int REQUEST_CODE = 1;
    private Uri dataUri;
    private LIBAnalysisResult libsResult;
    private PrintUtils printUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_start);

        printUtils = new PrintUtils(SplashStartActivity.this);
        printUtils.setPrinterCallback(new PrintUtils.PrintCallBack() {
            @Override
            public void onPrintSent() {
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        final String mac = SettingsHelper.getBluetoothAddress(getApplicationContext());
        Log.i(TAG, "Searching for Printer: " + mac);
        Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        dataUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        //if no mac address go to settings
        if (mac.length() == 0) {
            Log.i(TAG, "Printer Not Set Up: " + mac);

            goToSetUpActivity();
            return;
        }





        new PrinterFinder(getApplicationContext(), new PrinterFinder.Callback() {
            @Override
            public void onMatchCallback(boolean printerWasFound) {
                if (printerWasFound) {
                    Log.i(TAG, "Printer found: " + mac);

                    if (PRINT_INTENT.equals(action) && type != null) {
                        try {
                            Log.e(TAG, "Share uri: " + dataUri);


                            InputStream is = getContentResolver().openInputStream(dataUri);
                            libsResult = loadResult(is);

                            print();
                            return;

                        } catch (Exception e) {
                            Log.e(TAG, "Error", e);

                        }


                    } else {
                        Log.w(TAG, "No file description");

                    }
                }
                Log.i(TAG, "Printer not found ");
                TextView txt = (TextView) findViewById(R.id.txt_splash_start);
                if (txt != null) {
                    txt.setText("Printer not found");
                }
                //choose the printer
                goToSetUpActivity();

                return;

            }
        }, mac);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void goToSetUpActivity(){
        Intent intent = new Intent(SplashStartActivity.this, ZebraPrintActivity.class);
        intent.setAction(PRINT_INTENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, dataUri);
        startActivityForResult(intent, REQUEST_CODE);

    }

    public static LIBAnalysisResult loadResult(InputStream is) throws IOException {
        LIBAnalysisResult retVal;
        try {
            retVal = JsonSerializerFactory.getSerializer(LIBAnalysisResult.class).deserialize(is);
        } catch (IOException e) {
            is.close();
            throw e;
        }
        return retVal;
    }

    private void print() {
        TextView txt = (TextView) findViewById(R.id.txt_splash_start);
        if (txt != null) {
            txt.setText("Printing...");
        }

        String mac = SettingsHelper.getBluetoothAddress(getApplicationContext());


        printUtils.printPhotoFromExternal(mac, dataUri, libsResult);


    }

}
