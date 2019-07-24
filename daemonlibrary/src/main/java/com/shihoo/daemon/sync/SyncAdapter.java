package com.shihoo.daemon.sync;

/**
 * Created by shihu.wang on 2017/4/10.
 * Email shihu.wang@bodyplus.cc
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.watch.WatchDogService;
import com.shihoo.daemon.watch.WatchProcessPrefHelper;


public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        DaemonEnv.startServiceSafely(mContext,WatchDogService.class);
    }
}