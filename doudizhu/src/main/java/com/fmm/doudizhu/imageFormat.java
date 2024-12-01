package com.fmm.doudizhu;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class imageFormat {
    /**
     * Scales the given bitmap to fit within a square of size targetSize,
     * maintaining the aspect ratio, and fills the rest with black.
     *
     * @param bitmap The original bitmap to be scaled and filled.
     * @return A new bitmap that is scaled and filled into a square.
     */
    public static Bitmap scaleAndFillBitmap(Bitmap bitmap) {
        int targetSize = 640;

        // Calculate the scaling factor based on the larger dimension.
        float scale;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            scale = (float) targetSize / bitmap.getWidth();
        } else {
            scale = (float) targetSize / bitmap.getHeight();
        }

        // Calculate the dimensions after scaling.
        int scaledWidth = Math.round(bitmap.getWidth() * scale);
        int scaledHeight = Math.round(bitmap.getHeight() * scale);

        // Create a new bitmap with the target size.
        Bitmap newBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();

        // Draw a black background on the canvas.
        canvas.drawColor(Color.BLACK);

        // Calculate the position to center the scaled bitmap.
        int left = (targetSize - scaledWidth) / 2;
        int top = (targetSize - scaledHeight) / 2;

        // Draw the scaled bitmap onto the canvas at the calculated position.
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true), left, top, paint);

        return newBitmap;
    }
}