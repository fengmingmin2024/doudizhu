package com.fmm.doudizhu;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;


public class getScreen {
    public static Bitmap takeScreenshot(Activity activity) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
