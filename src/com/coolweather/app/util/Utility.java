package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

// handle response from server
public class Utility {
	public static final String TAG = "Utility";
	// handle provinces data
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		LogUtil.i(TAG, "*** response: " + response);
		if (!TextUtils.isEmpty(response)) {
			String[] provinces = response.split(",");
			if (provinces != null && provinces.length > 0) {
				for (String p : provinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);

					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	// handle cities data
	public synchronized static boolean handleCitiesResponse(
			CoolWeatherDB coolWeatherDB, String response, int provinceId) {
		LogUtil.i(TAG, "*** provinceId: " + provinceId + " response: " + response);
		if (!TextUtils.isEmpty(response)) {
			String[] cities = response.split(",");
			if (cities != null && cities.length > 0) {
				for (String c : cities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	// handle counties data
	public synchronized static boolean handleCountiesResponse(
			CoolWeatherDB coolWeatherDB, String response, int cityId) {
		LogUtil.i(TAG, "*** cityId: " + cityId + " response: " + response);
		if (!TextUtils.isEmpty(response)) {
			String[] counties = response.split(",");
			if (counties != null && counties.length > 0) {
				for (String c : counties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
