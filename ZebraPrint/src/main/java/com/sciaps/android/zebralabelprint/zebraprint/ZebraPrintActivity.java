package com.sciaps.android.zebralabelprint.zebraprint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.sciaps.android.zebralabelprint.zebraprint.utils.SettingsHelper;
import com.sciaps.android.zebralabelprint.zebraprint.utils.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZebraPrintActivity extends ActionBarActivity {
    private static final String TAG = "ZebraPrintActivity";

    private UIHelper helper = new UIHelper(this);

    private Button btn_selectPrinter;
    private String dName;
    private String dMac;
    private Button btn_print;
    private Uri dataUri;
    private File resFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_print);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            try {
                dataUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                Log.e(TAG, "Share uri: "+dataUri);


//
//                  resFile = new File(Environment.getExternalStorageDirectory(),"alloy2.pdf");
//                    if (!resFile.isFile()){
//                        resFile.createNewFile();
//                    }

                getLibzResultFromURI(dataUri);

//                InputStream is = getContentResolver().openInputStream(dataUri);
//                IOUtils.copy(is, new FileOutputStream(resFile) );

            } catch (Exception e) {
                Log.e(TAG, "Error", e);
              //  finish();
            }



        } else {
            Toast.makeText(getApplicationContext(), "Error in file description", Toast.LENGTH_LONG).show();
            resFile = new File(Environment.getExternalStorageDirectory(),"Pic1.jpg");

            //finish();
        }

        btn_selectPrinter = (Button) findViewById(R.id.btn_selectPrinter);
        btn_selectPrinter.setOnClickListener(selectPrinterListener);
        btn_print = (Button) findViewById(R.id.btn_print);
        btn_print.setOnClickListener(printListener);


        dName =SettingsHelper.getBluetoothName(getApplicationContext());
        if (dName.length() > 0) {
            btn_selectPrinter.setText(dName);
        }


    }

    public static Pattern sItemIdRegex = Pattern.compile("item/([0-9]+)");

    private void getLibzResultFromURI(Uri dataUri) {
        String itemId = extractItemId(dataUri.toString());

//        String filename = DBUtils.lookupSingleStringValue(getDB(), ResultsTable.FILENAME, ResultsTable.TABLE_NAME,
//                ResultsTable.ID + "=?", new String[]{itemId}, null);
//
//        //create json out of result
//        File theFile = new File(filename);
//        LIBAnalysisResult result = loadResult(theFile);
        Log.i(TAG,"item id is: "+itemId);
    }

    private static String extractItemId(String uri) {
        String retval = null;
        Matcher matcher = sItemIdRegex.matcher(uri);
        if (matcher.find()) {
            retval = matcher.group(1);
        }

        return retval;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.zebra_print, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void printPhotoFromExternal(final Bitmap bitmap) {
        new Thread(new Runnable() {
            public void run() {
                try {
                 //   getAndSaveSettings();

                    Looper.prepare();
                    helper.showLoadingDialog("Sending image to printer");
                    Connection connection = getZebraPrinterConn();
                    connection.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

//                    if (((CheckBox) findViewById(R.id.checkBox)).isChecked()) {
//                        printer.storeImage(printStoragePath.getText().toString(), new ZebraImageAndroid(bitmap), 550, 412);
//                    } else {
                        printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 550, 412, false);
//                    }
                    connection.close();

//                    if (resFile != null) {
//                        resFile.delete();
//                        resFile = null;
//                    }
                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                }
//                catch (ZebraIllegalArgumentException e) {
//                    helper.showErrorDialogOnGuiThread(e.getMessage());
//                }
                finally {
                    bitmap.recycle();
                    helper.dismissLoadingDialog();
                    Looper.myLooper().quit();
                }
            }
        }).start();

    }

    private Connection getZebraPrinterConn() {
        int portNumber;
        try {
            portNumber = Integer.parseInt(getTcpPortNumber());
        } catch (NumberFormatException e) {
            portNumber = 0;
        }
        return isBluetoothSelected() ? new BluetoothConnection(dMac) : new TcpConnection(getTcpPortNumber(), portNumber);
    }

    private boolean isBluetoothSelected() {
        //return btRadioButton.isChecked();
        return true;
    }

    private String getTcpPortNumber() {
        return "";//portNumberEditText.getText().toString();
    }

    private OnClickListener printListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            dMac = SettingsHelper.getBluetoothAddress(getApplicationContext());

            Bitmap bm  =BitmapFactory.decodeFile(resFile.getAbsolutePath());

            printPhotoFromExternal(bm);

//
//
//
//            Log.i(TAG,"Print clicked: ");
//
//            Connection connection = new BluetoothConnection(dMac);
//
//            try {
//
//                helper.showLoadingDialog("Sending file to printer ...");
//                connection.open();
//
//
//                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
//                testSendFile(printer);
//                connection.close();
//            } catch (ConnectionException e) {
//                Log.e(TAG, "Print error: ", e);
//
//                helper.showErrorDialogOnGuiThread(e.getMessage());
//
//            } catch (ZebraPrinterLanguageUnknownException e) {
//                Log.e(TAG,"Print error: ", e);
//
//                helper.showErrorDialogOnGuiThread(e.getMessage());
//            } finally {
//                helper.dismissLoadingDialog();
//            }
//

        }
    };


//
//    private void testSendFile(ZebraPrinter printer) {
//        try {
//            //File filepath = getFileStreamPath("TEST.LBL");
////            printer.sendFileContents(resFile.getAbsolutePath());
////            SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
////            SettingsHelper.saveIp(this, getTcpAddress());
////            SettingsHelper.savePort(this, getTcpPortNumber());
//
//           //createDemoFile(printer, "alloy.pdf");
////            dataUri
//
////            InputStream is = getContentResolver().openInputStream(dataUri);
////            IOUtils.copy(is, new FileOutputStream(f) );
//
//            Log.i(TAG,"sending file: "+ resFile.getAbsolutePath());
//            printer.sendFileContents(resFile.getAbsolutePath());
////            SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
////            SettingsHelper.saveIp(this, getTcpAddress());
////            SettingsHelper.savePort(this, getTcpPortNumber());
//
//        } catch (ConnectionException e1) {
//            Log.e(TAG,"Print error: ", e1);
//
//            helper.showErrorDialogOnGuiThread("Error sending file to printer");
//        } catch (Exception e) {
//            Log.e(TAG,"Print error: ", e);
//
//            helper.showErrorDialogOnGuiThread("Error creating file");
//        }
//    }


    private OnClickListener selectPrinterListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ZebraPrintActivity.this, BluetoothDiscovery.class);
//            DialogFragment deviceListDialog  = new DeviceListDialogFragment("Available Devices","");
//            deviceListDialog.show(getSupportFragmentManager(),"Loading");
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

                //save Device
//                SharedPreferences prfs = getApplicationContext().getSharedPreferences("DEFAULT_SCIAPPS_DEVICE", Context.MODE_PRIVATE);
//
//                SharedPreferences.Editor editor = prfs.edit();
//                editor.putString(DEVICE_NAME, name);
//                editor.putString(DEVICE_ADDRESS, mac);
//                editor.commit();


                SettingsHelper.saveBluetoothAddress(this, mac);
                SettingsHelper.saveBluetoothName(this, name);
//            SettingsHelper.saveIp(this, getTcpAddress());
//            SettingsHelper.savePort(this, getTcpPortNumber());

                btn_selectPrinter.setText(name);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
}
