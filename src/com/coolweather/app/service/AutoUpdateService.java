package com.coolweather.app.service;

import java.nio.channels.AlreadyConnectedException;

import com.coolweather.app.activity.WeatherActivity;
import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.LogUtil;
import com.coolweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AutoUpdateService extends Service {
	public static final String TAG = "AutoUpdateService";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
		}).start();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int millis = 8 * 60 * 60 * 1000;	// 8 hours
		long triggerAtMillis = SystemClock.elapsedRealtime() + millis;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi);
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		
		HttpUtil.sendHttpGetRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				LogUtil.i(TAG, "onFinish, response: " + response);
				// TODO Auto-generated method stub
				Utility.handWeatherResponse(AutoUpdateService.this, response);
			}

			@Override
			public void onError(Exception e) {
				LogUtil.i(TAG, "onError");
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}
