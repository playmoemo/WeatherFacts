package com.playmoemo.weatherfacts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
 * her kan bruker lagre navnet sitt til SharedPreferences
 */

public class StartUpActivity extends Activity {

	private Button btnSkip, btnSetUserName;
	private EditText etUserName;
	private String userName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		
		etUserName = (EditText)findViewById(R.id.etUsername);
		btnSetUserName = (Button)findViewById(R.id.btnSetUsername);
		btnSetUserName.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent loadDashBoardIntent = new Intent(getApplicationContext(), DashBoardActivity.class);
				userName = etUserName.getText().toString();
				
				if(userName.length() < 1){
					
					Toast.makeText(getApplicationContext(), "Du må skrive inn et navn", Toast.LENGTH_LONG).show();
				}else{
					saveUserNameToSharedPrefs("username", userName);
					setUserNamePresentPrefs("username_present", true);
					saveIsFirstRunPref("isFirstRun", false);
					
					startActivity(loadDashBoardIntent);
				}
			}
		});
		
		btnSkip = (Button)findViewById(R.id.btnSkipUsername);
		btnSkip.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent skipToDashBoardIntent = new Intent(getApplicationContext(), DashBoardActivity.class);
				if(loadUserNamePresentPref()){
					startActivity(skipToDashBoardIntent);
				} else{
					saveUserNameToSharedPrefs("username", null);
					setUserNamePresentPrefs("username_present", false);
					saveIsFirstRunPref("isFirstRun", false);
					
					startActivity(skipToDashBoardIntent);
				}
				
			}
		});
	}
	
	private boolean loadUserNamePresentPref(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean userNamePresent = sp.getBoolean("username_present", false);
		
		return userNamePresent;
	}
	// lagrer boolean verdi for om det er første gang applikasjonen kjører
	// til SharedPreferences
	private void saveIsFirstRunPref(String key, boolean value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	// lagrer boolean verdi for om brukernavn er satt til SharedPreferences
	private void setUserNamePresentPrefs(String key, boolean value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	
	// lagrer brukernavn til SharedPreferences
	private void saveUserNameToSharedPrefs(String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
