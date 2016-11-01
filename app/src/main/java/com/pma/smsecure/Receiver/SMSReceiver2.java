package com.pma.smsecure.Receiver;

import com.pma.smsecure.Service.SMSService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver2 extends BroadcastReceiver {

	private static final String TAG = "SMSReceiver2";
	private String phoneNumber = "";
	private String smsContent = "";
	 
	public SMSReceiver2() {
    }
 
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the data (SMS data) bound to intent
        Bundle bundle = intent.getExtras();
        Log.d(TAG,"onReceive u secure prijemniku poruke");
        
        SmsMessage[] msgs = null;
 
        String str = "";// samo za debug, da se vidi sta je stiglo
 
        if (bundle != null){
            // Retrieve the Binary SMS data
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length]; //as many pdus are there, as much SMS messages will be created
 
            // For every SMS message received (although multipart is not supported with binary)
            for (int i=0; i<msgs.length; i++) {
                byte[] data = null;
 
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
 
                str += "Binary SMS from " + msgs[i].getOriginatingAddress() + " :";
                phoneNumber = msgs[i].getOriginatingAddress();
 
                str += "\nBINARY MESSAGE: ";
 
                // Return the User Data section minus the
                // User Data Header (UDH) (if there is any UDH at all)
                data = msgs[i].getUserData();
 
                // Generally you can do away with this for loop
                // You'll just need the next for loop
                for (int index=0; index < data.length; index++) {
                    str += Byte.toString(data[index]);
                }
 
                str += "\nTEXT MESSAGE (FROM BINARY): ";
 
                for (int index=0; index < data.length; index++) {
                    str += Character.toString((char) data[index]);
                    smsContent+=Character.toString((char) data[index]);
                }
 
                str += "\n";
            }
       
            // u intent ubaci broj telefona i sadrzaj poruke i naglasi da je kriptovana
    		Intent i = new Intent(context, SMSService.class);
    		i.putExtra("sender", phoneNumber);
    		i.putExtra("smsContent", smsContent);
    		i.putExtra("isSecure", true);
    		context.startService(i);
	    	
        }
    }
}