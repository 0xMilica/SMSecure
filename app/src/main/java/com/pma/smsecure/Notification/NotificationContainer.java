package com.pma.smsecure.Notification;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;

/**
 * Get current notification is not possible due to security issues.
 * Its like stealing/intercepting other app's info which is not allowed in android framework.
 * NotificationDataHelper implements singletone design pattern and store all the data shown by notifications.
 */
public class NotificationContainer {
	
	private List<NData> notificationsData = new ArrayList<NData>();
	private static NotificationContainer notificationDataHelper;
	
	private NotificationContainer(){}
	
	public static NotificationContainer getInstance(){
		
		if(notificationDataHelper == null)
			notificationDataHelper = new NotificationContainer();
		
		return notificationDataHelper;
	}
	
	public void addNData(NData data){
		notificationsData.add(data);
	} 
	
	public boolean removeNData(NData data){
		return notificationsData.remove(data);
	}
	
	public List<NData> getAllNData(){
		return notificationsData;
	}
	
	public void cancelNotification(Context ctx, int notifyId) {
	    String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
	    nMgr.cancel(notifyId);
	}
}
