package com.pma.smsecure.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pma.smsecure.R;

public class UtilProperties {

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	private static final String TAG = "UtilProperties";
	
	
	public static void changeToTheme(Activity activity)
	{
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);

		String listPrefs3 = prefs.getString("listthemas", "Default list prefs");
		
		if(!listPrefs3.isEmpty()){
		
			switch (listPrefs3)
			{

			case "Pink":
				activity.setTheme(R.style.Pink);
				break;
			}
		}
		else activity.setTheme(R.style.Pink);
		Log.d(TAG, "Promena teme");
		
	}
	
	public static int changeFrequency(Activity activity)
	{
		int sync_after_sec=300;
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		
		String listPrefs1 = prefs.getString("listsync", "Default list prefs");
		
		if(!listPrefs1.isEmpty()){
			
			switch (listPrefs1)
			{
			case "Every 6 hours":
				sync_after_sec = 21600;
				break;		
				
			case "Daily":
				sync_after_sec = 43200;
				break;
			case "Weekly":
				sync_after_sec = 302400;
				break;			
			}
		}
		Log.d(TAG, "Promena frekvencije sinhronizacije javnih kljuceva");
		return sync_after_sec;
		
		
	}

}
