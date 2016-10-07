package com.pma.smsecure.Helper;

import java.util.ArrayList;

import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {

	public static void sendPlainTextSMS(String phoneNumber, String msg) {

		try {
			// get a SmsManager
			SmsManager smsManager = SmsManager.getDefault();
			// Message may exceed 160 characters
			// need to divide the message into multiples
			ArrayList<String> parts = smsManager.divideMessage(msg);
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null,
					null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendEncryptedSMS(String phoneNumber, String encryptedMsg) {
		
		// The maximum SMS message size is 140 bytes !
		// Mi delimo na 101, iz nekog razloga ne radi sa vise !??
		try {
			SmsManager smsManager = SmsManager.getDefault();
			short port = 6734;
			
			int x = 1;// broj delova
			byte[] smsParts = encryptedMsg.getBytes(); // poruka podeli u niz bajtova
			
			byte[] partTemp = new byte[101];
			ArrayList<byte[]> dividedMsg = new ArrayList<byte[]>();
			
			if(smsParts.length > 99){
				for(int i=0, j=0; i<smsParts.length; i++,j++){//182
					
					partTemp[j] = smsParts[i];
					
					if(i == 99 * x){
						partTemp[j+1]=" ".getBytes()[0]; // " " SE MORAJU IZBACITI PRE SNIMANJA U BAZU
						j=-1;
						x++;
						dividedMsg.add(partTemp);
						partTemp = new byte[101];
					}
				}
				if(x>dividedMsg.size()){
					// dodaj poslednji deo poruke
					dividedMsg.add(partTemp);
				}
				// za svaki deo pusti poruku
				for(int i=0; i<x; i++){
					Log.d("kripto delovi",new String(dividedMsg.get(i)));
					smsManager.sendDataMessage(phoneNumber, null, port, dividedMsg.get(i), null, null);
				}
				
			}
			else{
				
				smsManager.sendDataMessage(phoneNumber, null, port, smsParts, null, null);	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
