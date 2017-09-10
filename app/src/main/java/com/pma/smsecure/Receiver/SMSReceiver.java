package com.pma.smsecure.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.pma.smsecure.Service.SMSService;

public class SMSReceiver extends BroadcastReceiver {

	private String phone_num = "";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// ova metoda treba da bude brza ! 
		
		//	Prijemnik poruka postoji samo u toku izvrsavanja onReceive
		//	metode (zato se u ovoj metodi ne mogu izvrsiti asinhrone
		//	operacije kao sto su prikazivanje dijaloga ili vezivanje za servis)
		Log.i("SMSReceiver", "SMSReceiver " + intent);
		Log.d("sms servis", "startovan, metoda onReceive obradi poruku");
		
		// Parse the SMS.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null)
        {
            // Retrieve the SMS.
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++)
            {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                str += msgs[i].getMessageBody().toString();
                str += "\n";
                Log.d("sms servis","poruka od = " + msgs[i].getOriginatingAddress());
                Log.d("sms servis","servisni centar = " + msgs[i].getServiceCenterAddress());
                phone_num = msgs[i].getOriginatingAddress();
                //}
            }
            if (str != "") { // remove the last \n
                str = str.substring(0, str.length()-1);
            }
			Log.d("sms servis","poruka = \n" + str);
		}
        
        //put in intent phone number and message id
		Intent i = new Intent(context, SMSService.class);
		i.putExtra("sender", phone_num);
		i.putExtra("smsContent", str);
		i.putExtra("isSecure", false);
		context.startService(i);
	}

}
