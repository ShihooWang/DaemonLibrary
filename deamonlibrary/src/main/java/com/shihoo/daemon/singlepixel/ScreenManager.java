package com.shihoo.daemon.singlepixel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

/**
 * Created by shihoo ON 2018/12/12.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 *
 * 对1像素Activity进行防止内存泄露的处理，新建一个ScreenManager类
 */
public class ScreenManager {

    private static final String TAG = ScreenManager.class.getSimpleName();
    private static ScreenManager sInstance;
    private Context mContext;
    private WeakReference<Activity> mActivity;

    private ScreenManager(Context mContext) {
        this.mContext = mContext;
    }

    public static ScreenManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ScreenManager(context);
        }
        return sInstance;
    }

    /** 获得SinglePixelActivity的引用
     * @param activity
     */
    public void setSingleActivity(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    /**
     * 启动SinglePixelActivity
     */
    public void startActivity() {
        Intent intent = new Intent(mContext, SinglePixelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 结束SinglePixelActivity
     */
    public void finishActivity() {
        if (mActivity != null) {
            Activity activity = mActivity.get();
            if (activity != null) {
                activity.finish();
            }
        }
    }
}
