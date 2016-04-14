package com.coolweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

	public static final String CREATE_PROVINCE = "create table Province("
			+ "id integer primary key autoincrement" + "province_code text"
			+ "province_name text)";

	public static final String CREATE_CITY = "create table City("
			+ "id integer primary key autoincrement" + "city_code text"
			+ "city_name text" + "province_id integer)";

	public static final String CREATE_COUNTY = "create table County("
			+ "id integer primary key autoincrement" + "county_code text"
			+ "county_name text" + "city_id integer)";

	public CoolWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}