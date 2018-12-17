package com.shihoo.daemonlibrary;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by shihoo ON 2018/12/13.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 */
public class ApkHelper {

    public static String getProcessName(Context context){
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null){
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo info : processes){
            if (info.pid == pid){
                return info.processName;
            }
        }
        return null;
    }
}
