package com.shihoo.daemonlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.IntentWrapper;
import com.shihoo.daemon.PlayMusicService;
import com.shihoo.daemon.WatchDogService;
import com.shihoo.daemon.WatchProcessPrefHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                MainWorkService.IsShouldStopService = false;
                WatchProcessPrefHelper.setIsStartSDaemon(this,true);
                DaemonEnv.startServiceSafely(this,new Intent(this,WatchDogService.class),true);

//                startService(new Intent(this,PlayMusicService.class));
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.btn_stop:
                MainWorkService.IsShouldStopService = true;
                WatchProcessPrefHelper.setIsStartSDaemon(this,false);
                DaemonEnv.stopAllServices(this);

//                bindService(new Intent(this, PlayMusicService.class), new ServiceConnection() {
//                    @Override
//                    public void onServiceConnected(ComponentName name, IBinder service) {
//                        Log.d("wsh-daemon", "启动并绑定服务 ：cg");
//                    }
//
//                    @Override
//                    public void onServiceDisconnected(ComponentName name) {
//                        Log.d("wsh-daemon", "启动并绑定服务 ：sb");
//                    }
//                },Context.BIND_AUTO_CREATE);
                break;
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }
}
