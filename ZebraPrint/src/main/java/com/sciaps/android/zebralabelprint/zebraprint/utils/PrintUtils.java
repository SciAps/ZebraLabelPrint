package com.sciaps.android.zebralabelprint.zebraprint.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Looper;

import com.sciaps.android.zebralabelprint.zebraprint.R;
import com.sciaps.common.algorithms.GradeMatchRanker;
import com.sciaps.common.calculation.libs.EmpiricalCurveCalc;
import com.sciaps.common.data2.ChemResult;
import com.sciaps.common.data2.ChemResultItem;
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

import static com.sciaps.android.zebralabelprint.zebraprint.PrintTypeDialog.Types.Landscape3X2;
import static com.sciaps.android.zebralabelprint.zebraprint.PrintTypeDialog.Types.Portrait2X3;

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

        void onPrintError(Exception e);
    }

    private static Bitmap mBmp;

    public void setPrinterCallback(PrintCallBack pcb) {
        this.mPrintCallBack = pcb;
    }


    public Bitmap createTestBitmap(Context ctx) {

        int type = SettingsHelper.getPrintType(ctx);
        mBmp = null;
        if (type == Landscape3X2.ordinal()) {
            mBmp = create3X2TestBitmap();
        } else if (type == Portrait2X3.ordinal()) {
            mBmp = create2X3TestBitmap();

        }
        return mBmp;
    }

    private Bitmap create2X3TestBitmap() {
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

    private Bitmap create3X2TestBitmap() {
        mBmp = Bitmap.createBitmap(460, 290, Bitmap.Config.ARGB_8888);
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
        mTextPaint.setTextSize(60);

        canvas.drawText("TEST", 200, 150, mTextPaint);
        return fixOrientation(mBmp);
    }

    public static Bitmap fixOrientation(Bitmap mBitmap) {

        int rotate = 90;


        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                mBitmap.getHeight(), matrix, true);


        return mBitmap;
    }

    public Bitmap createBitmapFromAnalysisResult(Context ctx, Uri dataUri, LIBAnalysisResult libsResult) {

        int type = SettingsHelper.getPrintType(ctx);
        mBmp = null;
        if (type == Landscape3X2.ordinal()) {
            mBmp = create3X2Label(ctx, dataUri, libsResult);
        } else if (type == Portrait2X3.ordinal()) {
            mBmp = create2X3Label(ctx, dataUri, libsResult);

        }

        return mBmp;

    }


    private Bitmap create2X3Label(Context ctx, Uri dataUri, LIBAnalysisResult libsResult) {

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

        String title = libsResult.mTitle != null ? " - " + libsResult.mTitle : "";

        canvas.drawText("Test #" + testIdName + title, 15, 30, mTextPaint);
        mTextPaint.setTextSize(16);

        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        canvas.drawText(df.format(libsResult.mTime.getTime()), 15, 47, mTextPaint);
        mTextPaint.setTextSize(20);

      // canvas.drawText("(User)", 15, 65, mTextPaint);



        ArrayList<ChemResultItem> chemResults = new ArrayList<ChemResultItem>(libsResult.mResult.chemResults.size());
        final GradeMatchRanker.GradeRank bestGrade;



        if (libsResult.mResult.gradeRanks != null && libsResult.mResult.gradeRanks.size() > 0) {
            bestGrade = libsResult.mResult.gradeRanks.get(0);
            for (EmpiricalCurveCalc.EmpiricalCurveResult r : libsResult.mResult.chemResults) {
                ChemResultItem i = new  ChemResultItem();
                i.chemResult = new ChemResult(r.element);
                i.chemResult.value = (float) r.percent;
                i.chemResult.error = (float) r.error;
                if (bestGrade != null) {
                    i.gradeRange = bestGrade.grade.spec.get(i.chemResult.element);
                }
                chemResults.add(i);
            }
            Collections.sort(chemResults, ChemResultItem.ConcentrationDecend);

            //Alloy bestFingerprintMatch = libsResult..mBestAlloyMatches.get(0);
            String matchAlloy = bestGrade.grade.getDisplayName();//bestFingerprintMatch.mName;
            String matchNumber = bestGrade.matchNumber > 0 ? DecimalRounder.round(bestGrade.matchNumber) : "0";
            canvas.drawText(matchAlloy + " | #" + matchNumber, 15, 68, mTextPaint);

            Paint mTextPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(Color.GRAY);
            mTextPaint.setTextSize(14);
            canvas.drawText("Base: " + bestGrade.grade.getBase().name, 15, 85, mTextPaint);


            if (libsResult.mResult.gradeRanks.size() > 1) {
                GradeMatchRanker.GradeRank secondGrade = libsResult.mResult.gradeRanks.get(1);

                //Alloy secondMatch = libsResult.mBestAlloyMatches.get(1);
                matchAlloy = secondGrade.grade.getDisplayName();
                matchNumber = secondGrade.matchNumber > 0 ? DecimalRounder.round(bestGrade.matchNumber) : "0";

                mTextPaint.setColor(Color.GRAY);
                mTextPaint.setTextSize(18);
                canvas.drawText("2nd Match: " + matchAlloy + " | #" + matchNumber, 15, 105, mTextPaint);

            }
        } else {
            bestGrade = null;
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

//            Bitmap elem =Bitmap.createScaledBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.element), 55,50, false) ;
//            Rect rec3 = new Rect(0, 0, 55, 50);
//            Rect rec4= new Rect( x, y, x + 55, y + 50);
//            canvas.drawBitmap(elem, rec3, rec4, paint);
//


            canvas.drawText(chemResults.get(i).chemResult.element.symbol, x + 3, y + 38, mTextPaint);
            canvas.drawText(chemResults.get(i).chemResult.element.atomicNumber + "", x + 30, y + 10, mTextPaint2);

          //  canvas.drawText(chemResults.get(i).chemResult.element.name(), x + 3, y + 3, mTextPaint4);


            String formatted = chemResults.get(i).chemResult.value > 0 ? DecimalRounder.roundWPercent(chemResults.get(i).chemResult.value) : "<0.05%";

            canvas.drawText(formatted, x + 54, y + 42, mTextPaint3);

        }


        return mBmp;
    }

    private Bitmap create3X2Label(Context ctx, Uri dataUri, LIBAnalysisResult libsResult) {


        List<String> segs = dataUri.getPathSegments();
        final String testIdName = segs.get(segs.size() - 2);


        mBmp = Bitmap.createBitmap(460, 290, Bitmap.Config.ARGB_8888);
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
        mTextPaint.setTextSize(30);

        String title = libsResult.mTitle != null ? " - " + libsResult.mTitle : "";
        canvas.drawText("Test #" + testIdName + title, 15, 34, mTextPaint);
        mTextPaint.setTextSize(25);
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        canvas.drawText(df.format(libsResult.mTime.getTime()), 15, 60, mTextPaint);

        mTextPaint.setTextSize(80);
        mTextPaint.setFakeBoldText(true);

        GradeMatchRanker.GradeRank bestGrade;
        if (libsResult.mResult.gradeRanks != null && libsResult.mResult.gradeRanks.size() > 0) {
            bestGrade = libsResult.mResult.gradeRanks.get(0);


            //Alloy bestFingerprintMatch = libsResult..mBestAlloyMatches.get(0);
            String matchAlloy = bestGrade.grade.getDisplayName();//bestFingerprintMatch.mName;


            String matchNumber = bestGrade.matchNumber > 0 ? DecimalRounder.round(bestGrade.matchNumber) : "0";

            canvas.drawText(matchAlloy, 15, 140, mTextPaint);
            mTextPaint.setTextSize(50);
            mTextPaint.setFakeBoldText(false);

            canvas.drawText("#" + matchNumber, 35, 195, mTextPaint);


            if (libsResult.mResult.gradeRanks.size() > 1) {
                GradeMatchRanker.GradeRank secondGrade = libsResult.mResult.gradeRanks.get(1);

                //Alloy secondMatch = libsResult.mBestAlloyMatches.get(1);
                matchAlloy = secondGrade.grade.name;
                matchNumber = secondGrade.matchNumber > 0 ? DecimalRounder.round(bestGrade.matchNumber) : "0";

                mTextPaint.setColor(Color.GRAY);
                mTextPaint.setTextSize(22);
                canvas.drawText("2nd Match: " + matchAlloy + " | #" + matchNumber, 15, 225, mTextPaint);

            }
        } else {
            bestGrade = null;
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


        return fixOrientation(mBmp);
    }


    public void printPhotoFromExternal(final String mac, final Uri dataUri, final LIBAnalysisResult libsResult) {
        new Thread(new Runnable() {
            public void run() {

                try {

                    if (libsResult != null) {
                        mBmp = createBitmapFromAnalysisResult(context, dataUri, libsResult);

                    } else {
                        mBmp = createTestBitmap(context);
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
                    //helper.showErrorDialogOnGuiThread(e.getMessage());
                    mPrintCallBack.onPrintError(e);
                } catch (ZebraPrinterLanguageUnknownException e) {
                    //helper.showErrorDialogOnGuiThread(e.getMessage());
                    mPrintCallBack.onPrintError(e);
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
