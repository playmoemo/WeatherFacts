package com.playmoemo.weatherfacts;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * databasen som holder på lagret vær.
 * den er implementert som Singleton, slik at det 
 * bare kan finnes en instans.
 */

public class WeatherStorage extends SQLiteOpenHelper {

	private static WeatherStorage sInstance;
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "weatherDB";
	private static final String TABLE_WEATHER = "weather";

	private static final String KEY_TIME = "time";
	private static final String KEY_CITY = "city";
	private static final String KEY_TEMPERATURE = "temperature";
	private static final String KEY_READABLE_TIME = "readableTime";

	// private constructor for å kontrollere instansieringen internt
	private WeatherStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// returnerer ny eller eksisterende instans.
	// resultat avhenger av om en instans finnes fra før
	public static WeatherStorage getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new WeatherStorage(context.getApplicationContext());
		}
		return sInstance;
	}

	// definerer opprettelsen av databasen.
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_WEATHER_TABLE = "CREATE TABLE " + TABLE_WEATHER + "("
				+ KEY_TIME + " INTEGER PRIMARY KEY," + KEY_CITY + " TEXT,"
				+ KEY_TEMPERATURE + " REAL," + KEY_READABLE_TIME + " TEXT"
				+ ")";

		db.execSQL(CREATE_WEATHER_TABLE);
	}

	// sletter en rad
	public void deleteWeather(long time) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.delete(TABLE_WEATHER, KEY_TIME + "=?",
					new String[] { String.valueOf(time) });
		} catch (Exception e) {
			Log.d("Trying to delete:", e.toString());
		} finally {
			db.close();
		}
	}

	// lagrer en værmelding
	public void addWeather(Weather weather) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		try {
			values.put(KEY_TIME, weather.getTime());
			values.put(KEY_CITY, weather.getCity());
			values.put(KEY_TEMPERATURE, weather.getTemperature());
			values.put(KEY_READABLE_TIME, weather.getReadableTime());

			// putter den nye raden inn i tabellen
			db.insertOrThrow(TABLE_WEATHER, null, values);
		} catch (SQLiteConstraintException ce) {
			Log.d("Dupblicate time", ce.toString());
		} catch (NullPointerException ex) {

			Log.i("addWeather", "fikk ikke lagret vær");
		} finally {
			db.close();
		}
	}


	// henter alt i databasen
	public List<Weather> getAllWeather() {
		List<Weather> weatherList = new ArrayList<Weather>();
		
		String selectQuery = "SELECT * FROM " + TABLE_WEATHER;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// bygger opp listen
		if (cursor.moveToFirst()) {
			do {
				Weather weather = new Weather();
				weather.setTime(cursor.getLong(0));
				weather.setCity(cursor.getString(1));
				weather.setTemperature(cursor.getDouble(2));
				weather.setReadableTime(cursor.getString(3));

				weatherList.add(weather);
			} while (cursor.moveToNext());
		}

		return weatherList;
	}

	// benyttes ikke
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}
