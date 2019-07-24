package com.shihoo.daemonlibrary;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.IntentWrapper;
import com.shihoo.daemon.watch.WatchProcessPrefHelper;

public class MainActivity extends Activity {

    //是否 任务完成, 不再需要服务运行? 最好使用SharePreference，注意要在同一进程中访问该属性
    public static boolean isCanStartWorkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:

                DaemonEnv.sendStartWorkBroadcast(this);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCanStartWorkService = true;
                        DaemonEnv.startServiceSafely(MainActivity.this,MainWorkService.class);
                    }
                },1000);

//                buildNotify(this);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.btn_stop:
                value ++;
//                buildNotify(this);
                DaemonEnv.sendStopWorkBroadcast(this);
                isCanStartWorkService = false;
                break;
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }

    private static final String CHANNEL_ID = "保活图腾";
    private static final int CHANNEL_POSITION = 1;
    private int value;

    private void buildNotify(Context service){
        NotificationManager manager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"主服务",
                    NotificationManager.IMPORTANCE_DEFAULT);
            //是否绕过请勿打扰模式
            channel.canBypassDnd();
            //闪光灯
            channel.enableLights(true);
            //锁屏显示通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            //闪关灯的灯光颜色
            channel.setLightColor(Color.RED);
            //桌面launcher的消息角标
            channel.canShowBadge();
            //是否允许震动
            channel.enableVibration(true);
            //获取系统通知响铃声音的配置
            channel.getAudioAttributes();
            //获取通知取到组
            channel.getGroup();
            //设置可绕过  请勿打扰模式
            channel.setBypassDnd(true);
            //设置震动模式
            channel.setVibrationPattern(new long[]{100, 100, 200});
            //是否会有灯光
            channel.shouldShowLights();
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(service,CHANNEL_ID)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }else {
            Notification notification = new Notification.Builder(service)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }
    }

}
