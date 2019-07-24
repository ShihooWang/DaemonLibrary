package com.shihoo.daemon.watch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.ForegroundNotificationUtils;
import com.shihoo.daemon.R;

/**
 * Created by shihoo ON 2018/12/13.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 *
 * 后台播放无声音乐
 */
public class PlayMusicService extends Service {
    private boolean mNeedStop = false; //控制是否播放音频
    private MediaPlayer mMediaPlayer;
    private StopBroadcastReceiver stopBroadcastReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ForegroundNotificationUtils.startForegroundNotification(this);
        startRegisterReceiver();
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.no_notice);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPlayMusic();
        return START_STICKY;
    }

    private void startPlayMusic(){
        if (mMediaPlayer!=null && !mMediaPlayer.isPlaying() && !mNeedStop) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("wsh-daemon", "开始后台播放音乐");
                    mMediaPlayer.start();
                }
            }).start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            Log.d("wsh-daemon", "关闭后台播放音乐");
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        Log.d("wsh-daemon",  "----> stopPlayMusic ,停止服务");
        // 重启自己
        if (!mNeedStop) {
            Log.d("wsh-daemon",  "----> PlayMusic ,重启服务");
            Intent intent = new Intent(getApplicationContext(), PlayMusicService.class);
            startService(intent);
        }
    }

    private void startRegisterReceiver(){
        if (stopBroadcastReceiver == null){
            stopBroadcastReceiver = new StopBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DaemonEnv.ACTION_CANCEL_JOB_ALARM_SUB);
            registerReceiver(stopBroadcastReceiver,intentFilter);
        }
    }

    private void startUnRegisterReceiver(){
        if (stopBroadcastReceiver != null){
            unregisterReceiver(stopBroadcastReceiver);
            stopBroadcastReceiver = null;
        }
    }

    /**
     * 停止自己
     */
    private void stopService(){
        mNeedStop = true;
        stopPlayMusic();
        startUnRegisterReceiver();
        stopSelf();
    }

    class StopBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            stopService();
        }
    }
}
