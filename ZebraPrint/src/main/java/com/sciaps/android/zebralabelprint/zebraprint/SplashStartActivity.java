package com.sciaps.android.zebralabelprint.zebraprint;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sciaps.android.zebralabelprint.zebraprint.utils.PrintUtils;
import com.sciaps.android.zebralabelprint.zebraprint.utils.SettingsHelper;
import com.sciaps.common.libs.LIBAnalysisResult;
import com.sciaps.common.serialize.JsonSerializerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SplashStartActivity extends Activity {

    private static final String TAG = "SplashStartActivity";
    public static final String PRINT_INTENT = "sciaps.intent.action.PRINT";
    private static final int REQUEST_CODE = 1;
    private Uri dataUri;
    private LIBAnalysisResult libsResult;
    private PrintUtils printUtils;
    private String mac;

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

            @Override
            public void onPrintError(Exception e) {
                goToSetUpActivity();
                return;
            }
        });
        mac = SettingsHelper.getBluetoothAddress(getApplicationContext());
        Log.i(TAG, "Searching for Printer: " + mac);
        Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        dataUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (dataUri == null)
        {
            dataUri = intent.getData();
        }



        //if no mac address go to settings
        if (getBTError() != 0) {
            goToSetUpActivity();
            return;

        }else {


            ContentResolver resolver = this.getContentResolver();


            //JsonSerializerFactory.getSerializer(LIBAnalysisResult.class).deserialize(is);// LibsApplication.getInstance(mActivity).getInjector().getInstance(Gson.class);


            Gson gson = ZebraPrintApplication.getInstance().getInjector().getInstance(Gson.class);
            //Log.e(TAG, "Error");

            try {
                libsResult = loadResult(resolver, dataUri, gson);
                print();
               // return retval;
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                Toast.makeText(getApplicationContext(),"Error Fetching Test",Toast.LENGTH_LONG).show();
                finish();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private int getBTError() {
        boolean macExists = mac.length() > 0;
        boolean btEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
        boolean dataUriExists = dataUri != null;

        if (!macExists) {
            Log.i(TAG, "Printer Not Set Up: " + mac);
            return 1;
        } else if (!btEnabled) {

            Log.i(TAG, "Bluetooth is off");

            return 2;
        } else if (!dataUriExists) {
            Log.i(TAG, "Share uri error");

            return 3;
        } else {
            return 0;
        }
    }

    ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void goToSetUpActivity() {
        Intent intent = new Intent(SplashStartActivity.this, ZebraPrintActivity.class);
        intent.setAction(PRINT_INTENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, dataUri);
        startActivityForResult(intent, REQUEST_CODE);

    }

    public static LIBAnalysisResult loadResult(ContentResolver resolver, Uri jsonUri, Gson gson) throws Exception {

        LIBAnalysisResult retval = null;
        InputStream in = resolver.openInputStream(jsonUri);
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        try {
            retval = gson.fromJson(reader, LIBAnalysisResult.class);
        }
        catch (Exception e)
        {
            reader.close();
            throw e;

        }
        finally {
            reader.close();
        }

        Cursor values = resolver.query(jsonUri, new String[]{"title"}, null, null, null);
        try {
            if (values.moveToFirst() && retval != null) {
                retval.mTitle = values.getString(0);
            }
        } finally {
            values.close();
        }

        return retval;
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
