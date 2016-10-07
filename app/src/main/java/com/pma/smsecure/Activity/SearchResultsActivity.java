package com.pma.smsecure.Activity;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pma.smsecure.R;
import com.pma.smsecure.Adapter.SearchListViewAdapter;
import com.pma.smsecure.Dao.ConversationDao.Properties;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Dao.SMSDao;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Properties.UtilProperties;

import de.greenrobot.dao.query.QueryBuilder;

public class SearchResultsActivity extends Activity {

	private SearchListViewAdapter searchAdapter;
	private String query;
	private int resultCount;
	private ProgressDialog pd;
	private ActionBar actionBar;
	private final String TAG = "SearchResultsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/************************Tema************************/
		UtilProperties.onActivityCreateSetTheme(this);
		
		setContentView(R.layout.activity_search_results);

		Intent intent = getIntent();
		query = intent.getExtras().getString("query");

		searchAdapter = new SearchListViewAdapter(this, query);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// popunjavanje liste
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				pd = ProgressDialog.show(SearchResultsActivity.this,"Searching..", "Please Wait", true, false);
			}

			@Override
			protected Void doInBackground(Void... params) {

				query = query.trim();
				//Log.d(TAG, "pretrazi bazu za dati query = " + query);

				DaoFactory daoFactory = new DaoFactory();
				SMSDao smsDao = (SMSDao) daoFactory.getDaoObject("SMSDao",
						getBaseContext());

				QueryBuilder<SMS> queryBuilder = smsDao.queryBuilder();

				ArrayList<SMS> smss = (ArrayList<SMS>) queryBuilder.where(
						SMSDao.Properties.Message.like("%"+query+"%")).list();

				resultCount = smss.size();
				searchAdapter.setMessages(smss);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				String title = resultCount + " results for \""+query+"\"";
				actionBar.setTitle(title);

				ListView listView = (ListView) findViewById(R.id.listSearchResult);
				listView.setAdapter(searchAdapter);

				// on click go to conversation
				// TODO: index for sms focus
				listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int rbrInListView, long id) {
						Intent intent = new Intent(getBaseContext(),
								ConversationActivity.class);
						SMS listItem = (SMS) parent.getAdapter().getItem(
								rbrInListView);

						intent.putExtra("thread_id", listItem
								.getConversationId().toString());
						startActivity(intent);
					}
				});

				pd.dismiss();
			}
		}.execute((Void[]) null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_results, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
}
