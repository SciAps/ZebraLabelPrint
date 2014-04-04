package com.sciaps.android.zebralabelprint.zebraprint;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sciaps.android.zebralabelprint.zebraprint.utils.DecimalRounder;
import com.sciaps.android.zebralabelprint.zebraprint.utils.SettingsHelper;
import com.sciaps.android.zebralabelprint.zebraprint.utils.UIHelper;
import com.sciaps.common.Alloy;
import com.sciaps.common.ChemResult;
import com.sciaps.common.libs.LIBAnalysisResult;
import com.sciaps.common.serialize.JsonSerializerFactory;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZebraPrintActivity extends ActionBarActivity {
    private static final String TAG = "ZebraPrintActivity";

    private UIHelper helper = new UIHelper(this);

    private Button btn_selectPrinter;
    private String dName;
    private String dMac;
    private Button btn_print;
    private Uri dataUri;
    private static Bitmap mPrintBm;
    private LIBAnalysisResult libsResult;

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


                InputStream is = getContentResolver().openInputStream(dataUri);
                libsResult  = loadResult(is);



            } catch (Exception e) {
                Log.e(TAG, "Error", e);
               // finish();
            }



        } else {
            Toast.makeText(getApplicationContext(), "Error in file description", Toast.LENGTH_LONG).show();

            //TEMP For testing purposes
            dataUri = Uri.parse("content://com.sciaps.libs.results/item/19/json");
            Log.e(TAG, "Share uri: "+dataUri);

            try {

            InputStream is = getContentResolver().openInputStream(dataUri);

                libsResult  = loadResult(is);
            } catch (IOException e) {
                e.printStackTrace();
                 finish();
            }


        }

        btn_selectPrinter = (Button) findViewById(R.id.btn_selectPrinter);
        btn_selectPrinter.setOnClickListener(selectPrinterListener);
        btn_print = (Button) findViewById(R.id.btn_print);
        btn_print.setOnClickListener(printListener);


        dName =SettingsHelper.getBluetoothName(getApplicationContext());
        if (dName.length() > 0) {
            btn_selectPrinter.setText(dName);
        }

         mPrintBm = createBitmapFromAnalysisResult();
        ImageView imv = (ImageView) findViewById(R.id.img_prev);
        imv.setImageBitmap(mPrintBm);


    }


    private SQLiteDatabase mDb;


    public static LIBAnalysisResult loadResult(InputStream is) throws IOException {
        LIBAnalysisResult retval = null;
        try {
            retval = JsonSerializerFactory.getSerializer(LIBAnalysisResult.class).deserialize(is);
        } catch (IOException e) {
            is.close();
            throw e;
        }
        return retval;
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


                    printer.printImage(new ZebraImageAndroid(bitmap), 0, 5, 385, 580, false);
                    connection.close();

                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                }
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
        return "0";//portNumberEditText.getText().toString();
    }


    private Bitmap createBitmapFromAnalysisResult(){

        List<String> segs = dataUri.getPathSegments();
        final String testIdName = segs.get(segs.size()-2);


        Bitmap b = Bitmap.createBitmap(290, 460, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);

        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);


        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        canvas.drawRect(new Rect(0,0,b.getWidth()-3,b.getHeight()-3),paint);
        paint.setColor(Color.WHITE);

        canvas.drawRect(new Rect(3,3,b.getWidth()-6,b.getHeight()-6),paint);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(20);


        canvas.drawText("Test #"+testIdName+" - "+libsResult.mTitle,15,30,mTextPaint);
        mTextPaint.setTextSize(16);

        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        canvas.drawText(df.format(libsResult.mTime.getTime()),15,47,mTextPaint);
        mTextPaint.setTextSize(20);

        canvas.drawText("(User)",15,65,mTextPaint);

        Alloy bestFingerprintMatch = libsResult.mBestAlloyMatches.get(0);
        String matchAlloy = bestFingerprintMatch.mName;
        String matchNumber = DecimalRounder.round(bestFingerprintMatch.getHitQuality());
        canvas.drawText(matchAlloy + " | #" + matchNumber, 15, 85, mTextPaint);

        if (libsResult.mBestAlloyMatches.size()>1){
            Alloy secondMatch  = libsResult.mBestAlloyMatches.get(1);
              matchAlloy = secondMatch.mName;
              matchNumber = DecimalRounder.round(secondMatch.getHitQuality());
            mTextPaint.setColor(Color.GRAY);
            mTextPaint.setTextSize(18);
            canvas.drawText("2nd Match: "+matchAlloy + " | #" + matchNumber, 15, 105, mTextPaint);

        }
        //add logos===========
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_z_bw);
        Rect rec = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect rec2 = new Rect(canvas.getWidth() - 80,  8, canvas.getWidth() - 8, 80);
        canvas.drawBitmap(icon,rec,rec2,paint);

        Bitmap icon2 = BitmapFactory.decodeResource(getResources(), R.drawable.sciaps_logo);
        rec = new Rect(0, 0, icon2.getWidth(), icon2.getHeight());
        float ratio = icon2.getWidth()/icon2.getHeight();

        int w=150;
        int h= (int) (w/ratio);
        rec2 = new Rect((canvas.getWidth()/2) - w/2,  canvas.getHeight()-10-h, (canvas.getWidth()/2) + w/2,  canvas.getHeight()-10);
        canvas.drawBitmap(icon2,rec,rec2,paint);


        //=====================
        Paint paint2 = new Paint();
        paint.setColor(Color.BLACK);
        paint2.setColor(Color.LTGRAY);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(24);

        Paint mTextPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint2.setColor(Color.BLACK);
        mTextPaint2.setTextSize(9);

        Paint mTextPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint3.setColor(Color.BLACK);
        mTextPaint3.setTextSize(18);


        ArrayList<ChemResult> chemResults = new ArrayList<ChemResult>(libsResult.getChemResults());

        Collections.sort(chemResults, ChemResult.ConcentrationDecend);

        int y =60;
        int x;
        for (int i=0;i<10&&i<chemResults.size(); i++){



            if (i%2==0){
                x =15;
                y+=58;
            }else {
                x = canvas.getWidth()/2;
            }

            paint.setStrokeWidth(3);
            canvas.drawRect(x, y, x + 43, y + 48, paint);
            canvas.drawRect(x+1, y+1, x+42, y+47, paint2);

            canvas.drawText(chemResults.get(i).element.symbol,x+3,y+38,mTextPaint);
            canvas.drawText(chemResults.get(i).element.atomicNumber + "", x + 30, y + 10, mTextPaint2);

            String formatted = chemResults.get(i).value>0? DecimalRounder.roundWPercent(chemResults.get(i).value):"<0.05%";

            canvas.drawText(formatted,x+54,y+42,mTextPaint3);

        }



        return b;
    };

    private OnClickListener printListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mPrintBm = createBitmapFromAnalysisResult();

            dMac = SettingsHelper.getBluetoothAddress(getApplicationContext());


            printPhotoFromExternal(mPrintBm);




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
