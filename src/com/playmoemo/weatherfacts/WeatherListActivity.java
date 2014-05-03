package com.playmoemo.weatherfacts;

import java.util.ArrayList;
import java.util.Collections;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class WeatherListActivity extends ActionBarActivity {
	
	private TextView tvSelection;
	private ListView lvPreviousWeather;
	private WeatherStorage db;
	private ArrayList<Weather> weatherFromDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_list);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		db = WeatherStorage.getInstance(this);
		
		lvPreviousWeather = (ListView)findViewById(R.id.lvPreviousWeatherlist);
		lvPreviousWeather.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Weather itemValue = (Weather)lvPreviousWeather.getItemAtPosition(position);
				
				if(tvSelection.getText().toString().length() < 1){
					tvSelection.setText("Tid: " + itemValue.getReadableTime() + 
							"\nBy: " + itemValue.getCity() + "\nTemp: " + itemValue.getTemperature()
							+ " \u00B0" + "C");
				} else{
					
					tvSelection.setText("");
				}	
			}
		});
		
		lvPreviousWeather.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Slette fra WeatherStorage
				Weather toBeDeleted = (Weather)lvPreviousWeather.getItemAtPosition(position);
				long timeKey = toBeDeleted.getTime();
				showDeleteDialog(timeKey);
				
				return true;
			}
		});
		
		tvSelection = (TextView)findViewById(R.id.tvOutput);
		
		// henter alt fra WeatherStorage
		weatherFromDB = (ArrayList<Weather>)db.getAllWeather();
		// snur listen for å presentere vær i synkende rekkefølge på tid
		Collections.reverse(weatherFromDB);
		// overfører listen til ArrayAdapter for å bygge opp ListView
		ArrayAdapter<Weather> adapter = new ArrayAdapter<Weather>(this,
				android.R.layout.simple_list_item_1, weatherFromDB);
		lvPreviousWeather.setAdapter(adapter);	
	}
	
	// viser dialog for bruker for å bekrefte sletting av værmelding i listen
	public void showDeleteDialog(final long time){
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Slette vær?");
		dialog.setMessage("Er du sikker på at du vil slette?");
		dialog.setIcon(R.drawable.ic_launcher);
		
		// setter Slett-knapp
		dialog.setPositiveButton("Slett", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				db.deleteWeather(time);
				
				// henter alt fra WeatherStorage
				weatherFromDB = (ArrayList<Weather>)db.getAllWeather();
				// snur listen for å presentere vær i synkende rekkefølge på tid
				Collections.reverse(weatherFromDB);
				// overfører listen til ArrayAdapter for å bygge opp ListView
				ArrayAdapter<Weather> adapter = new ArrayAdapter<Weather>(WeatherListActivity.this,
						android.R.layout.simple_list_item_1, weatherFromDB);
				lvPreviousWeather.setAdapter(adapter);
			}
		});
		
		
		// setter Angre-knapp
		dialog.setNegativeButton("Angre", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// viser Dialog
		dialog.show();
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
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
