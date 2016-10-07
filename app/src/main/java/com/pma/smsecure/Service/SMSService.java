package com.pma.smsecure.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pma.smsecure.R;
import com.pma.smsecure.Activity.SplashScreen;
import com.pma.smsecure.Dao.Cache;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Dao.SMSDao;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.SMSHelper;
import com.pma.smsecure.Notification.NData;
import com.pma.smsecure.Notification.NotificationContainer;
import com.pma.smsecure.Notification.NotificationSMSData;

public class SMSService extends IntentService {

	private static final String TAG = "SMSService";
	// Binder given to clients
	private final IBinder mBinder = new MyBinder();
	
	private DaoFactory daoFactory = new DaoFactory();
	// za obavestenja ui aktivnosti
	private LocalBroadcastManager broadcaster;
	
	private SMS newSmsMessage = null;

	
	public SMSService() {
		super("SMSService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.d(TAG,"stigla poruka");
		
		/**********************************************************************************************/
		/**									Obrada poruke											 **/
		/**********************************************************************************************/
		String phoneNumber = intent.getExtras().getString("sender");// preuzmi broj telefona 
		String smsContent = intent.getExtras().getString("smsContent");// preuzmi sadrzaj poruke
		
		boolean isSecure = intent.getExtras().getBoolean("isSecure"); 
		//DEBUG !!
		// poruke se iz delova salju preko emulatora; simulacija za data message;
		//isSecure = true;
		//DEBUG !!
		
		Log.d("fix", isSecure +" : "+ smsContent);
		String sender_name = SMSHelper.getContactName(this, phoneNumber); // za broj vrati iz imenika ime, ako ga nema vrati broj
		
		if(isSecure){
			if(smsContent.substring(smsContent.length()-1).equals(" ")){// deo poruke
				
				smsContent = smsContent.substring(0, smsContent.length()-1);// ukloni poslednji karakter
				
				Cache active_cache = getCacheForPhoneNum(phoneNumber);
				if(active_cache != null){
					String sms_part = active_cache.getSms_parts();
					sms_part+=smsContent;
					active_cache.setSms_parts(sms_part);
					Log.d(TAG,"update kesa");
					daoFactory.getNewDaoSession(this).getCacheDao().update(active_cache);
						
				}else{
					active_cache = new Cache();
					Log.d(TAG,"kreiran novi kes");
					active_cache.setPhoneNumber(phoneNumber);
					active_cache.setSms_parts(smsContent);
					daoFactory.getNewDaoSession(this).getCacheDao().insert(active_cache);
					
				}
				
				return;
				
			}else{// poslednji deo poruke, radi spajanje, brisi kes
				
				Log.d(TAG,"update kesa finall");
				Cache active_cache = getCacheForPhoneNum(phoneNumber);
				String all_parts = active_cache.getSms_parts();
				all_parts+=smsContent;
				daoFactory.getNewDaoSession(this).getCacheDao().delete(active_cache);
				
				// osvezi smsContent sa sadrzajem cele poruke
				smsContent=all_parts; // mrzi me dole da menjam kod
			}
		}
		
		// nova poruka
		newSmsMessage = new SMS();
		newSmsMessage.setIsEncrypted(isSecure);
		newSmsMessage.setIsRead(false);
		newSmsMessage.setMessage(smsContent);
		newSmsMessage.setFolder("1");//INBOX
		newSmsMessage.setTime(new Date());
		
		
		// ako je secure, ne cuva se i u telefonu, samo kod nas u bazi od nase app
		if(isSecure){
			newSmsMessage.setPhone_id_sms(null);
			Log.d("sms servis","stigla secure poruka");
			
		}
		else
			newSmsMessage.setPhone_id_sms(999); // TODO getId last sms ID from phone DB
		
		 
		Contact contact = SMSHelper.getContactForPhoneNumber(phoneNumber, daoFactory, getBaseContext());
		if(contact == null){
			// obrati se Web servisu za javni kljuc ?
			Log.d(TAG, "get CONTACT IZ BAZE je NULL");
			contact = new Contact();
			contact.setPhoneNumber(phoneNumber);
			ContactDao contactDao = (ContactDao) daoFactory.getDaoObject("ContactDao", getBaseContext());
			long contactId = contactDao.insert(contact);
			contact = contactDao.load(contactId);
		}
		
		Conversation conversation = SMSHelper.getConversationForPhoneNumber(phoneNumber, daoFactory, getBaseContext());
		if(conversation == null){
			conversation = new Conversation();
			conversation.setPhoneNumberC(phoneNumber);
			conversation.setSmsCount(1);
			conversation.setPhone_id_conversation(null);
			conversation.setIsSecure(isSecure);
			conversation.setSenderName(sender_name);
			// TODO if isSecure; ako je sifrovana ne prikazuje se otvoren tekst, trennutno
			conversation.setSnippet(newSmsMessage.getMessage());
			
			conversation.setTimeForLastSMS(newSmsMessage.getTime());
			Log.d(TAG, "SET CONTACT IZ BAZE");
			
			conversation.setContact(contact);
			
			ConversationDao conversationDao =(ConversationDao)daoFactory.getDaoObject("ConversationDao", getBaseContext());
			long conversationId = conversationDao.insert(conversation);
			Log.d(TAG, "snimo novu konverzaciju");
			
			conversation = conversationDao.load(conversationId);
			Log.d(TAG, "ucitao novu konverzaciju");
			
		}
		else{
			conversation.setSnippet(newSmsMessage.getMessage());
			conversation.setTimeForLastSMS(newSmsMessage.getTime());
			conversation.setSmsCount(conversation.getSmsCount()+1);
			
		}
		
		newSmsMessage.setConversation(conversation);
		
		ConversationDao conversationDao =(ConversationDao)daoFactory.getDaoObject("ConversationDao", getBaseContext());
		conversationDao.update(conversation);

		SMSDao smsDao = (SMSDao) daoFactory.getDaoObject("SMSDao", getBaseContext());
		smsDao.insert(newSmsMessage);
		Log.d(TAG, "snimo novu poruku");
		
		/**********************************************************************************************/
		/**									Notifikacija											 **/
		/**********************************************************************************************/
		int bezNotifikacije = 1;
		HashSet<String> senders = new HashSet<String>();
		senders.add(sender_name);
		
		NotificationContainer nDataStore = NotificationContainer.getInstance();
		
		NotificationSMSData nData = new NotificationSMSData("SmsNotifikacija");
		nData.contentText = sender_name;
		nData.contentTitle = bezNotifikacije + " New Messages";
		nData.smallIconId = R.drawable.app_icon;
		 
		for(NData dataN : nDataStore.getAllNData() )
		{
			if(dataN instanceof NotificationSMSData)
			{
				bezNotifikacije++;
				senders.add(dataN.contentText);
			}
		}
		nDataStore.addNData(nData);
		
		// brisi [] zagrade iz liste brojeva/imena
		String allSenders = senders.toString();
		allSenders = allSenders.substring(1, allSenders.length()-1);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.app_icon)
				.setContentTitle(bezNotifikacije +" New Messages")
				.setContentText(allSenders);
				

		// da se posle klika brise notifikacija
		mBuilder.setAutoCancel(true);

		// kad kliknes na notifikaciju vrati ga na login
		Intent resultIntent = new Intent(this, SplashScreen.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(SplashScreen.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) this .getSystemService(Context.NOTIFICATION_SERVICE);
		
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
		
		// javi aktivnostima da je poruka stigla (MainActivity)
		notifyActivity();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		Log.d("sms servis","onBind u servisu");
		return mBinder;
	}

	// Class used for the client Binder
	public class MyBinder extends Binder {
		public SMSService getService() {
			return SMSService.this;
		}
	}

	// Method for clients
	public SMS getNewMessage(){
		return newSmsMessage;
	}
	
	public void syncData(){
		Log.d(TAG,"SNHONIZU KLJUCEVE");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		broadcaster = LocalBroadcastManager.getInstance(this);
	}
	
	/**
	 * Metoda zaduzena da ako se nalazimo u main aktivnosti i stigne nam nova poruka
	 * servis obavesti aktivnost
	 */
	public void notifyActivity() {
	    Intent intent = new Intent("refresh");
	    broadcaster.sendBroadcast(intent);
	}
	
	private Cache getCacheForPhoneNum(String phoneNumber){
		
		List<Cache> caches = daoFactory.getNewDaoSession(getBaseContext()).getCacheDao().loadAll();
		
		if(caches != null){
			for(Cache c : caches){
				if(c.getPhoneNumber().equals(phoneNumber)){
					return c;
				}
			}
		}
		return null;
	}
	
}
