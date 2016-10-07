package com.pma.smsecure.Fragment;

/**
 * Interfejs za dijaloge; 
 * Spaja aktivnost sa dijalogom koji startuje
 */
public interface ICommunicator {
	
	/***
	 * 
	 * @param dialogClass
	 * @param strData
	 * @param intData
	 */
	public void messageFromDialog(Class<?> dialogClass, String strData, int intData);
	
}
