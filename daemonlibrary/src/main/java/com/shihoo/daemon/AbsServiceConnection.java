package com.shihoo.daemon;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by shihoo ON 2018/12/14.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 *
 */
abstract class AbsServiceConnection implements ServiceConnection {

    // 当前绑定的状态
    boolean mConnectedState = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mConnectedState = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mConnectedState = false;
        onDisconnected(name);
    }

    @Override
    public void onBindingDied(ComponentName name) {
        onServiceDisconnected(name);
    }

    public abstract void onDisconnected(ComponentName name);
}
