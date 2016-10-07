package com.pma.smsecure.Fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pma.smsecure.R;
import com.pma.smsecure.Activity.SplashScreen;
import com.pma.smsecure.AsyncTasks.LoadViewSplashScreen;

public class PhoneNumberDialog extends DialogFragment{
	
	private ICommunicator communicator;
	private EditText txtPhoneNumber;
	private Button btnOk;
	private Activity sScreen;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.dialog_phone_number, container);
		txtPhoneNumber = (EditText) view.findViewById(R.id.txtPhoneNumber);
		btnOk = (Button)view.findViewById(R.id.btnOkPhoneNumber);
		getDialog().setTitle("Unknow phone number.\nPlease enter the information manually.");
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				communicator.messageFromDialog(PhoneNumberDialog.class, txtPhoneNumber.getText().toString(), -1);
				dismiss();
				new LoadViewSplashScreen((SplashScreen)sScreen).execute();
				
			}
		});
		
        return view;
	}
	
	// poziva se pre svih u zivotnom ciklusu fragmenta
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		sScreen = activity;
		if (activity instanceof ICommunicator) {
			communicator = (ICommunicator) getActivity();
		} else {
			throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.communicator");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
}
