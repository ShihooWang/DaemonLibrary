package com.shihoo.daemonlibrary;

import android.app.Application;
import android.util.Log;

import com.shihoo.daemon.DaemonEnv;

/**
 * Created by shihoo ON 2018/12/13.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        // 每一次创建进程的时候都需要对Daemon环境进行初始化，所以这里没有判断进程


        String processName = ApkHelper.getProcessName(this.getApplicationContext());
        if ("com.shihoo.daemonlibrary".equals(processName)){
            // 主进程 进行一些其他的操作
            Log.d("wsh-daemon", "启动主进程");

        }else if ("com.shihoo.daemonlibrary:watch".equals(processName)){
            DaemonEnv.mWorkServiceClass = MainWorkService.class;
            Log.d("wsh-daemon", "启动了看门狗进程");
        }

    }
}
