package com.fmm.doudizhu;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ai.onnxruntime.OrtException;

public class doudizhu_main {

    private static View myFloatView;
    private static WindowManager myFloatWindow;
    private DetectOption detectOption;


    public doudizhu_main(String modelPath,Activity activity,Context context) throws IOException, OrtException {
        this.detectOption=new DetectOption(modelPath,context);
    }

    //获取屏幕截图
    public  static Bitmap getImage(Activity activity){
        return getScreen.takeScreenshot(activity);
    }

    //将图片格式化为以长边为基准，640*640px的正方形图片，空余部分填充为黑色，用于模型检测
    public  static Bitmap getIamgeFormated(Bitmap bitmap){
        return imageFormat.scaleAndFillBitmap(bitmap);
    }

    //显示悬浮窗
    public  static void showFloat(Context applicationContext,Activity activity){
        //检查是否已获取悬浮窗开启权限，如何已经获取那么显示悬浮窗，如果没有那么就弹出请求框
        if (hasPermissionFloat(applicationContext)) {
            Log.d("Screenshot", "权限已经开启");
            Intent intent = new Intent(applicationContext, floatOption.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startService(intent);
            }
        } else {
            // 悬浮窗权限尚未授予，请求用户授予权限
            Toast.makeText(applicationContext, "悬浮窗权限尚未开启，无法启动！", Toast.LENGTH_SHORT).show();
            Log.d("Screenshot", "权限尚未开启");
            Intent intent = createIntentForFloatPermission(applicationContext);
            activity.startActivity(intent);
            Toast.makeText(applicationContext, "请授予悬浮窗权限！", Toast.LENGTH_SHORT).show();

        }
    }

    //关闭悬浮窗
    public  static void closeFloat(Context applicationContext,Activity activity) throws JSONException {
        Intent updateIntent = new Intent(floatOption.ACTION_UPDATE_CONTENT);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","closeFloat");
        updateIntent.putExtra("message", jsonObject.toString());
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(updateIntent);
    }

    //修改悬浮窗显示内容
    public  static void sendServiceMessage(Context applicationContext,JSONObject jsonObject){
        Intent updateIntent = new Intent(floatOption.ACTION_UPDATE_CONTENT);
        updateIntent.putExtra("message", jsonObject.toString());
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(updateIntent);
    }

    //最小化应用
    public  static void hideMainApp(Activity activity){
        appOption.minimizeApp(activity);
    }

    //最大化应用
    public  static void showMainApp(Context context,Activity activity){
        appOption.bringAppToFront(context,activity);
    }


    //请求悬浮窗权限
    public static Intent createIntentForFloatPermission(Context applicationContext) {
        return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + applicationContext.getPackageName()));
    }

    //检查是否已经获取悬浮窗请求权限，返回true或者false
    public static Boolean hasPermissionFloat(Context applicationContext){
        return Settings.canDrawOverlays(applicationContext);
    }

    //检测图片，返回检测结果
    public static void detectImage(Activity activity,Context context) throws OrtException, IOException {
        // Bitmap bitmap= getImage(activity);
        //Bitmap newBitMap=getIamgeFormated(bitmap);
        //DetectOption detectOption=new DetectOption("yolov5s.onnx",960,960,context);
       //detectOption.detectObjects(newBitMap);
        DetectOption detectOption=new DetectOption("test.png","base.onnx",640,640,context);
    }

}
