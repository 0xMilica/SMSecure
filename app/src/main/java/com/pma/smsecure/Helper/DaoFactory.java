package com.pma.smsecure.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.pma.smsecure.Dao.DaoMaster;
import com.pma.smsecure.Dao.DaoMaster.DevOpenHelper;
import com.pma.smsecure.Dao.DaoSession;
// TODO ovo bas i nije najsrecnije
// izbaciti getDaoObject, treba da ostane samo getNewDaoSession sa 
// manjom dopunom
public class DaoFactory {
	
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	
	/**
	 * Create new dao session and return daoObject for daoClassName
	 * @param daoClassName
	 * @param context
	 * @return Object
	 */
	public Object getDaoObject(String daoClassName, Context context){
		
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "pmasms-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		
		if(daoClassName == null)
			return null;
		
		if(daoClassName.equals("ContactDao")){
			return daoSession.getContactDao();
		}
		else if(daoClassName.equals("ConversationDao")){
			return daoSession.getConversationDao();
		}
		else if(daoClassName.equals("SMSDao")){
			return daoSession.getSMSDao();		
		}
		else if(daoClassName.equals("UserDao")){
			return daoSession.getUserDao();
		}
		
		return null;
	}

	public SQLiteDatabase getSQLiteDatabase(){
		return db;
	}
	
	// TODO ostaviti samo ovu i metodu iznad
	public DaoSession getNewDaoSession(Context context){
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "pmasms-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}
}
