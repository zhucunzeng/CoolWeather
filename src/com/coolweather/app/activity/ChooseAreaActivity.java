package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
import android.os.Process;
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		LogUtil.d(TAG, "onCrate");
		
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
				if (mCurrentLevel == LEVEL_PROVINCE) {
					mSelectedProvince = mProvinceList.get(index);
					queryCities();
				} else if (mCurrentLevel == LEVEL_CITY) {
					mSelectedCity = mCityList.get(index);
					queryCounties();
				}
			}
		});
		queryProvinces();
	}
	
	private void queryProvinces() {
		LogUtil.d(TAG, "queryProvinces");
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
		LogUtil.d(TAG, "queryCities");
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
		LogUtil.d(TAG, "queryCounties");
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
		LogUtil.d(TAG, "queryFromServer");
		String address = null;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		
		HttpUtil.sendHttpGetRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
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
		LogUtil.d(TAG, "showProgressDialog");
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("正在加载...");
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}
	
	private void closeProgressDialog() {
		LogUtil.d(TAG, "closeProgressDialog");
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		LogUtil.d(TAG, "onBackPressed");
		// Upon current level
		if (mCurrentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (mCurrentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}
