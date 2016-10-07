package com.pma.smsecure.Adapter;

import java.util.ArrayList;
import java.util.List;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.SMS;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchListViewAdapter extends BaseAdapter {

	private List<SMS> messages = new ArrayList<SMS>();
	private Activity activity;
	private String query;
	
	public SearchListViewAdapter(Activity activity, String query) {
		super();
		this.activity = activity;
		this.query = query;
	}
	
	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SMS sms = messages.get(position);
		
		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_search, parent, false);
	
        TextView nameAndNumber = (TextView)convertView.findViewById(R.id.txtNameAndNumber);
		TextView snippet = (TextView)convertView.findViewById(R.id.txtSnippet);
		
		nameAndNumber.setText(sms.getConversation().getSenderName()+" [ "+sms.getConversation().getPhoneNumberC()+" ]");
		// TODO: na osnovu trazenog stringa izdvoj i prikazi samo deo teksta;
		// bold - kljucnu rec iz upita
		snippet.setText(sms.getMessage());
		
		return convertView;
	}
	
	public String getQuery(){
		return this.query;
	}
	
	public void setQuery(String query){
		this.query = query;
	}

	public void setMessages(List<SMS> messages){
		this.messages = messages; 
	}
}
