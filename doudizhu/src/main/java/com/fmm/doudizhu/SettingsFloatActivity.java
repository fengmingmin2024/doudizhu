package com.fmm.doudizhu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class SettingsFloatActivity extends Activity {

    private static final int REQUEST_CODE = 1; // 定义请求码

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // 请求悬浮窗权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // 已经有悬浮窗权限，可以继续操作
                startFloatingWindowService();
            }
        } else {
            // 对于低于 Android 6.0 的系统，不需要请求悬浮窗权限
            startFloatingWindowService();
        }
    }

    private void startFloatingWindowService() {
        Intent serviceIntent = new Intent(this, floatOption.class);
        this.startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 用户已经授予悬浮窗权限，可以继续操作
                    startFloatingWindowService();
                } else {
                    // 用户拒绝了悬浮窗权限
                    // 你可以在这里提示用户重新授权，或者采取其他措施
                    Toast.makeText(this, "悬浮窗权限尚未开启，无法启动！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
