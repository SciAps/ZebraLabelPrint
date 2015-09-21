package com.sciaps.android.zebra.tests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

import com.devsmart.android.BitmapUtils;
import com.sciaps.android.zebra.PrintActivity;
import com.sciaps.android.zebra.R;

public class PrintActivityTest extends ActivityInstrumentationTestCase2<PrintActivity> {

    public PrintActivityTest() {
        super(PrintActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent("com.sciaps.android.zebra.PRINT");

        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.RGB_565);
        intent.putExtra("bitmap", bitmap);
        setActivityIntent(intent);
    }

    public void testActivity() {
        PrintActivity activity = getActivity();
        assertNotNull(activity);

        ImageView preview = (ImageView) activity.findViewById(R.id.preview);
        assertNotNull(preview.getDrawable());

        getInstrumentation().waitForIdleSync();

    }
}
