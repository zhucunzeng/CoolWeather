package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//�й�������  ��ѯ�����ַ
//http://www.weather.com.cn/data/list3/city.xml" ==> all provinces data in china
//http://www.weather.com.cn/data/list3/city" + provinceCode +".xml" ==> all citise data of the province
//http://www.weather.com.cn/data/list3/city" + cityCode +".xml" ==> all counties data of the city
//http://www.weather.com.cn/data/list3/city" + countyCode +".xml" ==> weatherCode of the county
//http://www.weather.com.cn/data/cityinfo" + weatherCode +".xml" ==> weather data that the county the weatherCode

public class HttpUtil {
	public static void sendHttpGetRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8 * 1000);
					connection.setReadTimeout(8 * 1000);

					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));

					StringBuilder response = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}

					if (listener != null) {
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					// TODO: handle exception
					if (listener != null) {
						listener.onError(e);
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
