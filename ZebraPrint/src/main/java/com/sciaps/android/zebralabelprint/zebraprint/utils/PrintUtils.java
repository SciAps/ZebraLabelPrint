package com.sciaps.android.zebralabelprint.zebraprint.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Looper;

import com.sciaps.android.zebralabelprint.zebraprint.R;
import com.sciaps.common.Alloy;
import com.sciaps.common.ChemResult;
import com.sciaps.common.libs.LIBAnalysisResult;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by MonkeyFish on 4/14/14.
 */
public class PrintUtils {
    private final Activity context;
    private UIHelper helper;
    private PrintCallBack mPrintCallBack;

    public PrintUtils(Activity activity) {
        this.context = activity;
        helper = new UIHelper(activity);
    }

    public interface PrintCallBack {
        void onPrintSent();
    }

    private static Bitmap mBmp;

    public void setPrinterCallback(PrintCallBack pcb) {
        this.mPrintCallBack = pcb;
    }


    public Bitmap createTestBitmap() {

        mBmp = Bitmap.createBitmap(290, 460, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBmp);

        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);


        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        canvas.drawRect(new Rect(0, 0, mBmp.getWidth() - 3, mBmp.getHeight() - 3), paint);
        paint.setColor(Color.WHITE);

        canvas.drawRect(new Rect(3, 3, mBmp.getWidth() - 6, mBmp.getHeight() - 6), paint);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(50);

        canvas.drawText("TEST", 15, 100, mTextPaint);

        return mBmp;
    }

    ;


    public Bitmap createBitmapFromAnalysisResult(Context ctx, Uri dataUri, LIBAnalysisResult libsResult) {

        List<String> segs = dataUri.getPathSegments();
        final String testIdName = segs.get(segs.size() - 2);


        mBmp = Bitmap.createBitmap(290, 460, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBmp);

        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);


        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        canvas.drawRect(new Rect(0, 0, mBmp.getWidth() - 3, mBmp.getHeight() - 3), paint);
        paint.setColor(Color.WHITE);

        canvas.drawRect(new Rect(3, 3, mBmp.getWidth() - 6, mBmp.getHeight() - 6), paint);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(20);


        canvas.drawText("Test #" + testIdName + " - " + libsResult.mTitle, 15, 30, mTextPaint);
        mTextPaint.setTextSize(16);

        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        canvas.drawText(df.format(libsResult.mTime.getTime()), 15, 47, mTextPaint);
        mTextPaint.setTextSize(20);

        canvas.drawText("(User)", 15, 65, mTextPaint);

        Alloy bestFingerprintMatch = libsResult.mBestAlloyMatches.get(0);
        String matchAlloy = bestFingerprintMatch.mName;
        String matchNumber = DecimalRounder.round(bestFingerprintMatch.getHitQuality());
        canvas.drawText(matchAlloy + " | #" + matchNumber, 15, 85, mTextPaint);

        if (libsResult.mBestAlloyMatches.size() > 1) {
            Alloy secondMatch = libsResult.mBestAlloyMatches.get(1);
            matchAlloy = secondMatch.mName;
            matchNumber = DecimalRounder.round(secondMatch.getHitQuality());
            mTextPaint.setColor(Color.GRAY);
            mTextPaint.setTextSize(18);
            canvas.drawText("2nd Match: " + matchAlloy + " | #" + matchNumber, 15, 105, mTextPaint);

        }
        //add logos===========
        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_z_bw);
        Rect rec = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect rec2 = new Rect(canvas.getWidth() - 80, 8, canvas.getWidth() - 8, 80);
        canvas.drawBitmap(icon, rec, rec2, paint);

        Bitmap icon2 = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.sciaps_logo);
        rec = new Rect(0, 0, icon2.getWidth(), icon2.getHeight());
        float ratio = icon2.getWidth() / icon2.getHeight();

        int w = 150;
        int h = (int) (w / ratio);
        rec2 = new Rect((canvas.getWidth() / 2) - w / 2, canvas.getHeight() - 10 - h, (canvas.getWidth() / 2) + w / 2, canvas.getHeight() - 10);
        canvas.drawBitmap(icon2, rec, rec2, paint);


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

        int y = 60;
        int x;
        for (int i = 0; i < 10 && i < chemResults.size(); i++) {


            if (i % 2 == 0) {
                x = 15;
                y += 58;
            } else {
                x = canvas.getWidth() / 2;
            }

            paint.setStrokeWidth(3);
            canvas.drawRect(x, y, x + 43, y + 48, paint);
            canvas.drawRect(x + 1, y + 1, x + 42, y + 47, paint2);

            canvas.drawText(chemResults.get(i).element.symbol, x + 3, y + 38, mTextPaint);
            canvas.drawText(chemResults.get(i).element.atomicNumber + "", x + 30, y + 10, mTextPaint2);

            String formatted = chemResults.get(i).value > 0 ? DecimalRounder.roundWPercent(chemResults.get(i).value) : "<0.05%";

            canvas.drawText(formatted, x + 54, y + 42, mTextPaint3);

        }


        return mBmp;
    }


    public Bitmap createPortraitBitmapFromAnalysisResult(Context ctx, Uri dataUri, LIBAnalysisResult libsResult) {

        List<String> segs = dataUri.getPathSegments();
        final String testIdName = segs.get(segs.size() - 2);


        mBmp = Bitmap.createBitmap(460,290,  Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBmp);

        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);


        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        canvas.drawRect(new Rect(0, 0, mBmp.getWidth() - 3, mBmp.getHeight() - 3), paint);
        paint.setColor(Color.WHITE);

        canvas.drawRect(new Rect(3, 3, mBmp.getWidth() - 6, mBmp.getHeight() - 6), paint);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(20);


        canvas.drawText("Test #" + testIdName + " - " + libsResult.mTitle, 15, 30, mTextPaint);
        mTextPaint.setTextSize(16);

        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        canvas.drawText(df.format(libsResult.mTime.getTime()), 15, 47, mTextPaint);
        mTextPaint.setTextSize(20);

        canvas.drawText("(User)", 15, 65, mTextPaint);

//        Alloy bestFingerprintMatch = libsResult.mBestAlloyMatches.get(0);
//        String matchAlloy = bestFingerprintMatch.mName;
//        String matchNumber = DecimalRounder.round(bestFingerprintMatch.getHitQuality());
//        canvas.drawText(matchAlloy + " | #" + matchNumber, 15, 85, mTextPaint);
//
//        if (libsResult.mBestAlloyMatches.size() > 1) {
//            Alloy secondMatch = libsResult.mBestAlloyMatches.get(1);
//            matchAlloy = secondMatch.mName;
//            matchNumber = DecimalRounder.round(secondMatch.getHitQuality());
//            mTextPaint.setColor(Color.GRAY);
//            mTextPaint.setTextSize(18);
//            canvas.drawText("2nd Match: " + matchAlloy + " | #" + matchNumber, 15, 105, mTextPaint);
//
//        }
//        //add logos===========
//        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_z_bw);
//        Rect rec = new Rect(0, 0, icon.getWidth(), icon.getHeight());
//        Rect rec2 = new Rect(canvas.getWidth() - 80, 8, canvas.getWidth() - 8, 80);
//        canvas.drawBitmap(icon, rec, rec2, paint);
//
//        Bitmap icon2 = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.sciaps_logo);
//        rec = new Rect(0, 0, icon2.getWidth(), icon2.getHeight());
//        float ratio = icon2.getWidth() / icon2.getHeight();
//
//        int w = 150;
//        int h = (int) (w / ratio);
//        rec2 = new Rect((canvas.getWidth() / 2) - w / 2, canvas.getHeight() - 10 - h, (canvas.getWidth() / 2) + w / 2, canvas.getHeight() - 10);
//        canvas.drawBitmap(icon2, rec, rec2, paint);
//
//
//        //=====================
//        Paint paint2 = new Paint();
//        paint.setColor(Color.BLACK);
//        paint2.setColor(Color.LTGRAY);
//
//        mTextPaint.setColor(Color.BLACK);
//        mTextPaint.setTextSize(24);
//
//        Paint mTextPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint2.setColor(Color.BLACK);
//        mTextPaint2.setTextSize(9);
//
//        Paint mTextPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint3.setColor(Color.BLACK);
//        mTextPaint3.setTextSize(18);
//
//
//        ArrayList<ChemResult> chemResults = new ArrayList<ChemResult>(libsResult.getChemResults());
//
//        Collections.sort(chemResults, ChemResult.ConcentrationDecend);
//
//        int y = 60;
//        int x;
//        for (int i = 0; i < 10 && i < chemResults.size(); i++) {
//
//
//            if (i % 2 == 0) {
//                x = 15;
//                y += 58;
//            } else {
//                x = canvas.getWidth() / 2;
//            }
//
//            paint.setStrokeWidth(3);
//            canvas.drawRect(x, y, x + 43, y + 48, paint);
//            canvas.drawRect(x + 1, y + 1, x + 42, y + 47, paint2);
//
//            canvas.drawText(chemResults.get(i).element.symbol, x + 3, y + 38, mTextPaint);
//            canvas.drawText(chemResults.get(i).element.atomicNumber + "", x + 30, y + 10, mTextPaint2);
//
//            String formatted = chemResults.get(i).value > 0 ? DecimalRounder.roundWPercent(chemResults.get(i).value) : "<0.05%";
//
//            canvas.drawText(formatted, x + 54, y + 42, mTextPaint3);
//
//        }


        return mBmp;
    }




    public void printPhotoFromExternal(final String mac, final Uri dataUri, final LIBAnalysisResult libsResult) {
        new Thread(new Runnable() {
            public void run() {

                try {

                    if (libsResult != null) {
                        mBmp = createBitmapFromAnalysisResult(context, dataUri, libsResult);

                    } else {
                        mBmp = createTestBitmap();
                    }

                    helper.showLoadingDialog("Sending image to printer");

                    //   getAndSaveSettings();

                    Looper.prepare();
                    Connection connection = getZebraPrinterConn(mac);
                    connection.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);


                    printer.printImage(new ZebraImageAndroid(mBmp), 0, 5, 385, 580, false);
                    connection.close();


                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } finally {
                    mBmp.recycle();
                    helper.dismissLoadingDialog();
                    Looper.myLooper().quit();
                    if (mPrintCallBack != null) {
                        mPrintCallBack.onPrintSent();
                    }
                }
            }
        }).start();

    }

    private String getTcpPortNumber() {
        return "0";//portNumberEditText.getText().toString();
    }

    private Connection getZebraPrinterConn(String mac) {
        int portNumber;
        try {
            portNumber = Integer.parseInt(getTcpPortNumber());
        } catch (NumberFormatException e) {
            portNumber = 0;
        }
        return isBluetoothSelected() ? new BluetoothConnection(mac) : new TcpConnection(getTcpPortNumber(), portNumber);
    }

    private boolean isBluetoothSelected() {
        //return btRadioButton.isChecked();
        return true;
    }
}