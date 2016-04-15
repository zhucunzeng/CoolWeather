package com.coolweather.app.activity;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.LogUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {

	public static final String TAG = "WeatherActivity";

	private LinearLayout llytWeatherInfo;
	private TextView tvCityName;
	private TextView tvPublishTime;
	private TextView tvWeatherDesp;
	private TextView tvHighTemp;
	private TextView tvLowTemp;
	private TextView tvCurrentDate;

	private Button btnSwitchCity;
	private Button btnRefreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LogUtil.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		initView();
		
		AdView adView = new AdView(this, AdSize.FIT_SCREEN);
		LinearLayout llytAd = (LinearLayout) findViewById(R.id.llyt_ad);
		llytAd.addView(adView);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			tvPublishTime.setText("同步中...");
			llytWeatherInfo.setVisibility(View.INVISIBLE);
			tvCityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	private void initView() {
		LogUtil.i(TAG, "initView");
		llytWeatherInfo = (LinearLayout) findViewById(R.id.llyt_weather_info);
		tvCityName = (TextView) findViewById(R.id.tv_city_name);
		tvPublishTime = (TextView) findViewById(R.id.tv_publish_time);
		tvWeatherDesp = (TextView) findViewById(R.id.tv_weather_desp);
		tvHighTemp = (TextView) findViewById(R.id.tv_high_temp);
		tvLowTemp = (TextView) findViewById(R.id.tv_low_temp);
		tvCurrentDate = (TextView) findViewById(R.id.tv_current_date);

		btnSwitchCity = (Button) findViewById(R.id.btn_switch_city);
		btnRefreshWeather = (Button) findViewById(R.id.btn_refresh_weather);
		btnSwitchCity.setOnClickListener(this);
		btnRefreshWeather.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		LogUtil.i(TAG, "onClick");
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.btn_refresh_weather:
			tvPublishTime.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;

		default:
			break;
		}
	}

	private void queryWeatherCode(String countyCode) {
		LogUtil.i(TAG, "queryWeatherCode, countyCode: " + countyCode);
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	private void queryWeatherInfo(String weatherCode) {
		LogUtil.i(TAG, "queryWeatherInfo, weatherCode: " + weatherCode);
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	private void queryFromServer(final String address, final String type) {
		LogUtil.i(TAG, "queryFromServer, address: " + address + " type: " + type);
		HttpUtil.sendHttpGetRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				LogUtil.i(TAG, "onFinish, response: " + response);
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				LogUtil.i(TAG, "onError");
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						tvPublishTime.setText("同步失败");
					}
				});
			}
		});
	}

	private void showWeather() {
		LogUtil.i(TAG, "showWeather");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		tvCityName.setText(prefs.getString("city_name", ""));
		tvPublishTime
				.setText("今天" + prefs.getString("publish_time", "") + "发布");
		tvHighTemp.setText(prefs.getString("high_temp", ""));
		tvLowTemp.setText(prefs.getString("low_temp", ""));
		tvWeatherDesp.setText(prefs.getString("weather_desp", ""));
		tvCurrentDate.setText(prefs.getString("current_date", ""));

		llytWeatherInfo.setVisibility(View.VISIBLE);
		tvCityName.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
}
