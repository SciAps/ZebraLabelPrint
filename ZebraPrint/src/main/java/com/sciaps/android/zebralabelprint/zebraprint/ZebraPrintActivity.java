package com.sciaps.android.zebralabelprint.zebraprint;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sciaps.android.zebralabelprint.zebraprint.utils.PrintUtils;
import com.sciaps.android.zebralabelprint.zebraprint.utils.SettingsHelper;
import com.sciaps.android.zebralabelprint.zebraprint.utils.UIHelper;
import com.sciaps.common.libs.LIBAnalysisResult;
import com.sciaps.common.serialize.JsonSerializerFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.sciaps.android.zebralabelprint.zebraprint.PrintTypeDialog.Types;

public class ZebraPrintActivity extends ActionBarActivity {
    private static final String TAG = "ZebraPrintActivity";


    private Button btn_selectPrinter;
    private String dName;
    private String dMac;
    private Button btn_print;
    private Uri dataUri;
    private LIBAnalysisResult libsResult;
    public static final String CUSTOM_INTENT = "sciaps.intent.action.PRINT";
    PrintUtils printUtils;
    private Button btn_type;
    private PrintTypeDialog dialog;
    private ImageView img_prev;
    private UIHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_print);

        helper = new UIHelper(this);
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            helper.showErrorDialog("Bluetooth Not Enabled");
        }
        printUtils = new PrintUtils(ZebraPrintActivity.this);
        printUtils.setPrinterCallback(new PrintUtils.PrintCallBack() {
            @Override
            public void onPrintSent() {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            @Override
            public void onPrintError(Exception e) {
               // helper.showErrorDialogOnGuiThread("Error: " + e.getMessage());
                Toast.makeText(getApplicationContext(),"Job Failed",Toast.LENGTH_LONG).show();
                return;
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        dataUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

//        //temp
//        if (dataUri==null){
//            dataUri = Uri.parse("content://com.sciaps.libs.results/item/37/json");
//        }

        if (CUSTOM_INTENT.equals(action) && type != null&&dataUri!=null) {
            try {
                Log.e(TAG, "Share uri: " + dataUri);


                InputStream is = getContentResolver().openInputStream(dataUri);
                libsResult = loadResult(is);


            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }

        } else {
            Log.w(TAG, "No file description");
        }

        btn_selectPrinter = (Button) findViewById(R.id.btn_selectPrinter);
        btn_selectPrinter.setOnClickListener(selectPrinterListener);
        btn_print = (Button) findViewById(R.id.btn_print);
        btn_print.setOnClickListener(printListener);

        btn_type = (Button) findViewById(R.id.btn_type);
        btn_type.setOnClickListener(typeListener);

        img_prev = (ImageView) findViewById(R.id.img_prev);


        dName = SettingsHelper.getBluetoothName(getApplicationContext());
        if (dName.length() > 0) {
            btn_selectPrinter.setText(dName);
        } else {
            btn_print.setEnabled(false);

        }

        Bitmap mPrintBm;
        if (libsResult != null) {
            mPrintBm = printUtils.createBitmapFromAnalysisResult(getApplicationContext(), dataUri, libsResult);

            img_prev.setImageBitmap(mPrintBm);
        } else {
            mPrintBm = printUtils.createTestBitmap(getApplicationContext());

            img_prev.setImageBitmap(mPrintBm);
        }

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public static LIBAnalysisResult loadResult(InputStream is) throws IOException {
        LIBAnalysisResult retVal = null;
        try {
            retVal = JsonSerializerFactory.getSerializer(LIBAnalysisResult.class).deserialize(is);
        } catch (IOException e) {
            is.close();
            throw e;
        }
        return retVal;
    }


    private OnClickListener printListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            //mPrintBm = printUtils.createBitmapFromAnalysisResult(getApplicationContext(), dataUri, libsResult);

            dMac = SettingsHelper.getBluetoothAddress(getApplicationContext());

            printUtils.printPhotoFromExternal(dMac, dataUri, libsResult);
        }
    };


    private OnClickListener typeListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            dialog = new PrintTypeDialog();
            dialog.setDialogCallback(new PrintTypeDialog.CallBack() {
                @Override
                public void onItemSelected(Types type) {

                    SettingsHelper.savePrintType(getApplicationContext(), type.ordinal());
                    Bitmap mPrintBm;

                    if (libsResult != null) {
                        mPrintBm = printUtils.createBitmapFromAnalysisResult(getApplicationContext(), dataUri, libsResult);

                    } else {
                        mPrintBm = printUtils.createTestBitmap(getApplicationContext());

                    }
                    img_prev.setImageBitmap(mPrintBm);


                }
            });
            dialog.show(getFragmentManager(), TAG);

        }
    };


    private OnClickListener selectPrinterListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ZebraPrintActivity.this, BluetoothDiscovery.class);
            startActivityForResult(intent, 1, null);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                Log.i(TAG, "ZebraPrintActivity Result MAC: " + name);

                String mac = data.getStringExtra("mac");
                Log.i(TAG, "ZebraPrintActivity Result MAC: " + mac);


                SettingsHelper.saveBluetoothAddress(this, mac);
                SettingsHelper.saveBluetoothName(this, name);

                btn_selectPrinter.setText(name);
                btn_print.setEnabled(true);

            }
            if (resultCode == RESULT_CANCELED) {
            }
        }
    }
}
