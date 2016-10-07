package com.pma.smsecure.Activity;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.SMSHelper;
import com.pma.smsecure.Helper.SMSSender;
import com.pma.smsecure.Properties.UtilProperties;
import com.pma.smsecure.Security.RSAUtil;
import com.pma.smsecure.Service.SyncService;

public class ComposeActivity extends Activity {

	private int PICK_CONTACT = 1;
	private boolean isSecure;
	private EditText editTextMsg;
	private EditText editTextPhone;
	private DaoFactory daoFactory = new DaoFactory();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UtilProperties.onActivityCreateSetTheme(this);
		setContentView(R.layout.activity_compose);
		
		editTextMsg = (EditText)findViewById(R.id.editTxtSMSC);
		editTextPhone = (EditText)findViewById(R.id.textSelectContact);
		// sminka, new message is unsecured by default
		editTextMsg.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, ActivitySettings.class);
			startActivity(intent);
		}else if (id == R.id.action_secure) {
		
			changeViewConversationSecurity(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public void open_phonebook(View view) {

		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, 1);
	}

	//podesavanje prikaza imena iz imenika i broja telefona
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String name = "";
		String number = "";
		if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {		
			Uri contactURI = data.getData();
			
			Cursor cursor = getContentResolver().query(contactURI, null, null, null, null);
            cursor.moveToFirst();
            int column_name = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int column_number = cursor.getColumnIndex(Phone.NUMBER);
            
            name = cursor.getString(column_name);
            number = cursor.getString(column_number);
            cursor.close();
		}
		else
		{
			name = "New Message";
			number = "";
		}
		EditText editText = (EditText)findViewById(R.id.textSelectContact);
		editText.setText(number);
		setTitle(name);
	}
	
	//promena okruzenja u zavisnosti da li je
	//korisnik odabrao secure ili unsecure poruku
	private void changeViewConversationSecurity(MenuItem item){
		editTextMsg = (EditText)findViewById(R.id.editTxtSMSC);
		ImageView img = (ImageView) findViewById(R.id.imgSendSMSC);
		
		if(isSecure)
			isSecure = false;
		else
			isSecure = true;
		
		if(!isSecure){
			item.setIcon(R.drawable.unlock_sms);
			editTextMsg.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
			editTextMsg.setHint("Type unsecured message");
			img.setBackgroundResource(R.drawable.plane_red);
			
		}else{
			item.setIcon(R.drawable.lock_sms);
			editTextMsg.getBackground().clearColorFilter();
			editTextMsg.setHint("Type secured message");
			img.setBackgroundResource(R.drawable.plane_green);
			startSync(editTextPhone.getText().toString());
		}
	}
	
	public void sendSMS(View view){
		 
		String phone_number = editTextPhone.getText().toString();
		String originalnaPoruka = editTextMsg.getText().toString();
		
		if(phone_number.trim().equals("") || originalnaPoruka.equals(""))
			return;
		
		phone_number = PhoneNumberUtils.stripSeparators(phone_number);
		
		Conversation activeConversation = SMSHelper.getConversationForPhoneNumber(phone_number, daoFactory, getBaseContext());
		
		//setovanje podataka za novu konverzaciju
		if(activeConversation == null){
			activeConversation = new Conversation();
			
			Contact contact = SMSHelper.getContactForPhoneNumber(phone_number, daoFactory, getBaseContext());
			if(contact == null){
				contact = new Contact();
				contact.setPhoneNumber(phone_number);
				// snimi novi kontakt
				long contact_id = daoFactory.getNewDaoSession(this).getContactDao().insert(contact);
				contact = daoFactory.getNewDaoSession(this).getContactDao().load(contact_id);
			}
			
			activeConversation.setContact(contact);
			activeConversation.setIsSecure(isSecure);
			activeConversation.setPhoneNumberC(phone_number);
			String senderName = SMSHelper.getContactName(getBaseContext(), phone_number);
			activeConversation.setSenderName(senderName);
			activeConversation.setSmsCount(1);
			activeConversation.setSnippet(originalnaPoruka);
			activeConversation.setTimeForLastSMS(new Date());
			// snimi novu konverzaciju
			long conversation_id = daoFactory.getNewDaoSession(this).getConversationDao().insert(activeConversation);
			activeConversation = daoFactory.getNewDaoSession(this).getConversationDao().load(conversation_id);
		}
		else{
			activeConversation.setSnippet(originalnaPoruka);
			activeConversation.setTimeForLastSMS(new Date());
			activeConversation.setSmsCount(activeConversation.getSmsCount()+1);
		}
		if(isSecure){
			String javniKljuc = activeConversation.getContact().getPublicKey();
			
			if(javniKljuc == null){
				// show dialog
				showDialogNoPKey();
				return;
			}
			Log.e("javni kojim kriptujem", javniKljuc);
			
			// ovde se sadrzaj poruke kriptuje
			String sifrovanaPoruka = RSAUtil.encrypt(originalnaPoruka, RSAUtil.stringToPublicKey(javniKljuc)); 
			SMSSender.sendEncryptedSMS(phone_number, sifrovanaPoruka);
			
		}
		else{
			SMSSender.sendPlainTextSMS(phone_number, originalnaPoruka);
		}
		
		//snimanje nove poruke u bazu
		SMS newSmsMessage = new SMS();
		newSmsMessage.setIsEncrypted(isSecure);
		newSmsMessage.setIsRead(true);
		newSmsMessage.setMessage(originalnaPoruka);
		newSmsMessage.setFolder("2");// SENT
		newSmsMessage.setTime(new Date());
		

		// ako je secure, ne cuva se i u standardnoj aplikaciji telefona, samo kod nas
		if(isSecure){
			newSmsMessage.setPhone_id_sms(null);
		}
		else
			newSmsMessage.setPhone_id_sms(999); // TODO getId last sms ID from phone DB
		

		newSmsMessage.setConversation(activeConversation);
		// snimi poruku u bazu
		daoFactory.getNewDaoSession(this).getSMSDao().insert(newSmsMessage);
		// update konverzacije
		daoFactory.getNewDaoSession(this).getConversationDao().update(activeConversation);
		
		editTextMsg.setText("");
		
		Intent intent = new Intent(this,ConversationActivity.class);
		intent.putExtra("thread_id",activeConversation.getId().toString());
//		setResult(Activity.RESULT_OK, intent);
		startActivity(intent);
		finish();
	}
	
	public void startSync(String friendNumber) {
		
		Intent intent = new Intent(ComposeActivity.this, SyncService.class);
		intent.putExtra("friendNumber", friendNumber);
		startService(intent);
	}
	
	
	public void showDialogNoPKey(){
		AlertDialog alertDialog = new AlertDialog.Builder(ComposeActivity.this).create();
		alertDialog.setTitle("Info");
 
		//TODO srediti sta ce se ispisivati
		alertDialog.setMessage("You are currently not connected to the internet! \n Connect and press lock button to secure.");
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",

		    new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int which) {
		            dialog.dismiss();
		        }
		    });
		alertDialog.show();
	}
}
