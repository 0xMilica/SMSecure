package com.pma.smsecure.Service;

import java.io.IOException;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.NetworkHelper;
import com.pma.smsecure.Helper.SMSHelper;
import com.pma.smsecure.publickeystoreendpoint.Publickeystoreendpoint;
import com.pma.smsecure.publickeystoreendpoint.model.PublicKeyStore;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SyncService extends IntentService{
	
	private static final String TAG = "SyncService";
	private DaoFactory daoFactory = new DaoFactory();
	private Publickeystoreendpoint service;

	public SyncService() {
		super("SyncService");
	}

	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
		// TODO intent ne prosledi uvak dobar podatak ? penIntent index ?
		boolean isSyncAll = intent.getBooleanExtra("isSyncAll", false);
		
		Log.d(TAG, "iz intenta-a isSyncAll = " + isSyncAll + " isOnline = " + NetworkHelper.isOnline(this));
		
		if(!NetworkHelper.isOnline(this)){
			Log.e(TAG, "Phone is offLine");
//			Toast.makeText(getBaseContext(), "An Internet connection is required.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Publickeystoreendpoint.Builder builder = new Publickeystoreendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
		
		service = builder.build();
		PublicKeyStore response = null;
		
		if(isSyncAll){
			try {
				Log.d(TAG, "beginning all sync");
				
				User user = daoFactory.getNewDaoSession(this).getUserDao().load(1L);
				String userNumber = user.getPhoneNumber();
				
				Log.d(TAG, "number = " + userNumber);
				PublicKeyStore pks = service.getPublicKeyStoreForPhoneNumber(userNumber).execute();
				if(pks == null){// if the key is not on the web service
					
					Log.d(TAG, "public key not found online");
					Log.d(TAG,"public key from db = "+user.getPublicKey());
					// upload
					PublicKeyStore publickey = new PublicKeyStore();
					publickey.setPhoneNumber(userNumber);
					publickey.setPublicKey(user.getPublicKey());
					service = builder.build();
					response = service.insertPublicKeyStore(publickey).execute();
					
					Log.d(TAG, "public key uploaded");
					
				}
				
				// refresh publick key for every contact
				ContactDao contactDao = daoFactory.getNewDaoSession(this).getContactDao();
				List<Contact> contacts = contactDao.loadAll();
				for(Contact contact : contacts){
					PublicKeyStore tempStore = service.getPublicKeyStoreForPhoneNumber(contact.getPhoneNumber()).execute();
					if(tempStore != null){
						contact.setPublicKey(tempStore.getPublicKey());
						contactDao.update(contact);
					}
				}
				
				Log.d(TAG, "kontakti osvezeni");
				
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, e.getMessage());
				
			}
		}
		else{
			String friendNumber = intent.getStringExtra("friendNumber");
			Log.d(TAG, "iz intenta-a friendNumber = " + friendNumber);
			friendNumber = PhoneNumberUtils.stripSeparators(friendNumber);
			Log.d(TAG, "friendNumber posle obrade = " + friendNumber);
			Contact contact = SMSHelper.getContactForPhoneNumber(friendNumber, daoFactory, getBaseContext());
			ContactDao contactDao = daoFactory.getNewDaoSession(getBaseContext()).getContactDao();
			//trazi ga na netu
			try {
				PublicKeyStore pks = service.getPublicKeyStoreForPhoneNumber(friendNumber).execute();
				if(pks != null){ // ako ga ima na netu
					if(contact == null){
						contact = new Contact();
						
						contact.setPhoneNumber(friendNumber);
						contact.setPublicKey(pks.getPublicKey());
						
						contactDao.insert(contact);
					}else
					{// update
						contact.setPublicKey(pks.getPublicKey());
						contactDao.update(contact);
					}
				}else
				{
					Log.e(TAG, "friendNumber = " + friendNumber + " publicKey not found on web! " );
//					Toast.makeText(getBaseContext(), "sorry, publicKey not found on web! ", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				
				Log.e(TAG, e.getMessage());
				
			}
			
			
		}
	}

}
