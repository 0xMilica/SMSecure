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
	
	public void changePassword(String newPassword){
		
		UserDao userDao = (UserDao)daoFactory.getDaoObject("UserDao", getBaseContext());
		User user = userDao.load(1L);
		try {
			// TODO add salt ..
			newPassword = SHA1.toSHA1(newPassword);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		user.setAppPassword(newPassword);
		userDao.update(user);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		switch (key) {
			
			// Tema
			case "listthemas":
				break;
			
			case "password":
				
				String updPassword = sharedPreferences.getString(key, "");
				if(!updPassword.equals("")){
					changePassword(updPassword);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(key, "");
					editor.commit();
				}
				
				break;
				
			// Sinhonizacija javnih kljuceva
			case "listsync":
				break;
				
			// Zvono
			case "listalerttones":
				break;
				
			default:
				Log.d(TAG, "Neregistrovana promena za podesavanje => " + key);
				break;
		}
	}
	
}
