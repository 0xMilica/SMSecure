package com.pma.smsecure.Helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;

/**
 * Klasa zaduzena za izvlacenje podataka iz baze android uredjaja
 */
public class SMSHelper {

	private static final String TAG = "SMSHelper";
	
	/**
	 * Get Conversation for phone number
	 * @param phoneNumber
	 * @param daoFactory
	 * @param context
	 * @return Conversation or null
	 */
	public static Conversation getConversationForPhoneNumber(String phoneNumber, DaoFactory daoFactory, Context context){
		
		ConversationDao conversationDao = (ConversationDao) daoFactory.getDaoObject("ConversationDao", context);
		for(Conversation con : conversationDao.loadAll()){
			Log.d(TAG,"00 konverzacija sa brojem tel = " + con.getPhoneNumberC());
			if(con.getPhoneNumberC().equals(phoneNumber))
				return con;
		}
		
		String cCode = GetCountryZipCode(context);
		// kod emulatora cCode je 1 kao i za CA i US
		if(cCode.equals("1"))
			cCode = "381";
		
		if(phoneNumber.startsWith("+")){
			phoneNumber = phoneNumber.replace("+" + cCode, "0");
			for(Conversation con : conversationDao.loadAll()){
				Log.d(TAG,"01 konverzacija sa brojem tel = " + con.getPhoneNumberC());
				if(con.getPhoneNumberC().equals(phoneNumber))
					return con;
			}
		}
		else{
			phoneNumber = "+"+ cCode + phoneNumber.substring(1);
			
			for(Conversation con : conversationDao.loadAll()){
				Log.d(TAG,"02 konverzacija sa brojem tel = " + con.getPhoneNumberC());
				if(con.getPhoneNumberC().equals(phoneNumber))
					return con;
			}
		}
		
		return null;
	}
	
	public static String GetCountryZipCode(Context c){
	    
		String CountryID = "";
	    String CountryZipCode = "";

	    TelephonyManager manager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
	    
	    CountryID = manager.getSimCountryIso().toUpperCase();
	    String[] rl = c.getResources().getStringArray(R.array.CountryCodes);
	    
	    for(int i=0; i<rl.length; i++){
	    	
	        String[] g = rl[i].split(",");
	        if(g[1].trim().equals(CountryID.trim())){
	            CountryZipCode = g[0];
	            break;  
	        }
	    }
	    return CountryZipCode;
	}
	
	/**
	 * Get Contact for phone number
	 * @param phoneNumber
	 * @param daoFactory
	 * @param context
	 * @return Contact or null
	 */
	public static Contact getContactForPhoneNumber(String phoneNumber, DaoFactory daoFactory, Context context){
		
		ContactDao contactDao = (ContactDao) daoFactory.getDaoObject("ContactDao", context);
		for(Contact c : contactDao.loadAll()){
			if(c.getPhoneNumber().equals(phoneNumber))
				return c;
		}
		
		return null;
	}

	/**
	 * Trazi u imeniku i u sim karticama, kontakt na osnovu broja;
	 * @param context
	 * @param phoneNumber
	 * @return ContactName ili phoneNumber ako ne pronadje
	 */
	public static String getContactName(Context context, String phoneNumber) {
		
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		if (cursor == null) {
			return null;
		}
		
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		if(contactName == null)
			return phoneNumber;
		
		return contactName;
	}
}
