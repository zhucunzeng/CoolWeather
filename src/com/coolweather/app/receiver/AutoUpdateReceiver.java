package com.coolweather.app.receiver;

import com.coolweather.app.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver {
	public static final String TAG = "AutoUpdateReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		LogUtil.i(TAG, "onReceive");
		context.startService(intent);
	}

}
