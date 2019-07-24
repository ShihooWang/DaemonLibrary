package com.shihoo.daemon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by Shihoo.Wang 2019/7/11
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class ForegroundNotificationUtils {
    // 通知渠道的id
    private static final String CHANNEL_ID = "保活图腾";
    private static final int CHANNEL_POSITION = 1;
    public static void startForegroundNotification(Service service){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //启动前台服务而不显示通知的漏洞已在 API Level 25 修复
            NotificationManager manager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel(CHANNEL_ID,"主服务",NotificationManager.IMPORTANCE_DEFAULT);
            Channel.enableLights(true);//设置提示灯
            Channel.setLightColor(Color.GREEN);//设置提示灯颜色
            Channel.setShowBadge(true);//显示logo
            Channel.setDescription("");//设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
            Channel.enableVibration(false);
            Channel.setSound(null,null);
            manager.createNotificationChannel(Channel);

            Notification notification = new Notification.Builder(service,CHANNEL_ID)
                    .setContentTitle("主服务")//标题
                    .setContentText("运行中...")//内容
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.icon1)//小图标一定需要设置,否则会报错(如果不设置它启动服务前台化不会报错,但是你会发现这个通知不会启动),如果是普通通知,不设置必然报错
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(),R.drawable.icon1))
                    .build();
            service.startForeground(CHANNEL_POSITION,notification);//服务前台化只能使用startForeground()方法,不能使用 notificationManager.notify(1,notification); 这个只是启动通知使用的,使用这个方法你只需要等待几秒就会发现报错了
        }else {
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
//            service.startForeground(Foreground_ID, new Notification());
            Notification notification = new Notification.Builder(service)
                    .setContentTitle("主服务")//设置标题
                    .setContentText("运行中...")//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(),R.drawable.icon1))//设置通知栏图标
                    .build();
            service.startForeground(CHANNEL_POSITION,notification);
        }
    }

    public static void deleteForegroundNotification(Service service){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (null != mChannel) {
                mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
            }
        }else {
            NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(CHANNEL_POSITION);
        }
    }


//    private void oldStartForegNotify(){
        //启动前台服务而不显示通知的漏洞已在 API Level 25 修复，大快人心！
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
//            //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
//            startForeground(HASH_CODE, new Notification());
//            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                Boolean needStartWorkService = needStartWorkService(null, 0, 0);
//                DaemonEnv.startServiceSafely(AbsWorkService.this,
//                        WorkNotificationService.class,
//                        needStartWorkService);
//            }
//        }
//    }

//    public static class WorkNotificationService extends Service {
//
//        /**
//         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
//         */
//        @Override
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            startForeground(1, new Notification());
//            stopSelf();
//            return START_STICKY;
//        }
//
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//    }
}
