package com.pma.smsecure.Adapter;

import java.util.ArrayList;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.SMS;

public class ConversationListViewAdapter extends BaseAdapter {

	private List<SMS> messages = new ArrayList<SMS>();
	private Activity activity;
	
	
	public ConversationListViewAdapter(Activity activity) {
		super();
		this.activity = activity;
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
		
		PrettyTime p = new PrettyTime();
		String firstTime = p.format(sms.getTime());
		
		// sent = 2 ; inbox = 1
		if(sms.getFolder().equals("2")){
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_sms_sent, parent, false);
		
            TextView sms_content = (TextView)convertView.findViewById(R.id.sms_content_sent);
    		TextView sms_date = (TextView)convertView.findViewById(R.id.txt_sms_date_sent);
    		sms_date.setText(firstTime);
    		
    		SpannableStringBuilder builder = new SpannableStringBuilder();
    	    builder.append(sms.getMessage()).append("   ");
    	    if(sms.getIsEncrypted()){
	    	    builder.setSpan(new ImageSpan(activity, R.drawable.lock_sms),
	    	            builder.length() - 1, builder.length(), 0);
    	    }else{
    	    	builder.setSpan(new ImageSpan(activity, R.drawable.unlock_sms),
	    	            builder.length() - 1, builder.length(), 0);
    	    }
    		sms_content.setText(builder);
    		
		}else{
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_sms_rec, parent, false);
		
            TextView sms_content = (TextView)convertView.findViewById(R.id.sms_content_rec);
            TextView sms_date = (TextView)convertView.findViewById(R.id.txt_sms_date_rec);
            sms_date.setText(firstTime);
    		
    		SpannableStringBuilder builder = new SpannableStringBuilder();
    		builder.append(sms.getMessage()).append("   ");
    	    
    		if(sms.getIsEncrypted()){
	    	    builder.setSpan(new ImageSpan(activity, R.drawable.lock_sms),
	    	            builder.length() - 1, builder.length(), 0);
    	    }else{
    	    	builder.setSpan(new ImageSpan(activity, R.drawable.unlock_sms),
	    	            builder.length() - 1, builder.length(), 0);
    	    }
    		sms_content.setText(builder);
		}
		
		
		return convertView;
	}

	public List<SMS> getMessages() {
		return messages;
	}

	public void setMessages(List<SMS> messages) {
		this.messages = messages;
	}
	
	

}
