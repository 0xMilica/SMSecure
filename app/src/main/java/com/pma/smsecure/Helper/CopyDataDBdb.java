package com.pma.smsecure.Helper;

import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;

import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;
import com.pma.smsecure.Dao.DaoSession;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Dao.SMSDao;

public final class CopyDataDBdb {
	
	private ContactDao contactDao;
	private ConversationDao conversationDao;
	private SMSDao smsDao;
	
	public CopyDataDBdb(DaoSession daoSession){
		contactDao = daoSession.getContactDao();
		conversationDao = daoSession.getConversationDao();
		smsDao = daoSession.getSMSDao();
	}
	
	public void doIt(Context context){
		
		Uri SMS_INBOX = Uri.parse("content://sms/conversations");
		Cursor c = context.getContentResolver().query(SMS_INBOX, null, null, null, "date desc");
		int allConversations = c.getCount();
		String[] count = new String[allConversations];
		String[] snippet = new String[allConversations];
		String[] thread_id = new String[allConversations];
	
		c.moveToFirst();

		for (int i = 0; i < allConversations; i++) {
			
			count[i] = c.getString(c.getColumnIndexOrThrow("msg_count")).toString();
			thread_id[i] = c.getString(c.getColumnIndexOrThrow("thread_id")).toString();
			snippet[i] = c.getString(c.getColumnIndexOrThrow("snippet")).toString();
			
			// treba nam jedan sms da iz njega izvucemo adresu (br_telefona)
			// pa na osnovu broja da u imeniku pronadjemo ime
			Uri smsFromThread = Uri.parse("content://sms/conversations/"+ thread_id[i]);
			Cursor c2 = context.getContentResolver().query(smsFromThread, null, null, null, "date asc");
			c2.moveToFirst();
			
			String address = c2.getString(c2.getColumnIndex("address"));
			String name = getContactName(context, address);

			Contact contact = new Contact();
			address = PhoneNumberUtils.stripSeparators(address);
			contact.setPhoneNumber(address);
			
			contactDao.insert(contact);
			
			Conversation conversation = new Conversation();
			conversation.setContact(contact);
			conversation.setPhone_id_conversation(Integer.parseInt(thread_id[i]));
			conversation.setSmsCount(Integer.parseInt(count[i]));
			conversation.setIsSecure(false);// uvek je false jer je aplikacija tek instalirana ...
			conversation.setPhoneNumberC(address);
			
			conversation.setSenderName(name);
			c2.moveToLast();
			conversation.setSnippet(c2.getString(c2.getColumnIndex("body")));
			conversation.setTimeForLastSMS(new Date(Long.parseLong(c2.getString(c2.getColumnIndex("date")))));
			
			conversationDao.insert(conversation);
			c2.moveToFirst();
			
			// poruke iz konverzacije
			for(int j=0; j<c2.getCount(); j++){
				
				String date = c2.getString(c2.getColumnIndex("date"));
				long ms = Long.parseLong(date);
				String message = c2.getString(c2.getColumnIndex("body"));	
				String folder = c2.getString(c2.getColumnIndex("type"));	
				String read = c2.getString(c2.getColumnIndex("read"));	
				int oldSMSid = Integer.parseInt(c2.getString(c2.getColumnIndex("_id")));
				
				SMS sms = new SMS();
				sms.setFolder(folder);
				sms.setMessage(message);
				sms.setPhone_id_sms(oldSMSid);
				sms.setConversation(conversation);
				sms.setIsEncrypted(false); // uvek je false jer je aplikacija tek instalirana ...
				if(read.equals("1"))
					sms.setIsRead(true);
				else
					sms.setIsRead(false);
				
				Date dateFromSms = new Date(ms);
				sms.setTime(dateFromSms);
				
				smsDao.insert(sms);
				
				c2.moveToNext();
			}
			c2.close();
			c.moveToNext();
		}
		c.close();
		
	}
	
	/**
	 * Trazi u imeniku i u sim karticama, kontakt na osnovu broja;
	 * @param context
	 * @param phoneNumber
	 * @return ContactName ili phoneNumber ako ne pronadje
	 */
	private String getContactName(Context context, String phoneNumber) {
		
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
