package com.fmm.doudizhu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class appOption {
    // 将应用最小化
    public static void minimizeApp(Activity activity) {
        activity.moveTaskToBack(true);
    }

    // 将应用恢复到前台
    public static void bringAppToFront(Context context, Activity activity) {
        Intent intent = new Intent(context, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
}