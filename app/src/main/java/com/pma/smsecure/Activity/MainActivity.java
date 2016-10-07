package com.pma.smsecure.Activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.pma.smsecure.R;
import com.pma.smsecure.Adapter.MainListViewAdapter;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;
import com.pma.smsecure.Dao.DaoMaster;
import com.pma.smsecure.Dao.DaoMaster.DevOpenHelper;
import com.pma.smsecure.Dao.DaoSession;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Fragment.ConversationItemDialog;
import com.pma.smsecure.Fragment.ICommunicator;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Notification.NotificationContainer;
import com.pma.smsecure.Properties.UtilProperties;
import com.pma.smsecure.Service.SMSService;
import com.pma.smsecure.Service.SyncService;
import com.pma.smsecure.Tabs.SlidingTabLayout;
import com.pma.smsecure.Tabs.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements ICommunicator{

	private static final String TAG = "main aktivnost";
	private MainListViewAdapter listViewAdapter;
	private ProgressDialog pd;
	private SMSService smsService; // ako je null onda smo diskonektovani od servisa
	private int item_position_clicked;
	private int sync_after_sec = 300; // 300 sekundi; 5 min

	//Tab atributi
	ViewPager pager;
	ViewPagerAdapter adapter;
	SlidingTabLayout tabs;
	CharSequence Titles[]={"Home","Events"};
	int Numboftabs =2;
	
	// db atributi
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private ConversationDao conversationDao;
	
	// listener za osluskivanje na novu poruku
	BroadcastReceiver receiver;
	
	// Defines callbacks for service binding, passed to bindService()
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			SMSService.MyBinder b = (SMSService.MyBinder) binder;
			smsService = b.getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			smsService = null;
		}
	};
	
	@Override
	protected void onRestart() {
		super.onRestart();
		UtilProperties.changeToTheme(this);
		Log.d(TAG, "onRestart");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		unbindService(mConnection);
		Log.d(TAG, "onPause otkacio se od servisa");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		Intent intent = new Intent(this, SMSService.class);
		
		//ako servis vec nije kreiran kreirace ga i nakacice se na njega Context.BIND_AUTO_CREATE
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onResume kreirao servis i nakacio se");
		
		/*******Brisanje svih notifikacija aplikacije********/
		NotificationContainer nDataStore = NotificationContainer.getInstance();
		nDataStore.getAllNData().clear();
		nDataStore.cancelNotification(this, 1);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), 
		        new IntentFilter("refresh"));
		
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onStop() {	
		super.onStop();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		
	    Log.d(TAG, "onStop");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		/************************Tabovi**********************/


		// Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
		adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);

		// Assigning ViewPager View and setting the adapter
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		// Assiging the Sliding Tab Layout View
		tabs = (SlidingTabLayout) findViewById(R.id.tabs);
		tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

		// Setting Custom Color for the Scroll bar indicator of the Tab View
		tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.colorPrimary);
			}
		});

		// Setting the ViewPager For the SlidingTabsLayout
		tabs.setViewPager(pager);

		/************************Tema************************/
		UtilProperties.onActivityCreateSetTheme(this);
		setContentView(R.layout.activity_main);

		/************************Sinhronizacija************************/		
		sync_after_sec = UtilProperties.changeFrequency(this);
		
		
		/************************Alarmi************************/
		UtilProperties.changeSound(this);		
		
		/***********************Baza*************************/
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(getBaseContext(), "pmasms-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		conversationDao = daoSession.getConversationDao();
		
		Intent intent = new Intent(this, SMSService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onCreate kreirao servis i nakacio se");

		// list adapter koji ce prikazati sve konverzacije
		listViewAdapter = new MainListViewAdapter();
		
		// popunjavanje main liste ...
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				 pd = ProgressDialog.show(MainActivity.this,"Loading SMSs..", "Please Wait", true, false);
			}

			@Override
			protected Void doInBackground(Void... params) {
				
				List<Conversation> listaca = getAllConversationFromPMA();
				listViewAdapter.setConversations(listaca);
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				ListView listView = (ListView) findViewById(R.id.listMainView);
				listView.setAdapter(listViewAdapter);
				// ON CLICK
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int rbrInListView, long id) {
						Intent intent = new Intent(getBaseContext(),ConversationActivity.class);
						Conversation listItem = (Conversation) parent.getAdapter().getItem(rbrInListView);
						
						intent.putExtra("thread_id", listItem.getId().toString());
						startActivity(intent);
					}
				});
				// ON HOLD
				listView.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						item_position_clicked = position;

						android.app.FragmentManager manager = getFragmentManager();

						ConversationItemDialog dialog = new ConversationItemDialog();
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
	            
	        	List<Conversation> listaca = getAllConversationFromPMA();
				listViewAdapter.setConversations(listaca);
				listViewAdapter.notifyDataSetChanged();
	            
	        }
	    };

	}
	
	public void createNewMessage(View view){
		Intent intent = new Intent(MainActivity.this, ComposeActivity.class);
		startActivity(intent);
	} 
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_actions, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
	            .getActionView();
	    if (null != searchView) {
	        searchView.setSearchableInfo(searchManager
	                .getSearchableInfo(new ComponentName(this, SearchResultsActivity.class)));//getComponentName()
	        searchView.setIconifiedByDefault(true);
	        
	    }

	    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
	        public boolean onQueryTextChange(String newText) {
	            // This is your adapter that will be filtered
	        	Log.d(TAG, "textChange = " + newText);
	        	
	        	
	        	//listViewAdapter.getFilter().filter(newText);
	        	
	        	return false;
	        }

	        public boolean onQueryTextSubmit(String query) {
	            // **Here you can get the value "query" which is entered in the search box.**
	        	Log.d(TAG, "Query = " + query);
	        	Intent searchIntent = new Intent(getApplicationContext(), SearchResultsActivity.class);
	        	searchIntent.putExtra("query", query);
	        	startActivity(searchIntent);
	        	
	        	return false;
	        }
	    };

		//INFO: posle prelaska na compact temu linija ispod je stavljena pod komentar
		// pri tome funkcionalnost nije ugrozena
		// searchView.setOnQueryTextListener(queryTextListener);
	    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			Intent intent = new Intent(this, ActivitySettings.class);
			startActivity(intent);
			break;
		case R.id.action_refresh:
			startSync();
			break;
		case R.id.action_mykey:
			Intent intent2 = new Intent(this, PublicKeyInfoActivity.class);
			DaoFactory daoFactory = new DaoFactory();
			UserDao contactDao = (UserDao) daoFactory.getDaoObject("UserDao", this);
			User user = contactDao.load(1L);
			
			intent2.putExtra("publicKey", user.getPublicKey());
			
			startActivity(intent2);
			
			break;
		}

		/*
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}*/

		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Kupi sve konverzacije sortirane po datumu
	 * @return List<Conversation>
	 */
	private List<Conversation> getAllConversationFromPMA(){
		
		String timeColumn = ConversationDao.Properties.TimeForLastSMS.columnName;
        String orderBy = timeColumn + " DESC";
        Cursor cursor = db.query(conversationDao.getTablename(), conversationDao.getAllColumns(), null, null, null, null, orderBy);
        
		List<Conversation> listaca = new ArrayList<Conversation>();
		cursor.moveToFirst();
		for(int i=0; i<cursor.getCount(); i++){
			Conversation conv = conversationDao.readEntity(cursor,0);
			listaca.add(conv);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return listaca;
	}

	
	@Override
	public void messageFromDialog(Class<?> cls, String strData, int intData) {
		
		Log.d(TAG, "communicator message from dialog = " + cls.getName());
		Conversation conversationTemp = conversationDao.load(listViewAdapter.getConversations()
				.get(item_position_clicked).getId());
		
		switch (intData) {
		
			case 0:
				// Call number from conversation
				String phone_number = listViewAdapter.getConversations().get(item_position_clicked).getPhoneNumberC();
				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number)); 
		        startActivity(callIntent);
				break;
			
			case 1:
				// Show Item Name TODO public key info
				String name = listViewAdapter.getConversations().get(item_position_clicked).getSenderName();
				Intent intent = new Intent(this, PublicKeyInfoActivity.class);
				intent.putExtra("name", name);
				intent.putExtra("contactId", conversationTemp.getContact().getId());
				String publicKey = conversationTemp.getContact().getPublicKey();
				intent.putExtra("publicKey", publicKey);
				startActivity(intent);

				break;
	
			case 2:
				// Delete Item
				showAlertConfirm();
				break;

		}
	}
	
	void showAlertConfirm() {
		
		final Conversation conv = listViewAdapter.getConversations().get(item_position_clicked);
		final String item_name = conv.getSenderName();
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		// Setting Dialog Title
		alertDialog.setTitle("Confirm Delete...");

		// Setting Dialog Message
		alertDialog.setMessage("Are you sure you want delete conversation: " + item_name
				+ "?");

		// Setting Positive "Yes" Button
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO remove selected conv from db [local and phone]
						listViewAdapter.getConversations().remove(item_position_clicked);
						listViewAdapter.notifyDataSetChanged();
						Conversation co = conversationDao.load(conv.getId());
						
						for(SMS sms : co.getSmslist()){
							daoSession.getSMSDao().delete(sms);
						}
						conversationDao.delete(co);
						
						Toast.makeText(getApplicationContext(), item_name + " Deleted", Toast.LENGTH_SHORT).show();
						
					}
				});

		// Setting Negative "Cancel" Button
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.cancel();
					}
				});

		alertDialog.show();

	}
	
	public void startSync() {
		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(MainActivity.this, SyncService.class);
		intent.putExtra("isSyncAll", true);
		PendingIntent pintent = PendingIntent.getService(MainActivity.this, 1, intent, 0);
		
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sync_after_sec*1000, pintent);
	}
	
}
