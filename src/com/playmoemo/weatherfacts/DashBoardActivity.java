package com.playmoemo.weatherfacts;

import java.util.Locale;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class DashBoardActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {

	private Button btnShowList;
	private TextView tvTime, tvCity, tvTemp, tvPersonalMessage;
	private ImageView imageViewIcon;
	private boolean hasInternetConnection;
	private DataReceiver dataReceiver;
	private AlarmManager alarmMgr;
	private PendingIntent pIntent;
	private String userName;
	private TextToSpeech tts;
	private String speakCity, speakReadableTime;
	private double speakTemp;
	private boolean isMuted;
	
	private String[] iconNames = {"01d", "02d", "03d", "04d", "09d", "10d", "11d", "13d", "50d",
			"01n", "02n", "03n", "04n", "09n", "10n", "11n", "13n", "50n"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		
		tvTime = (TextView)findViewById(R.id.tvOutputTime);
		tvCity = (TextView)findViewById(R.id.tvOutputCity);
		tvTemp = (TextView)findViewById(R.id.tvOutputTemperature);
		tvPersonalMessage = (TextView)findViewById(R.id.tvPersonalMessage);
		
		imageViewIcon = (ImageView)findViewById(R.id.imageViewIcon);
		
		btnShowList = (Button)findViewById(R.id.btnShowList);
		btnShowList.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), WeatherListActivity.class);
				startActivity(intent);
			}
		});
		
		// initialiserer og konfigurerer TextToSpeech
		tts = new TextToSpeech(this, this);
		tts.setPitch((float)0.7);
		
		// henter brukernavn fra SharedPreferences
		loadSavedPreferences();
		
	}// onCreate()----------------------------------------------------------------------

	// henter brukernavn fra SharedPreferences
	private void loadSavedPreferences() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean didSetUserNamePref = sp.getBoolean("username_present", false);
		String userNamePref = sp.getString("username", null);
		if(userNamePref != null && didSetUserNamePref) {
			userName = " " + userNamePref + "!";
		} else if(userNamePref == null) {
			userName = "!";
		} else {
			;
		}
		
	}
	
	// presenterer fakta om været basert på tilhørende ikon
	private void showPersonalFact(String icon) {
		Resources res = getResources();
		String inputIcon = icon;
		String[] facts = res.getStringArray(R.array.weather_facts);
		Log.d("facts 0", facts[0]);
		for(int i = 0; i < iconNames.length; i++) {
			if(inputIcon.equals(iconNames[i])) {
				tvPersonalMessage.setText("Hei" + userName + " " + facts[i]);
			} else {
				continue;
			}
		}
	}
	
	// setter alarm for når TestService skal starte
	private void setAlarm() {
		alarmMgr = (AlarmManager)getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.ACTION_ALARM,
				AlarmReceiver.ACTION_ALARM);
		pIntent = PendingIntent.getBroadcast(getApplicationContext(),
				1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// starter hvert tiende minutt, hvis DashBoardActivity er started.
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), 10 * 60000, pIntent);
		
	}
	
	// tar imot data fra TestService
	private class DataReceiver extends BroadcastReceiver  {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI(intent);
		}
	};
	
	// oppdaterer UI med data fra DataReceiver
	private void updateUI(Intent intent) {
		String readableTime = intent.getStringExtra("readableTime");
		String city = intent.getStringExtra("city");
		double temp = intent.getDoubleExtra("temperature", 0.0);
		String currentIcon = intent.getStringExtra("icon");
		
		speakCity = city;
		speakTemp = temp;
		speakReadableTime = readableTime;
		
		tvTime.setText(readableTime);
		tvCity.setText(city);
		tvTemp.setText(String.valueOf(temp) + " \u00B0" + "C");
		
		// setter riktig icon fra nedlastet vær
		int neededIcon = java.util.Arrays.asList(iconNames).indexOf(currentIcon);
		
		switch (neededIcon) {
		case 0:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.sky_is_clear_d));
			break;
		case 1:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.few_clouds_d));
			break;
		case 2:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.scattered_clouds_d));
			break;
		case 3:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.broken_clouds_d));
			break;
		case 4:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.shower_rain_d));
			break;
		case 5:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.rain_d));
			break;
		case 6:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.thunderstorm_d));
			break;
		case 7:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.snow_d));
			break;
		case 8:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.mist_d));
			break;
		case 9:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.sky_is_clear_n));
			break;
		case 10:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.few_clouds_n));
			break;
		case 11:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.scattered_clouds_n));
			break;
		case 12:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.broken_clouds_n));
			break;
		case 13:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.shower_rain_n));
			break;
		case 14:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.rain_n));
			break;
		case 15:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.thunderstorm_n));
			break;
		case 16:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.snow_n));
			break;
		case 17:
			imageViewIcon.setImageDrawable(getResources().getDrawable(
					R.drawable.mist_n));
			break;
		}
		
		// viser fakta som matcher værtypen
		showPersonalFact(currentIcon);
		
		speak(speakCity, speakReadableTime, speakTemp);
		
	}
	
	// kontrollerer om enheten er tilkoblet WiFi eller mobilnett
	private void checkNetworkConnection() {	
		ConnectionTester ct = new ConnectionTester(getApplicationContext());
		hasInternetConnection = ct.canConnectToInternet();
		if(hasInternetConnection) {
			dataReceiver = new DataReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(TestService.BROADCAST_ACTION);
			registerReceiver(dataReceiver, filter);
			
			// setter alarm og starter Service
			setAlarm();
		} else {
			// viser Dialog for å slå på nettverksforbindelse, eller ikke
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Ingen Internettforbindelse");
			dialog.setMessage("Du må slå på Internett for å bruke denne applikasjonen. " +
					"Velg WiFi eller Mobilnett for å gå videre. Velg Nei hvis du ikke ønsker å gå videre.");
			dialog.setIcon(R.drawable.ic_launcher);
			
			// setter Mobilnett-knapp
			dialog.setPositiveButton("Mobilnett", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
				}
			});
			
			// setter WiFi-knapp
			dialog.setNeutralButton("WiFi", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				}
			});
			
			// setter Nei-knapp
			dialog.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			// viser Dialog
			dialog.show();
		}
	}
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(TestService.BROADCAST_ACTION);
		registerReceiver(dataReceiver, filter);
		
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		checkNetworkConnection();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("WF");
		loadIsMutedPref();
		
		// initialiserer og konfigurerer TextToSpeech
		tts = new TextToSpeech(this, this);
		tts.setPitch((float)0.7);
		
		super.onStart();
	}
	
	
	@Override
	protected void onPause() {
		// hvis bruker trykker home-knapp før dataReceiver er initialisert i onStart()
		if(dataReceiver != null) {
			unregisterReceiver(dataReceiver);
		}	
		super.onPause();
	}
	
	
	@Override
	protected void onStop() {
		// slår av alarm for start av TestService
		if(alarmMgr != null) {
			alarmMgr.cancel(pIntent);
		}
		
		// slår av TextToSpeech
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		
		super.onStop();
	}
	

	@Override
	public void onBackPressed() {
		// slår av alarm for start av TestService
		if(alarmMgr != null) {
			alarmMgr.cancel(pIntent);
		}
		
		super.onBackPressed();
	}

	// starter WeatherForecastActivity
	public void btnMoreWeatherClick(View v){
		Intent intent = new Intent(this, WeatherForecastActivity.class);
		startActivity(intent);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// håndterer knappetrykk i actionbar
		switch(item.getItemId()) {
		case R.id.action_username:
			startActivity(new Intent(this, StartUpActivity.class));
			break;
		case R.id.action_mute:
			if(isMuted){
				isMuted = false;
				saveIsMutedPref("isMuted", false);
				item.setIcon(R.drawable.volume_on);
			}else{
				isMuted = true;
				saveIsMutedPref("isMuted", true);
				item.setIcon(R.drawable.volume_muted);
			}
			break;
		case R.id.action_forecast:
			startActivity(new Intent(this, WeatherForecastActivity.class));
			break;
		case R.id.action_weather_list:
			startActivity(new Intent(this, WeatherListActivity.class));
			break;
		}
		return true;
	}
	
	// laster inn boolean verdi for om TextToSpeech er muted fra SharedPreferences
	private boolean loadIsMutedPref(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isMutedPref = sp.getBoolean("isMuted", false);
		isMuted = isMutedPref; 
		
		return isMutedPref; 
	}
	
	// lagrer boolean verdi for om TextToSpeech er muted til SharedPreferences
	private void saveIsMutedPref(String key, boolean value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	// bygger actionbar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		return true;
	}
	
	// setter volum-ikon i actionbar basert på boolean verdi fra SharedPreferences
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(loadIsMutedPref()){
			
			menu.getItem(2).setIcon(R.drawable.volume_muted);
		}else{
			menu.getItem(2).setIcon(R.drawable.volume_on);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	// starter TextToSpeech, og bruker tekst med eller uten brukernavn
	private void speak(String city, String readableTime, double temp) {
		String text = null;
		if(userName != null){
		 	text = "Hello, " + userName + ".\n" + city +
		 			". " + readableTime + ".\nTemperature. " + 
		 			String.valueOf(temp) + " \u00B0" + "C";
		} else {
			text = "Hello.\n" + city +
		 			". " + readableTime + ".\nTemperature. " + 
		 			String.valueOf(temp) + " \u00B0" + "C";
		}
		 	
		if (city != null && !isMuted) {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	 }
	
	// legger til rette for å bruke TextToSpeech
	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if(result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "Språk er ikke støttet");
			} else {
				
				speak(speakCity, speakReadableTime, speakTemp);
			}
		} else {
			Log.e("TTS", "Initialisering feilet!");
		}	
	}
}
