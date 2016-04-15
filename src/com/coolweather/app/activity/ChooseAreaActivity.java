package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.AdManager;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.LogUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final String TAG = "ChooseAreaActivity";
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog mProgressDialog = null;
	private TextView mTitleText = null;
	private ListView mListView = null;
	
	private ArrayAdapter<String> mAdapter = null;
	private CoolWeatherDB mCoolWeatherDB = null;
	
	private List<String> mDataList = new ArrayList<String>();
	private List<Province> mProvinceList = null;
	private List<City> mCityList = null;
	private List<County> mCountyList = null;
	
	private Province mSelectedProvince = null;
	private City mSelectedCity = null;
	private int mCurrentLevel = 0;
	
	private boolean isFromWeatherActivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String publishId = "3d8bc49a1fadc246";
		String appSecretKey = "9be23b83d25778da";
		AdManager.getInstance(this).init(publishId, appSecretKey, false);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_area);
		LogUtil.i(TAG, "onCrate");
		
		mListView = (ListView) findViewById(R.id.lv_area_list);
		mTitleText = (TextView) findViewById(R.id.tv_title);
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDataList);
		mCoolWeatherDB = CoolWeatherDB.getInstance(this);
		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				// TODO Auto-generated method stub
				LogUtil.i(TAG, "index: " + index);
				if (mCurrentLevel == LEVEL_PROVINCE) {
					mSelectedProvince = mProvinceList.get(index);
					queryCities();
				} else if (mCurrentLevel == LEVEL_CITY) {
					mSelectedCity = mCityList.get(index);
					queryCounties();
				} else if (mCurrentLevel == LEVEL_COUNTY) {
					String countyCode = mCountyList.get(index).getCountyCode();
					LogUtil.i(TAG, "countyCode: " + countyCode);
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	
	private void queryProvinces() {
		LogUtil.i(TAG, "queryProvinces");
		mProvinceList = mCoolWeatherDB.loadProvince();
		if (mProvinceList.size() > 0) {
			mDataList.clear();
			for (Province province : mProvinceList) {
				mDataList.add(province.getProvinceName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitleText.setText("中国");
			mCurrentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}
	
	private void queryCities() {
		LogUtil.i(TAG, "queryCities");
		mCityList = mCoolWeatherDB.loadCity(mSelectedProvince.getId());
		if (mCityList.size() > 0) {
			mDataList.clear();
			for (City city : mCityList) {
				mDataList.add(city.getCityName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitleText.setText(mSelectedProvince.getProvinceName());
			mCurrentLevel = LEVEL_CITY;
		} else {
			queryFromServer(mSelectedProvince.getProvinceCode(), "city");
		}
	}

	private void queryCounties() {
		LogUtil.i(TAG, "queryCounties");
		mCountyList = mCoolWeatherDB.loadCounty(mSelectedCity.getId());
		if (mCountyList.size() > 0) {
			mDataList.clear();
			for (County county : mCountyList) {
				mDataList.add(county.getCountyName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitleText.setText(mSelectedCity.getCityName());
			mCurrentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(mSelectedCity.getCityCode(), "county");
		}
	}
	
	private void queryFromServer(final String code, final String type) {
		LogUtil.i(TAG, "queryFromServer, code: " + code + " type: " + type);
		String address = null;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
//			if ("city".equals(type)) {
//				address = "http://m.weather.com.cn/manage/citylist/provshi.html?id=101" + code;
//			} else if ("county".equals(type)) {
//				address = "http://m.weather.com.cn/manage/citylist/station.html?id=101" + code;
//			}
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
			//address = "http://m.weather.com.cn/manage/citylist/china.html";
		}
		showProgressDialog();
		
		HttpUtil.sendHttpGetRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				LogUtil.i(TAG, "onFinish, response: " + response);
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(mCoolWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(mCoolWeatherDB, response, mSelectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(mCoolWeatherDB, response, mSelectedCity.getId());
				}
				
				if (result) {
					// Back to main Thread
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				LogUtil.i(TAG, "onError");
				// Back to main Thread
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void showProgressDialog() {
		LogUtil.i(TAG, "showProgressDialog");
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("正在加载...");
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}
	
	private void closeProgressDialog() {
		LogUtil.i(TAG, "closeProgressDialog");
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		LogUtil.i(TAG, "onBackPressed");
		// Upon current level
		if (mCurrentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (mCurrentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
