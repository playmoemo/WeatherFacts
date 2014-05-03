package com.playmoemo.weatherfacts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherForecastActivity extends ActionBarActivity{

	private LocationManager locationManager;
	private LocationListener locationListener;
	private TextView textViewLocation;
	private ListView listView;
	private Weather weather;

	private WeatherAdapter wAdapter;
	private ArrayList<Weather> weatherList;
	private double latitude, longitude;
	private String urlAddress;
	private String jsonWeatherData;
	private String icon;
	private String dateAndTime;
	private boolean hasInternetConnection;

	private String[] iconNames = { "01d", "02d", "03d", "04d", "09d", "10d",
			"11d", "13d", "50d", "01n", "02n", "03n", "04n", "09n", "10n",
			"11n", "13n", "50n" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_forecast);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		textViewLocation = (TextView) findViewById(R.id.textViewLocation);
		listView = (ListView) findViewById(R.id.weather_forecast_list);

		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

		weather = new Weather();

	}
	
	

	public void startTask() {
		WeatherForecastTask task = new WeatherForecastTask();
		task.execute(new String[] { urlAddress });
	}

	private class WeatherForecastTask extends AsyncTask<String, Void, String> {

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

			fetchJSON();
			super.onPostExecute(result);
		}
	}

	// Create string for HttpRequest in WeatherTask
	public String createURL(double lat, double lon) {
		String baseURL = "http://api.openweathermap.org/data/2.5/forecast?lat=";
		String midleURL = "&lon=";
		String endURL = "&units=metric";
		String fullURL = baseURL + lat + midleURL + lon + endURL;

		return fullURL;
	}

	// Collects coordinates from current location
	private void getCoordinates() {
		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				
			}

			@Override
			public void onLocationChanged(Location location) {
				storeLocation(location);
			}
		};

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 60*60000, 0, locationListener);
	}
	
	

	public void storeLocation(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();

		weather.setLatitude(latitude);
		weather.setLongitude(longitude);

		urlAddress = createURL(latitude, longitude);
		
		startTask();
	}

	public void fetchJSON() {
		try {
			weatherList = readWeather(parseJSON(jsonWeatherData));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// throws IOException
	public JSONObject parseJSON(String json) {
		JSONObject jObject = null;

		try {
			jObject = new JSONObject(json);
		} catch (JSONException jex) {
			jex.printStackTrace();
			return null;
		}
		return jObject;
	}

	public ArrayList<Weather> readWeather(JSONObject jObject)
			throws JSONException {

		try {
			String city = jObject.getString("city");
			JSONObject cityObject = new JSONObject(city);
			city = cityObject.getString("name");
			JSONArray jArray = jObject.getJSONArray("list");
			weather.setCity(city);
			textViewLocation.setText(city);
			ArrayList<Weather> weatherList = new ArrayList<Weather>();
			for (int n = 0; n < jArray.length(); n++) {
				JSONObject jObject2 = jArray.getJSONObject(n);

				dateAndTime = jObject2.getString("dt_txt");
				double celcius = jObject2.getJSONObject("main").getDouble(
						"temp");
				celcius = Math.round(celcius);
				double kelvin = Math.round(celcius + 273.15);
				double farenheit = Math.round((celcius * 1.800) + 32);
				icon = jObject2.getJSONArray("weather").getJSONObject(0)
						.getString("icon");

				Weather weather = new Weather(city, celcius, farenheit, kelvin);

				weather.setIcon(icon);
				weather.setReadableTime(dateAndTime);

				weatherList.add(weather);
			}

			if (wAdapter == null) {

				wAdapter = new WeatherAdapter(this, weatherList);
				listView.setAdapter(wAdapter);
			} else {

				wAdapter.notifyDataSetChanged();
			}
		} catch (NullPointerException ex) {

			Toast.makeText(getApplicationContext(),
					"Ingen nettverksforbindelse", Toast.LENGTH_LONG).show();
		}

			return weatherList;
		
	}

	private Drawable findIcon(String icon) {
		Drawable drawable = null;
		int neededIcon = java.util.Arrays.asList(iconNames).indexOf(icon);

		switch (neededIcon) {
		case 0:
			drawable = getResources().getDrawable(R.drawable.sky_is_clear_d);
			break;
		case 1:
			drawable = getResources().getDrawable(R.drawable.few_clouds_d);
			break;
		case 2:
			drawable = getResources()
					.getDrawable(R.drawable.scattered_clouds_d);
			break;
		case 3:
			drawable = getResources().getDrawable(R.drawable.broken_clouds_d);
			break;
		case 4:
			drawable = getResources().getDrawable(R.drawable.shower_rain_d);
			break;
		case 5:
			drawable = getResources().getDrawable(R.drawable.rain_d);
			break;
		case 6:
			drawable = getResources().getDrawable(R.drawable.thunderstorm_d);
			break;
		case 7:
			drawable = getResources().getDrawable(R.drawable.snow_d);
			break;
		case 8:
			drawable = getResources().getDrawable(R.drawable.mist_d);
			break;
		case 9:
			drawable = getResources().getDrawable(R.drawable.sky_is_clear_n);
			break;
		case 10:
			drawable = getResources().getDrawable(R.drawable.few_clouds_n);
			break;
		case 11:
			drawable = getResources()
					.getDrawable(R.drawable.scattered_clouds_n);
			break;
		case 12:
			drawable = getResources().getDrawable(R.drawable.broken_clouds_n);
			break;
		case 13:
			drawable = getResources().getDrawable(R.drawable.shower_rain_n);
			break;
		case 14:
			drawable = getResources().getDrawable(R.drawable.rain_n);
			break;
		case 15:
			drawable = getResources().getDrawable(R.drawable.thunderstorm_n);
			break;
		case 16:
			drawable = getResources().getDrawable(R.drawable.snow_n);
			break;
		case 17:
			drawable = getResources().getDrawable(R.drawable.mist_n);
			break;
		}

		return drawable;
	}

	private class WeatherAdapter extends BaseAdapter {

		ArrayList<Weather> wList = new ArrayList<Weather>();
		LayoutInflater layoutInflater;
		Context context;

		public WeatherAdapter(Activity context, ArrayList<Weather> list) {
			this.context = context;
			this.wList = list;
			layoutInflater = LayoutInflater.from(this.context);

		}

		@Override
		public int getCount() {

			return wList.size();
		}

		@Override
		public Object getItem(int position) {

			return wList.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CompleteListViewHolder viewHolder;

			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.weather_list_item, null);
				viewHolder = new CompleteListViewHolder();
				convertView.setTag(viewHolder);

			} else {

				viewHolder = (CompleteListViewHolder) convertView.getTag();
			}

			viewHolder.textViewTime = detailTime(convertView,
					R.id.textViewTime, wList.get(position).getReadableTime());
			viewHolder.imageViewIcon = icondetail(convertView,
					R.id.imageViewIcons, wList.get(position).getIcon());
			viewHolder.textViewCelcius = detailCelcius(convertView,
					R.id.textViewCelcius, wList.get(position).getTemperature());
			viewHolder.textViewFarenheit = detailFarenheit(convertView,
					R.id.textViewFarenheit, wList.get(position).getFarenheit());
			viewHolder.textViewKelvin = detailKelvin(convertView,
					R.id.textViewKelvin, wList.get(position).getKelvin());
			return convertView;
		}

		private TextView detailTime(View v, int resId, String string) {
			TextView tv = (TextView) v.findViewById(resId);
			String dateAndTime = String.valueOf(string);
			String dateAndTimesplit[] = dateAndTime.split("\\s+");
			String date = dateAndTimesplit[0];
			String time = dateAndTimesplit[1];
			String timeArray[] = time.split(":");
			String hour = timeArray[0];
			String minute = timeArray[1];

			tv.setText("Dato: " + String.valueOf(formateDate(date))
					+ "\nKlokkeslett: " + String.valueOf(hour) + ":"
					+ String.valueOf(minute));
			return tv;
		}

		private TextView detailCelcius(View v, int resId, double temp) {
			TextView tv = (TextView) v.findViewById(resId);
			String temprature = String.valueOf(temp);

			tv.setText(String.valueOf(temprature + " " + (char) 0x00B0 + "C"));
			return tv;
		}

		private TextView detailFarenheit(View v, int resId, double temp) {
			TextView tv = (TextView) v.findViewById(resId);
			String temprature = String.valueOf(temp);

			tv.setText(String.valueOf(temprature + " " + (char) 0x00B0 + "F"));
			return tv;
		}

		private TextView detailKelvin(View v, int resId, double temp) {
			TextView tv = (TextView) v.findViewById(resId);
			String temprature = String.valueOf(temp);

			tv.setText(String.valueOf(temprature + " " + (char) 0x00B0 + "K"));
			return tv;
		}

		private ImageView icondetail(View v, int resId, String string) {

			ImageView imageViewIcon = (ImageView) v.findViewById(resId);
			imageViewIcon.setImageDrawable(findIcon(string));

			return imageViewIcon;
		}

	}

	private class CompleteListViewHolder {

		public TextView textViewTime;
		public ImageView imageViewIcon;
		public TextView textViewCelcius;
		public TextView textViewFarenheit;
		public TextView textViewKelvin;

	}

	private String formateDate(String date) {

		String[] dayMnthYear = date.split("\\-");
		StringBuilder builder = new StringBuilder();

		for (int i = dayMnthYear.length - 1; i >= 0; i--) {
			if (builder.length() > 0 && builder.length() < 6) {
				builder.append("-");
			}

			builder.append(dayMnthYear[i]);
		}

		String formatedDate = builder.toString();
		return formatedDate;
	}

	private void checkNetworkConnection() {

		ConnectionTester ct = new ConnectionTester(getApplicationContext());
		hasInternetConnection = ct.canConnectToInternet();
		if (hasInternetConnection) {

			getCoordinates();

		} else {
			// viser Dialog for å slå på nettverksforbindelse, eller ikke
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Ingen Internettforbindelse");
			dialog.setMessage("Du må slå på Internett for å bruke denne applikasjonen. "
					+ "Velg WiFi eller Mobilnett for å gå videre. Velg Nei hvis du ikke ønsker å gå videre.");
			dialog.setIcon(R.drawable.ic_launcher);

			// setter WiFi-knapp
			dialog.setPositiveButton("Mobilnett",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(
									Settings.ACTION_WIRELESS_SETTINGS));
						}
					});

			// setter Mobilnett-knapp
			dialog.setNeutralButton("WiFi",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(
									Settings.ACTION_WIFI_SETTINGS));
						}
					});

			// setter Nei-knapp
			dialog.setNegativeButton("Nei",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			// viser Dialog
			dialog.show();
		}
	}
	

	protected void onStart(){
		
		checkNetworkConnection();
		super.onStart();
	}
	

	@Override
	public void onBackPressed() {
		
		if(locationListener != null){
			locationManager.removeUpdates(locationListener);
		}
		
		
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		if(locationListener != null){
			locationManager.removeUpdates(locationListener);
		}

		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_list, menu);
		return true;
	}



}
