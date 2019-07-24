package com.shihoo.daemon.watch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import com.shihoo.daemon.AbsServiceConnection;
import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.ForegroundNotificationUtils;


public class WatchDogService extends Service {
    protected static final int HASH_CODE = 11222;
    protected static Disposable mDisposable;
    protected static PendingIntent mPendingIntent;
    private StopBroadcastReceiver stopBroadcastReceiver;
    private boolean isCanStartWatchDog;

    /**
     * 服务绑定相关的操作
     */
    private AbsServiceConnection mConnection = new AbsServiceConnection() {

        @Override
        public void onDisconnected(ComponentName name) {
            startBindWorkServices();
        }
    };

    private void startBindWorkServices(){
        if (WatchProcessPrefHelper.mWorkServiceClass!=null && isCanStartWatchDog) {
            DaemonEnv.startServiceMayBind(WatchDogService.this, WatchProcessPrefHelper.mWorkServiceClass, mConnection);
            DaemonEnv.startServiceSafely(WatchDogService.this,
                    PlayMusicService.class);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        isCanStartWatchDog = WatchProcessPrefHelper.getIsStartDaemon(this);
        if (!isCanStartWatchDog){
            stopSelf();
        }
        startRegisterReceiver();
        ForegroundNotificationUtils.startForegroundNotification(this);
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        onStart();
        return START_STICKY;
    }

    /**
     * 守护服务，运行在:watch子进程中
     */
    protected final void onStart() {
        if (mDisposable == null || mDisposable.isDisposed()) {
            //定时检查 AbsWorkService 是否在运行，如果不在运行就把它拉起来   Android 5.0+ 使用 JobScheduler，效果比 AlarmManager 好
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobInfo.Builder builder = new JobInfo.Builder(HASH_CODE,
                        new ComponentName(WatchDogService.this, JobSchedulerService.class));
                builder.setPeriodic(DaemonEnv.getWakeUpInterval(DaemonEnv.MINIMAL_WAKE_UP_INTERVAL));
                //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
                }
                builder.setPersisted(true);
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.schedule(builder.build());
            } else {
                //Android 4.4- 使用 AlarmManager
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent i = new Intent(WatchDogService.this, WatchProcessPrefHelper.mWorkServiceClass);
                mPendingIntent = PendingIntent.getService(WatchDogService.this, HASH_CODE, i, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + DaemonEnv.getWakeUpInterval(DaemonEnv.MINIMAL_WAKE_UP_INTERVAL),
                        DaemonEnv.getWakeUpInterval(DaemonEnv.MINIMAL_WAKE_UP_INTERVAL), mPendingIntent);
            }
            //使用定时 Observable，避免 Android 定制系统 JobScheduler / AlarmManager 唤醒间隔不稳定的情况
            mDisposable = Observable
                    .interval(DaemonEnv.getWakeUpInterval(DaemonEnv.MINIMAL_WAKE_UP_INTERVAL), TimeUnit.MILLISECONDS)
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            startBindWorkServices();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
            startBindWorkServices();
            //守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用
            getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WatchProcessPrefHelper.mWorkServiceClass.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }


    @Override
    public final IBinder onBind(Intent intent) {
        return new Messenger(new Handler()).getBinder();
    }

    private void onEnd() {
        Log.d("wsh-daemon", "onEnd ----  搞事 + IsShouldStopSelf  ：" + isCanStartWatchDog);
        if (isCanStartWatchDog){
            DaemonEnv.startServiceSafely(WatchDogService.this,WatchProcessPrefHelper.mWorkServiceClass);
            DaemonEnv.startServiceSafely(WatchDogService.this,WatchDogService.class);
        }
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd();
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd();
        stopRegisterReceiver();
    }


    /**
     * 停止运行本服务,本进程
     */
    private void stopService(){
        isCanStartWatchDog = false;
        WatchProcessPrefHelper.setIsStartSDaemon(this,false);
        cancelJobAlarmSub();
        if (mConnection.mConnectedState) {
            unbindService(mConnection);
        }
        exit();
    }

    private void exit(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        },2000);
    }


    private void startRegisterReceiver(){
        if (stopBroadcastReceiver == null){
            stopBroadcastReceiver = new StopBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DaemonEnv.ACTION_CANCEL_JOB_ALARM_SUB);
            registerReceiver(stopBroadcastReceiver,intentFilter);
        }
    }

    private void stopRegisterReceiver(){
        if (stopBroadcastReceiver != null){
            unregisterReceiver(stopBroadcastReceiver);
            stopBroadcastReceiver = null;
        }
    }

    /**
     * 用于在不需要服务运行的时候取消 Job / Alarm / Subscription.
     *
     * 因 WatchDogService 运行在 :watch 子进程, 请勿在主进程中直接调用此方法.
     * 而是向 WakeUpReceiver 发送一个 Action 为 WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB 的广播.
     */
    public void cancelJobAlarmSub() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler scheduler = (JobScheduler) WatchDogService.this.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(HASH_CODE);
        } else {
            AlarmManager am = (AlarmManager) WatchDogService.this.getSystemService(ALARM_SERVICE);
            if (mPendingIntent != null) {
                am.cancel(mPendingIntent);
            }
        }
        if (mDisposable !=null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    class StopBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            stopService();
        }
    }
}
