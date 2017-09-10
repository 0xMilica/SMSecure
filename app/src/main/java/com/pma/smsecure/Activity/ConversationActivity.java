package com.pma.smsecure.Activity;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pma.smsecure.R;
import com.pma.smsecure.Adapter.ConversationListViewAdapter;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;
import com.pma.smsecure.Dao.DaoMaster;
import com.pma.smsecure.Dao.DaoMaster.DevOpenHelper;
import com.pma.smsecure.Dao.DaoSession;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Dao.SMSDao;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Fragment.ICommunicator;
import com.pma.smsecure.Fragment.SmsDialog;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Helper.SMSSender;
import com.pma.smsecure.Properties.UtilProperties;
import com.pma.smsecure.Security.RSAUtil;
import com.pma.smsecure.Service.SyncService;

//interfejs za dijaloge
public class ConversationActivity extends Activity implements ICommunicator {

	private static final String TAG = "conversation aktivnost";
	private ConversationListViewAdapter listViewAdapter;
	private ListView listView;
	private ProgressDialog pd;
	private List<SMS> smss;
	private Conversation activeConversation;

	// db atributi
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private ConversationDao conversationDao;
	private SMSDao smsDao;

	private ActionBar actionBar;
	// listener za osluskivanje na novu poruku
	BroadcastReceiver receiver;
	private int sync_after_sec = 300;
	private int item_position_clicked = -1;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UtilProperties.onActivityCreateSetTheme(this);
		setContentView(R.layout.activity_conversation);

		// action bar init
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// ovo se ponavlja odvoj ga u zasebnu klasu, moze da bude singleton
		DevOpenHelper helper = new DevOpenHelper(getBaseContext(), "pmasms-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		conversationDao = daoSession.getConversationDao();

		listViewAdapter = new ConversationListViewAdapter(this);

		// main list population ...
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				pd = ProgressDialog.show(ConversationActivity.this, "Loading SMSs..", "Please Wait", true, false);
			}

			@Override
			protected Void doInBackground(Void... params) {

				Intent i = getIntent();
				String thread_id = i.getStringExtra("thread_id");
				Log.d(TAG, "CONVERSATION ID = " + thread_id);
				activeConversation = conversationDao.load(Long.parseLong(thread_id, 10));
				//in this list the final sms will be added
				User user = daoSession.getUserDao().load(1L);
				smss = new ArrayList<SMS>();

				PrivateKey pk = RSAUtil.stringToPrivateKey(user.getPrivateKey().replaceAll("\\s+", ""));

				Log.d("fixPK", user.getPrivateKey());
				smsDao = daoSession.getSMSDao();
				for (SMS sms : activeConversation.getSmslist()) {

					// decrypt just the unread ones(regarding the performances) and those in inbox
					// after the decryption, record it in the db
					if (sms.getIsEncrypted() && sms.getFolder().equals("1") && !sms.getIsRead()) {
						try {
							String decryptedContent = RSAUtil.decrypt(sms.getMessage().replaceAll("\\s+", ""), pk);
							sms.setMessage(decryptedContent);

						} catch (Exception e) {

							Log.e(TAG, "message decryption failed");

						} finally {
							smss.add(sms);
						}

					} else {
						smss.add(sms);
					}
					// set all to read
					sms.setIsRead(true);
					smsDao.update(sms);
				}
				listViewAdapter.setMessages(smss);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				String title = activeConversation.getSenderName();
				actionBar.setTitle(title);
				if (!title.equals(activeConversation.getPhoneNumberC()))
					actionBar.setSubtitle(activeConversation.getPhoneNumberC());
				listView = (ListView) findViewById(R.id.listSMSs);
				listView.setAdapter(listViewAdapter);
				// fokus na poslednji element u listi
				listView.setSelection(listViewAdapter.getMessages().size() - 1);

				listView.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

						item_position_clicked = position;

						FragmentManager manager = getFragmentManager();

						SmsDialog dialog = new SmsDialog();
						dialog.show(manager, "dialog");
						return true;
					}


				});
				pd.dismiss();
			}
		}.execute((Void[]) null);

		/********************listener za novu poruku*****************/
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO odraditi slanje objekta preko intenta
				// da bi se ovde ustedelo na resursima, ovo je samo da proradi sve :)
				activeConversation.resetSmslist();

				Conversation ccc = conversationDao.load(activeConversation.getId());
				smss = ccc.getSmslist();

				listViewAdapter.setMessages(smss);
				listViewAdapter.notifyDataSetChanged();

				// fokus na poslednji element u listi
				listView.setSelection(listViewAdapter.getMessages().size() - 1);
			}
		};
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}


	@Override
	protected void onStart() {
		super.onStart();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		//client.connect();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter("refresh")
		);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
/*		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Conversation Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
//				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.pma.smsecure.Activity/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);*/
	}

	@Override
	protected void onStop() {
		super.onStop();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
	/*	Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Conversation Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.pma.smsecure.Activity/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);*/
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
	//	client.disconnect();
	}

	public void sendSMS(View view) {

		String brojTelefona = activeConversation.getPhoneNumberC();
		EditText et = (EditText) findViewById(R.id.editTxtSMS);
		if (!activeConversation.getContact().getPhoneNumber().equals(brojTelefona))
			Log.e(TAG, "broj telefona nije isti u tabeli");//TODO: code review
		SMS newSmsMessage = new SMS();
		String poruka = et.getText().toString();
		newSmsMessage.setMessage(poruka);
		Log.d(TAG, "activeConversation.getIsSecure() = " + activeConversation.getIsSecure());

		// da li treba da se kriptuje
		if (activeConversation.getIsSecure()) {
			String javniKljuc = activeConversation.getContact().getPublicKey();
			if (javniKljuc == null) {
				// show dialog
				showDialogNoPKey();

				return;
			}
			Log.e("javni kojim kriptujem", javniKljuc);
			poruka = RSAUtil.encrypt(poruka, RSAUtil.stringToPublicKey(javniKljuc));
			SMSSender.sendEncryptedSMS(activeConversation.getPhoneNumberC(), poruka);

			Log.d(TAG, "Kripto PORUKA POSLATAAA");
		} else {
			Log.d(TAG, "Obicna PORUKA POSLATAAA");
			SMSSender.sendPlainTextSMS(activeConversation.getPhoneNumberC(), poruka);
		}

		// novu poruku setuj i snimi u bazu

		newSmsMessage.setIsEncrypted(activeConversation.getIsSecure());
		newSmsMessage.setIsRead(true);
		newSmsMessage.setFolder("2");// SENT
		newSmsMessage.setTime(new Date());
		smss.add(newSmsMessage);
		listViewAdapter.setMessages(smss);
		// prikazi je u otvorenoj aktivnosti
		listViewAdapter.notifyDataSetChanged();

		// ako je secure, ne cuva se i u telefonu, samo kod nas
		if (activeConversation.getIsSecure()) {
			newSmsMessage.setPhone_id_sms(null);
		} else
			newSmsMessage.setPhone_id_sms(999); // TODO getId last sms ID from phone DB


		newSmsMessage.setConversation(activeConversation);

		long idsms = daoSession.getSMSDao().insert(newSmsMessage);
		newSmsMessage = daoSession.getSMSDao().load(idsms);
		// update konverzacije
		activeConversation.setSnippet(poruka);
		int br_poruka = activeConversation.getSmslist().size();
		//Log.d("Bag broj poruka","br_poruka = " + br_poruka);
		activeConversation.setSmsCount(br_poruka + 1);
		activeConversation.setTimeForLastSMS(newSmsMessage.getTime());
		daoSession.getConversationDao().update(activeConversation);
		// fokus na poslednji element u listi
		listView.setSelection(listViewAdapter.getMessages().size() - 1);
		// izbrisi sadrzaj iz tekst polja
		et.setText("");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conversation, menu);
		showSecurityStatusMenu(menu.findItem(R.id.action_secure));// 0 settings, 1 lock
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

		} else if (id == R.id.action_secure) {
			changeViewConversationSecurity(item);
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(this, ActivitySettings.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void changeViewConversationSecurity(MenuItem item) {
		EditText editText = (EditText) findViewById(R.id.editTxtSMS);
		ImageView img = (ImageView) findViewById(R.id.imgSendSMS);

		if (activeConversation.getIsSecure()) {
			activeConversation.setIsSecure(false);
			conversationDao.update(activeConversation);
			item.setIcon(R.drawable.unlock_sms);
			editText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
			editText.setHint("Type unsecured message");
			img.setBackgroundResource(R.drawable.plane_red);

		} else {
			startSync(activeConversation.getPhoneNumberC());
			activeConversation.setIsSecure(true);
			conversationDao.update(activeConversation);
			item.setIcon(R.drawable.lock_sms);
			editText.getBackground().clearColorFilter();
			editText.setHint("Type secured message");
			img.setBackgroundResource(R.drawable.plane_green);
		}
	}

	private void showSecurityStatusMenu(MenuItem item) {
		EditText editText = (EditText) findViewById(R.id.editTxtSMS);
		ImageView img = (ImageView) findViewById(R.id.imgSendSMS);

		if (!activeConversation.getIsSecure()) {
			item.setIcon(R.drawable.unlock_sms);
			editText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
			editText.setHint("Type unsecured message");
			img.setBackgroundResource(R.drawable.plane_red);

		} else {
			item.setIcon(R.drawable.lock_sms);
			editText.getBackground().clearColorFilter();
			editText.setHint("Type secured message");
			img.setBackgroundResource(R.drawable.plane_green);
			startSync(activeConversation.getPhoneNumberC());
		}
	}

	public void startSync(String friendNumber) {

		Intent intent = new Intent(ConversationActivity.this, SyncService.class);
		intent.putExtra("friendNumber", friendNumber);
		startService(intent);
	}

	public void showDialogNoPKey() {
		AlertDialog alertDialog = new AlertDialog.Builder(ConversationActivity.this).create();
		alertDialog.setTitle("Info");

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


	@Override
	public void messageFromDialog(Class<?> cls, String strData,
								  int intData) {

		Log.d(TAG, "communicator message from dialog = " + cls.getName());
		switch (intData) {

			case 0:
				// Delete
				final SMS sms = listViewAdapter.getMessages().get(item_position_clicked);
				listViewAdapter.getMessages().remove(item_position_clicked);
				listViewAdapter.notifyDataSetChanged();
				long sms_id = sms.getId();
				DaoFactory daoFactory = new DaoFactory();
				SMSDao smsDao = (SMSDao) daoFactory.getDaoObject("SMSDao", this);
				SMS smsFromDb = smsDao.load(sms_id);
				ConversationDao conversationDao = (ConversationDao) daoFactory.getDaoObject("ConversationDao", this);
				Conversation c = smsFromDb.getConversation();
				c.setSmsCount(c.getSmsCount() - 1);
				c.getSmslist().remove(c.getSmslist().size() - 1);
				if (sms.getMessage().equals(c.getSnippet())) {
					c.setSnippet(c.getSmslist().get(c.getSmslist().size() - 1).getMessage());
				}

				smsDao.delete(smsFromDb);
				conversationDao.update(c);

				break;

			case 1:
				// Forward
				break;

			case 2:
				// Copy
				break;

			case 3:
				// Show Details
				break;

		}


	}

}
