package com.shihoo.daemon.singlepixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by shihoo ON 2018/12/12.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 *
 * 对广播进行监听，封装为一个ScreenReceiverUtil类，进行锁屏解锁的广播动态注册监听
 */
public class ScreenReceiverUtil {
    private Context mContext;
    private SreenBroadcastReceiver mScreenReceiver;
    private ScreenManager mScreenManager;

    public ScreenReceiverUtil(Context mContext) {
        this.mContext = mContext;
    }

    public void startScreenReceiverListener() {
        // 动态启动广播接收器
        this.mScreenReceiver = new SreenBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenReceiver, filter);
        mScreenManager = ScreenManager.getInstance(mContext);
    }

    public void stopScreenReceiverListener() {
        if (null != mScreenReceiver) {
            mContext.unregisterReceiver(mScreenReceiver);
            mScreenReceiver = null;
        }
        mScreenManager = null;
    }

    public class SreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                if (mScreenManager != null) {
                    mScreenManager.startActivity();
                }
                Log.d("wsh-daemon", "打开了1像素Activity");
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                if (mScreenManager != null) {
                    mScreenManager.finishActivity(); // 解锁
                }
                Log.d("wsh-daemon", "关闭了1像素Activity");
            }
        }
    }
}
