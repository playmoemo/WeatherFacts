package com.playmoemo.weatherfacts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/*
 * laster ned værdata med WeatherTask,
 * og sender data til DataReceiver i DashBoardActivity
 * med SendDataThread
 */

public class TestService extends Service implements LocationListener {

	public static final String BROADCAST_ACTION = "com.playmoemo.servicetest.senddata";
	private double longitude, latitude;
	private LocationManager locationManager;
	private Location mLocation;
	private WeatherStorage db;
	private Weather mWeather;
	private String urlAddress;
	private String jsonWeatherData;
	private JSONObject mJSONObject;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		// åpner forbindelse med WeatherStorage
		db = WeatherStorage.getInstance(this);
		// kobler LocationManager opp mot tilkoblet nettverk
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				0, 0, this);
		
		return Service.START_NOT_STICKY;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("onCreate()", "skaper");
		// henter lokasjon fra tilkoblet nettverk
		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
	}
	
	
	public void onStart(Intent intent, int startId) {
		Log.d("onStart()", "starter");
	};
	
	// egen tråd for å sende data til DataReceiver i DashBoardActivity
	private class SendDataThread extends Thread {
		@Override
		public void run() {
			
			try {
				Thread.sleep(1000);
				
				saveToDB(mWeather);
				
				Intent intent = new Intent();
				intent.setAction(BROADCAST_ACTION);
				intent.putExtra("city", mWeather.getCity());
				intent.putExtra("temperature", mWeather.getTemperature());
				intent.putExtra("readableTime", mWeather.getReadableTime());
				intent.putExtra("icon", mWeather.getIcon());
				
				// sender data til DataReceiver
				sendBroadcast(intent);
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch(NullPointerException ex){
				Log.i("thread", "får ikke sendt boradcast");
			}
			// stopper tråd etter sending av data
			stopSelf();
		}
	}
	
	// lagrer informasjon i Weather-objekt til WeatherStorage
	private void saveToDB(Weather weather) {
		db.addWeather(weather);
	}
	
	// bygger opp URL for å hente JSON-data fra openweathermap.org
	public String createURL(double lat, double lon) {
		String baseURL = "http://api.openweathermap.org/data/2.5/weather?lat=";
		String midleURL = "&lon=";
		// avslutter URL med 'metric' for å få metriske verdier
		String endURL = "&units=metric";
		String fullURL = baseURL + lat + midleURL + lon + endURL;
		
		return fullURL;
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("onDestroy()", "destruerer");
		// stopper å lytte til oppdateringer for lokasjon
		locationManager.removeUpdates(this);
	}

	// benyttes ikke
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	// tilhører LocationListener
	@Override
	public void onLocationChanged(Location location) {
		Log.d("i onLocationChanged()", "coords tilgjengelige her");
		
		this.mLocation = location;
		
		longitude = mLocation.getLongitude();
		latitude = mLocation.getLatitude();
		Log.d("i onLocationChanged()", longitude + " " + latitude);
		
		urlAddress = createURL(latitude, longitude);
		
		// starter AsyncTask for å hente JSON-data her
		startTask();
		
	}
	// tilhører LocationListener
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	// tilhører LocationListener
	@Override
	public void onProviderEnabled(String provider) {
		// gjelder GPS, benyttes ikke
	}
	// tilhører LocationListener
	@Override
	public void onProviderDisabled(String provider) {
		// gjelder GPS, benyttes ikke
	}
	
	// kommunikasjon med Openweahtermap.org
	private class WeatherTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			String jsonData = "";
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet getRequest = new HttpGet(url);

				try {
					HttpResponse response = client.execute(getRequest);
					InputStream jsonStream = response.getEntity().getContent();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(jsonStream));
					StringBuilder builder = new StringBuilder();
					String line = "";
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					jsonData = builder.toString();

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return jsonData;
		}

		@Override
		protected void onPostExecute(String result) {
			jsonWeatherData = result;
			Log.d("AsyncTask Output", jsonWeatherData);
			
			// setter verdier til weather-objekt
			mJSONObject = fetchJSON();

			try {
				readWeather(mJSONObject);
				//-------------------------------------------------------
				SendDataThread dataThread = new SendDataThread();
				Log.d("onPostExecute", "lager og starter tråd");
				dataThread.start();
				//-------------------------------------------------------
			} catch (JSONException e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}
	}
	
	
	
	private void startTask() {
		WeatherTask task = new WeatherTask();
		task.execute(new String[] { urlAddress });
	}
	
	
	
	public JSONObject fetchJSON() {
		JSONObject jo = null;
		try {
			jo = new JSONObject(jsonWeatherData);
		} catch(JSONException je) {
			je.printStackTrace();
			return null;
		}
		return jo;
	}
	
		
	public void readWeather(JSONObject jObject) throws JSONException {
		
		try{
			String city = jObject.getString("name");
			double temp = jObject.getJSONObject("main").getDouble("temp");
			temp = Math.round(temp);
			long time = jObject.getLong("dt");
			String icon = jObject.getJSONArray("weather").getJSONObject(0).getString("icon");
			
			mWeather = new Weather(time, city, temp);
			mWeather.setLongitude(longitude);
			mWeather.setLatitude(latitude);
			mWeather.setIcon(icon);
			Log.d("city, temp", city + " " + temp);
			
			// lager dato og klokkeslett som lesbar text
			createReadableTime(time);
			
		}catch(NullPointerException ex){
			Toast.makeText(getApplicationContext(), "Kunne ikke hente værdata", Toast.LENGTH_LONG).show();
		}
	}
	
	
	private void createReadableTime(long time) {
		long timeMillisec = time * 1000;
		Date date = new Date(timeMillisec);
		String readable = new SimpleDateFormat("dd. MMMM. yyyy. H:mm").format(date);
		mWeather.setReadableTime(readable);
	}
}
