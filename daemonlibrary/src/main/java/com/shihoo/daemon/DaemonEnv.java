package com.shihoo.daemon;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;


/**
 * daemon 环境配置 每个进程独享一份 不同的进程，所有的静态和单例都会失效
 *
 * 这里将Daemon类 做成工具类
 *
 * 每一个进程都会有一个Daemon类
 *
 */
public final class DaemonEnv {

    /**
     * 向 WakeUpReceiver 发送带有此 Action 的广播, 即可在不需要服务运行的时候取消 Job / Alarm / Subscription.
     */
    static final String ACTION_CANCEL_JOB_ALARM_SUB = "com.shihoo.CANCEL_JOB_ALARM_SUB";

    static final int DEFAULT_WAKE_UP_INTERVAL = 2 * 60 * 1000; // 默认JobScheduler 唤醒时间为 2 分钟
    static final int MINIMAL_WAKE_UP_INTERVAL = 60 * 1000; // 最小时间为 1 分钟

    // 多进程时，尽量少用静态、单例 此处不得已
    public static Class<? extends AbsWorkService> mWorkServiceClass;


    static void startServiceMayBind(@NonNull final Context context,
                                    @NonNull final Class<? extends Service> serviceClass,
                                    @NonNull AbsServiceConnection connection,
                                    boolean isNeedStop) {
        if (isNeedStop){
            return;
        }
        // 判断当前绑定的状态
        if (!connection.mConnectedState) {
            Log.d("wsh-daemon", "启动并绑定服务 ："+serviceClass.getSimpleName());
            final Intent intent = new Intent(context, serviceClass);
            startServiceSafely(context, serviceClass,false);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }


    public static void startServiceSafely(Context context, Class<? extends Service> i,boolean isNeedStop) {
        if (isNeedStop){
            return;
        }
        Log.d("wsh-daemon", "安全启动服务。。: "+i.getSimpleName());
        try {
            context.startService(new Intent(context,i));
        } catch (Exception ignored) {

        }
    }

    static int getWakeUpInterval(int sWakeUpInterval) {
        return Math.max(sWakeUpInterval, MINIMAL_WAKE_UP_INTERVAL);
    }

    /**
     * 当前哪个进程使用的时候 就用其上下文发送广播
     * @param context
     */
    public static void stopAllServices(Context context) {
        if (context != null) {
            Log.d("wsh-daemon", "发送停止广播。。。。");
            // 以广播的形式通知所有进程终止
            context.sendBroadcast(new Intent(ACTION_CANCEL_JOB_ALARM_SUB));
        }
    }
}
