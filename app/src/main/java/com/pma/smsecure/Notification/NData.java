package com.pma.smsecure.Notification;

public abstract class NData{

	/*
	 * identifikator za notifikaciju
	 */
	private String notificationName;
	public int smallIconId;
	public String contentTitle;
	public String contentText;
	
	public NData(String notificationName){
		this.notificationName = notificationName; 
	}
	
	// TODO brisi iz liste na osnovu tipa objekta
	// void clearAll() ..

	public String getNotificationName() {
		return notificationName;
	}
}
