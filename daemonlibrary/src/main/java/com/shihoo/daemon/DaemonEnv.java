package com.shihoo.daemon;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shihoo.daemon.work.AbsWorkService;


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
    public static final String ACTION_START_JOB_ALARM_SUB = "com.shihoo.START_JOB_ALARM_SUB";
    public static final String ACTION_CANCEL_JOB_ALARM_SUB = "com.shihoo.CANCEL_JOB_ALARM_SUB";

    public static final int DEFAULT_WAKE_UP_INTERVAL = 2 * 60 * 1000; // 默认JobScheduler 唤醒时间为 2 分钟
    public static final int MINIMAL_WAKE_UP_INTERVAL = 60 * 1000; // 最小时间为 1 分钟

    public static void startServiceMayBind(@NonNull final Context context,
                                    @NonNull final Class<? extends Service> serviceClass,
                                    @NonNull AbsServiceConnection connection) {

        // 判断当前绑定的状态
        if (!connection.mConnectedState) {
            Log.d("wsh-daemon", "启动并绑定服务 ："+serviceClass.getSimpleName());
            final Intent intent = new Intent(context, serviceClass);
            startServiceSafely(context, serviceClass);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public static void startServiceSafely(Context context, Class<? extends Service> i) {
        Log.d("wsh-daemon", "安全启动服务。。: "+i.getSimpleName());
        try {
            if (Build.VERSION.SDK_INT >= 26){
                context.startForegroundService(new Intent(context,i));
            }else {
                context.startService(new Intent(context,i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getWakeUpInterval(int sWakeUpInterval) {
        return Math.max(sWakeUpInterval, MINIMAL_WAKE_UP_INTERVAL);
    }


    public static void safelyUnbindService(Service service, AbsServiceConnection mConnection){
        try{
            if (mConnection.mConnectedState) {
                service.unbindService(mConnection);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 当前哪个进程使用的时候 就用其上下文发送广播
     *
     * 如果是同一进程，可以自定义启动方式 不使用广播的模式
     */
    public static void sendStartWorkBroadcast(Context context) {
        Log.d("wsh-daemon", "发送开始广播。。。。");
        // 以广播的形式通知所有进程终止
        context.sendBroadcast(new Intent(ACTION_START_JOB_ALARM_SUB));
    }


    /**
     * 当前哪个进程使用的时候 就用其上下文发送广播
     */
    public static void sendStopWorkBroadcast(Context context) {
        Log.d("wsh-daemon", "发送停止广播。。。。");
        // 以广播的形式通知所有进程终止
        context.sendBroadcast(new Intent(ACTION_CANCEL_JOB_ALARM_SUB));
    }


}
