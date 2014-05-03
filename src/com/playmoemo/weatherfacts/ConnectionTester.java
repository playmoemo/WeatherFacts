package com.playmoemo.weatherfacts;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
 * sjekker om enheten er tilkoblet internett
 */
public class ConnectionTester {
	
	private Context mContext;
	
	public ConnectionTester(Context context) {
		this.mContext = context;
	}
	
	public boolean canConnectToInternet() {
		ConnectivityManager conMan = (ConnectivityManager)mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(conMan != null) {
			NetworkInfo[] info = conMan.getAllNetworkInfo();
			if(info != null) {
				for(int i = 0; i < info.length; i++) {
					if(info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
