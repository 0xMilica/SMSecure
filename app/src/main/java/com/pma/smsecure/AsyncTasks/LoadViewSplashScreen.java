package com.pma.smsecure.AsyncTasks;

import java.io.IOException;
import java.security.KeyPair;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.pma.smsecure.R;
import com.pma.smsecure.Activity.SplashScreen;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Helper.CopyDataDBdb;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.NetworkHelper;
import com.pma.smsecure.Security.RSAUtil;
import com.pma.smsecure.publickeystoreendpoint.Publickeystoreendpoint;
import com.pma.smsecure.publickeystoreendpoint.model.PublicKeyStore;

public class LoadViewSplashScreen extends AsyncTask<Void, Integer, Void>{

	private static final String TAG = "LoadViewSplashScreen";
	private SplashScreen splashScreenActivity;
	private ProgressDialog pd;
	
	// veza sa servisom
	private Publickeystoreendpoint service;

	
	public LoadViewSplashScreen(SplashScreen splashScreenActivity)
	{
		this.splashScreenActivity = splashScreenActivity;
	}
	
	
	@Override
	protected void onPreExecute() {
		if(splashScreenActivity.isFirstTimeStart())
			pd = ProgressDialog.show(splashScreenActivity, "First start Init DB..", "Please Wait", true, false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		
		DaoFactory daoFactory = new DaoFactory();
		UserDao userDao = (UserDao)daoFactory.getDaoObject("UserDao", splashScreenActivity.getBaseContext());

		Cursor cursor = daoFactory.getSQLiteDatabase().query(userDao.getTablename(), 
				userDao.getAllColumns(), null, null, null, null, null);
		
		cursor.moveToFirst();
		
		
		Log.d(TAG, "Broj korisnika (naloga) u bazi = " + cursor.getCount());
		
		if (cursor.getCount() == 0) {
			
			Log.d(TAG, "Kreiramo novog korisnika = ");
			User userApp = new User();
			
			userApp.setAppPassword(splashScreenActivity.getAppPassword());
			userApp.setPhoneNumber(splashScreenActivity.getUserNumber());
			
			// generisi par kljuceva
			KeyPair kljucevi = RSAUtil.generateNewKeys();
			
			String javni = Base64.encodeToString(kljucevi.getPublic().getEncoded(), Base64.DEFAULT);
			String privatni = Base64.encodeToString(kljucevi.getPrivate().getEncoded(), Base64.DEFAULT);
			
			Log.d(TAG, "Prvi generisani Javni kljuc = " + javni);
			Log.d(TAG, "Prvi generisani Privatni kljuc = " + privatni);
			
			userApp.setPublicKey(javni);
			// TODO kriptuj privatni kljuc sa mojom sifrom AES
			userApp.setPrivateKey(privatni);
			
			// posalji odmah svoj na server ako imas konekciju ?
			if(NetworkHelper.isOnline(splashScreenActivity)){
				
				Log.d(TAG, "Telefon je Online");
				
				Publickeystoreendpoint.Builder builder = new Publickeystoreendpoint.Builder(
						AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				
				service = builder.build();
				
				PublicKeyStore pks;
				try {
					pks = service.getPublicKeyStoreForPhoneNumber(splashScreenActivity.getUserNumber()).execute();
				
					if(pks == null){// nas javni kljuc nije na web servisu
						
						Log.d(TAG, "Nas Javni kljuc nije pronasao na web servisu");
						
						// upload
						PublicKeyStore publickey = new PublicKeyStore();
						publickey.setPhoneNumber(splashScreenActivity.getUserNumber());
						publickey.setPublicKey(userApp.getPublicKey());
						service = builder.build();
						service.insertPublicKeyStore(publickey).execute();
						
						Log.d(TAG, "Nas Javni kljuc je upload-ovan");
						
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				}
			}
			else
			{
				Log.d(TAG, "Telefon je OffLine");
			}
			
			userDao.insert(userApp);
			
			// punimo bazu za prvi start
			CopyDataDBdb copyDb = new CopyDataDBdb(daoFactory.getNewDaoSession(splashScreenActivity.getBaseContext()));
			copyDb.doIt(splashScreenActivity.getBaseContext());	
			
		} else {
			
			Log.d(TAG, "Postoji nalog u bazi sa jvnim i privatnim kljucem");
			
			String passwdFromDB = cursor.getString(cursor.getColumnIndexOrThrow("APP_PASSWORD")).toString();
			Log.d(TAG, "sifra iz baze za pristup aplikaciji = " + passwdFromDB);
			
			if(passwdFromDB.equals(splashScreenActivity.getAppPassword()))
				splashScreenActivity.goToLoginActivity();
			else{
				splashScreenActivity.passwordErrors++;
			}
		}
		
		cursor.close();
		
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

	}

	@Override
	protected void onPostExecute(Void result) {
		
		if(splashScreenActivity.passwordErrors != 0){
			TextView errorView = (TextView)splashScreenActivity.findViewById(R.id.txtErrorLogin);
			errorView.setText("Incorect input - " + splashScreenActivity.passwordErrors + "/3");
		}
		splashScreenActivity.getBtnLogin().setEnabled(true);
		splashScreenActivity.getBtnLogin().setClickable(true);
		
		if(splashScreenActivity.isFirstTimeStart())
			pd.dismiss();
		splashScreenActivity.setFirstTimeStart(false);
		
		splashScreenActivity.getBtnLogin().setText("Log In");
		EditText passwdField = (EditText) splashScreenActivity.findViewById(R.id.passwordEditText);
		passwdField.setText("");
	}


}
