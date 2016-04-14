package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	public static final String TAG = "CoolWeatherDB";
	public static final String DB_NAME = "CoolWeather";
	public static final int VERSION = 1;

	private static CoolWeatherDB mCoolWeatherDB = null;
	private static SQLiteDatabase db = null;

	private CoolWeatherDB(Context context) {
		LogUtil.i(TAG, "CoolWeatherDB");
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public synchronized static CoolWeatherDB getInstance(Context context) {
		LogUtil.i(TAG, "getInstance");
		if (mCoolWeatherDB == null) {
			mCoolWeatherDB = new CoolWeatherDB(context);
		}
		return mCoolWeatherDB;
	}

	public void saveProvince(Province province) {
		LogUtil.i(TAG, "saveProvince, province: " + province);
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_code", province.getProvinceCode());
			values.put("province_name", province.getProvinceName());
			db.insert("Province", null, values);
		}
	}

	public List<Province> loadProvince() {
		LogUtil.i(TAG, "loadProvince");
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		LogUtil.i(TAG, "list size: " + list.size());
		return list;
	}

	public void saveCity(City city) {
		LogUtil.i(TAG, "saveCity, city: " + city);
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_code", city.getCityCode());
			values.put("city_name", city.getCityName());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	public List<City> loadCity(int provinceId) {
		LogUtil.i(TAG, "loadCity, provinceId: " + provinceId);
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setProvinceId(cursor.getInt(cursor
						.getColumnIndex("province_id")));
				list.add(city);
			} while (cursor.moveToNext());
		}
		LogUtil.i(TAG, "list size: " + list.size());
		return list;
	}

	public void saveCounty(County county) {
		LogUtil.i(TAG, "saveCounty, county: " + county);
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_code", county.getCountyCode());
			values.put("county_name", county.getCountyName());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}

	public List<County> loadCounty(int cityId) {
		LogUtil.i(TAG, "loadCounty, cityId: " + cityId);
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
				list.add(county);
			} while (cursor.moveToNext());
		}
		LogUtil.i(TAG, "list size: " + list.size());
		return list;
	}

}
