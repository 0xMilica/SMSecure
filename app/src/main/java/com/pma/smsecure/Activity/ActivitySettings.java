package com.pma.smsecure.Activity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Security.SHA1;

public class ActivitySettings extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	//atributi baze
	private DaoFactory daoFactory = new DaoFactory();
	private final String TAG = "ActivitySettings";
	private SharedPreferences sPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   addPreferencesFromResource(R.xml.settings);
		   sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		   sPrefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		sPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		switch (key) {
			
			// Tema
			case "listthemas":
				break;

			// obsolite
			case "password":
				
//				String newPassword = sharedPreferences.getString(key, "");
//				String oldPassword = sharedPreferences.getString("old_"+key, "");
//				if(!newPassword.equals("") && !oldPassword.equals("")){
//					changePassword(newPassword, oldPassword);
//					SharedPreferences.Editor editor = sharedPreferences.edit();
//					editor.putString(key, "");//
//					editor.commit();
//				}
				
				break;
				
			// Sinhonizacija javnih kljuceva
			case "listsync":
				break;

				
			default:
				Log.d(TAG, "Neregistrovana promena za podesavanje => " + key);
				break;
		}
	}
	
}
