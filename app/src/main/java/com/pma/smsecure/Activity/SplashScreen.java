package com.pma.smsecure.Activity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pma.smsecure.R;
import com.pma.smsecure.AsyncTasks.LoadViewSplashScreen;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Fragment.ICommunicator;
import com.pma.smsecure.Fragment.PhoneNumberDialog;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.SMSHelper;
import com.pma.smsecure.Security.SHA1;

public class SplashScreen extends Activity implements ICommunicator {

	private static final String TAG = "SplashScreen";
	private boolean isFirstTimeStart;

	public int passwordErrors = 0;
	private String appPassword = "";
	private Button btnLogin;
	private String userNumber;
	
	
	private PhoneNumberDialog dialogPNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
		setContentView(R.layout.activity_splash_screen);
		
		btnLogin = (Button) findViewById(R.id.btnLogin);
		
		DaoFactory daoFactory = new DaoFactory();
		UserDao userDao = (UserDao)daoFactory.getDaoObject("UserDao", getBaseContext());

		Cursor cursor = daoFactory.getSQLiteDatabase().query(userDao.getTablename(), 
				userDao.getAllColumns(), null, null, null, null, null);
		
		cursor.moveToFirst();

		if (cursor.getCount() == 0) {

			isFirstTimeStart = true;
		    btnLogin.setText("New Password");
		    Log.d(TAG,"PRVI START APLIKACIJEEEEE");
		     
		}else{
			
			isFirstTimeStart = false;
			btnLogin.setText("Log in");
			Log.d(TAG,"N-ti START APLIKACIJEEEEE");
		}
		
		cursor.close();
		
	}

	public void btnLoginFunction(View view) {
		
		if(passwordErrors >= 2)
			finish();
		
		EditText passwdField = (EditText) findViewById(R.id.passwordEditText);
		appPassword = passwdField.getText().toString();
		try {
			// TODO add salt ..
			appPassword = SHA1.toSHA1(appPassword);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		btnLogin = (Button) findViewById(R.id.btnLogin);
		
		btnLogin.setEnabled(false);
		btnLogin.setClickable(false);
		
		// TODO: za neku narednu verziju resiti problem menjanja sim kartice, odnosno broja ...
		if(isFirstTimeStart){
			if(GetPhoneNumber())
			{
				new LoadViewSplashScreen(this).execute();
			}
		}
		else
		{
			new LoadViewSplashScreen(this).execute();
		}
	}
	
	private boolean GetPhoneNumber()
	{
		// pokusaj preuzimanja broja sa sim kartice
		TelephonyManager telemanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//String simSerialNumber = telemanager.getSimSerialNumber();
		
		userNumber = telemanager.getLine1Number();
		
		if(userNumber == null || userNumber.equals(""))
		{
			android.app.FragmentManager fragmentManager = getFragmentManager();
			
			dialogPNumber = new PhoneNumberDialog();
			dialogPNumber.show(fragmentManager, "dijalogUnosBroja");
			return false;
		}
		return true;
	}

	@Override
	public void messageFromDialog(Class<?> cls, String strData, int intData) {
		
		Log.d(TAG, "communicator message from dialog = " + cls.getName());
		Log.d(TAG, "message string = " + strData);
		
		// broj telefona mora imati bar 9 cifara
		if(strData.length() < 9)
		{
			GetPhoneNumber();
			return;
		}
		// izbrisi zagrade, crtice i prazne stringove
		strData = PhoneNumberUtils.stripSeparators(strData);
		strData.trim();
		
		String cCode = SMSHelper.GetCountryZipCode(this);
		
		// cCode je je 1 na emulatoru, 1 je i CA i US
		if(cCode.equals("1"))
			cCode = "381";
		
		if(strData.startsWith("0"))
		{
			strData = strData.replace("0", "+"+cCode);
		}
		userNumber = strData;
		Log.d(TAG, "posle formatiranja - userNumber string = " + userNumber);
		
	}
	
	public void goToLoginActivity(){
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	public boolean isFirstTimeStart() {
		return isFirstTimeStart;
	}

	public void setFirstTimeStart(boolean isFirstTimeStart) {
		this.isFirstTimeStart = isFirstTimeStart;
	}

	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}

	public Button getBtnLogin() {
		return btnLogin;
	}

	public void setBtnLogin(Button btnLogin) {
		this.btnLogin = btnLogin;
	}

	public String getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public PhoneNumberDialog getDialogPNumber() {
		return dialogPNumber;
	}

	public void setDialogPNumber(PhoneNumberDialog dialogPNumber) {
		this.dialogPNumber = dialogPNumber;
	}

}
