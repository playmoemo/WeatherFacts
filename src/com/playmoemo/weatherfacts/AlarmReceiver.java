package com.playmoemo.weatherfacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/*
 * Starter TestService når AlarmManager sier ifra
 */
public class AlarmReceiver extends BroadcastReceiver {

	public static final String ACTION_ALARM = "com.playmoemo.spinnertest.alarm";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Alarm Receiver", "Entered");

		Bundle bundle = intent.getExtras();
		String action = bundle.getString(ACTION_ALARM);
		if (action.equals(ACTION_ALARM)) {
			Intent inService = new Intent(context, TestService.class);
			context.startService(inService);
		} else {
			Log.d("else striked", "error");
		}
	}

}
