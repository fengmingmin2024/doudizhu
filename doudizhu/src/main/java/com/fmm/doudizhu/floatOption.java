package com.fmm.doudizhu;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;


public class floatOption extends Service {

    public static final String ACTION_UPDATE_CONTENT = "com.fmm.doudizhu.ACTION_UPDATE_CONTENT";
    private WindowManager windowManager;
    private View floatingView;

    @Override
    public void onCreate() {
        Log.d("Screenshot", "开始创建悬浮窗");
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_window, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        windowManager.addView(floatingView, params);

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        Button operationButton = floatingView.findViewById(R.id.operationButton);
        operationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当按钮被点击时执行的操作
                closeFloat();
                stopSelf();
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_UPDATE_CONTENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        stopSelf();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void closeFloat(){
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null; // 清空引用以避免内存泄漏
        }
        stopSelf();
    }

    private void updateFloatingWindowContent(Context context, Intent intent) throws JSONException {

        TextView upstreamRecord = floatingView.findViewById(R.id.upstreamRecord);
        TextView selfRecord = floatingView.findViewById(R.id.selfRecord);
        TextView downstreamRecord = floatingView.findViewById(R.id.downstreamRecord);

        if (upstreamRecord != null && selfRecord != null && downstreamRecord != null) {
            String str = intent.getStringExtra("message");
            JSONObject messageObject=new JSONObject(str);
            String messageType=  messageObject.getString("type");
            if(messageType.equals("closeFloat")){
                closeFloat();
            }else if(messageType.equals("changeCard")) {
                JSONObject record = messageObject.getJSONObject("data");
                String leftCards = record.getString("left");
                String myCards = record.getString("my");
                String rightCards = record.getString(("right"));
                upstreamRecord.setText(leftCards);
                selfRecord.setText(myCards);
                downstreamRecord.setText(rightCards);
            }
        } else {
            Log.e("FloatOptionService", "One or more views are null.");
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Button", "收到消息");
            String action = intent.getAction();
            if (action.equals(floatOption.ACTION_UPDATE_CONTENT)) {
                try {
                    updateFloatingWindowContent(context,intent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

}