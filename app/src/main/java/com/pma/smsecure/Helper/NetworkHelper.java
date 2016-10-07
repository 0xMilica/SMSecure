package com.pma.smsecure.Helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean isOnline(Context activity) {
		
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
