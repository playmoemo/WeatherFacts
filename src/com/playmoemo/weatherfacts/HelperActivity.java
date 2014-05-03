package com.playmoemo.weatherfacts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/*
 * tom Activity som avgjør om bruker kan sette brukernavn i StartUpActivity,
 * eller sendes rett til DashBoardActivity
 */

public class HelperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadIsFirstRunPref();
	}
	
	private void loadIsFirstRunPref() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFirstRun = sp.getBoolean("isFirstRun", true);
		
		if(isFirstRun) {
			// starter StartUpActivity
			startActivity(new Intent(this, StartUpActivity.class));
		} else {
			// starter DashBoardActivity
			startActivity(new Intent(this, DashBoardActivity.class));
		}
	}
}
