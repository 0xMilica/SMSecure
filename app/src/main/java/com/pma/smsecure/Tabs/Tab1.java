package com.pma.smsecure.Tabs;

/**
 * Created by Milica on 06-Oct-16.
 */

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Dao.ConversationDao;
import com.pma.smsecure.Dao.DaoMaster;
import com.pma.smsecure.Dao.DaoSession;
import com.pma.smsecure.R;
import com.pma.smsecure.Service.SMSService;

import java.util.ArrayList;
import java.util.List;

public class Tab1 extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_1, container, false);
        return v;
    }
    // db atributi
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private ConversationDao conversationDao;

    /***********************Baza*************************/
    //DevOpenHelper helper = new DaoMaster.DevOpenHelper(getBaseContext(), "pmasms-db", null);
    //db = helper.getWritableDatabase();
    //daoMaster = new DaoMaster(db);
    //daoSession = daoMaster.newSession();
  // conversationDao = daoSession.getConversationDao();

    //Intent intent = new Intent(this, SMSService.class);
    //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    //Log.d(TAG, "onCreate kreirao servis i nakacio se");

    // list adapter koji ce prikazati sve konverzacije
  /*  listViewAdapter = new MainListViewAdapter();
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
	}*/
}