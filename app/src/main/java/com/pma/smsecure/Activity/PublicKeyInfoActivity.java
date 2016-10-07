package com.pma.smsecure.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.Properties.UtilProperties;

public class PublicKeyInfoActivity extends Activity {

	private EditText txtPublicKey;
	private long contact_id = -1;
	private DaoFactory daoFactory = new DaoFactory();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/************************Tema************************/
		UtilProperties.onActivityCreateSetTheme(this);
		
		setContentView(R.layout.activity_public_key_info);
		
		txtPublicKey = (EditText) findViewById(R.id.txtPublicKey);
		TextView txtViewContact = (TextView) findViewById(R.id.textViewContact);
		
		Intent intent = getIntent();
		
		String publicKey = intent.getStringExtra("publicKey");
		String contact = intent.getStringExtra("name");
		
		contact_id = intent.getLongExtra("contactId", -1L);
			
		if(publicKey != null && !publicKey.isEmpty())
			txtPublicKey.setText(publicKey);
		else
			txtPublicKey.setText("Public key does not exist");
		if(contact == null){
			txtViewContact.setText("My Key Info:");
		}
		else
			txtViewContact.setText(contact);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.public_key_info, menu);
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
	
	// Set new public key for contact
	public void importNewPublicKey(View view){
	
		String publicKey = txtPublicKey.getText().toString();
		if(publicKey.equals("") || publicKey.contains("not exist")){
			Toast.makeText(getApplicationContext(), "Key modification faild", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(contact_id == -1L)
			{
				UserDao userDao = (UserDao) daoFactory.getDaoObject("UserDao", this);
				User user = userDao.load(1L);
				user.setPublicKey(publicKey);
				userDao.update(user);
				
			}
			else{
				ContactDao contactDao = (ContactDao) daoFactory.getDaoObject("ContactDao", this);
				Contact contact = contactDao.load(contact_id);
				contact.setPublicKey(publicKey);
				contactDao.update(contact);		
			}
		
			Toast.makeText(getApplicationContext(), "Key was successfully modified manually", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Share public key..
	public void exportPublicKey(View view){
		
		String publicKey = txtPublicKey.getText().toString();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, publicKey);
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}
}
