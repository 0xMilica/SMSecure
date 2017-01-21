package com.pma.smsecure.Adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.pma.smsecure.R;
import com.pma.smsecure.Dao.Conversation;

public class MainListViewAdapter extends BaseAdapter implements Filterable {

	private List<Conversation> conversations = new ArrayList<Conversation>();
	private SMSFilter smsFilter;
	
	/**
	 * This method tells the listview the number of rows it will require. 
	 * This count can come from your data source. It can be the size of your Data Source. 
	 * If you have your datasource as a list of objects, this value will be the size of the list.
	 */
	@Override
	public int getCount() {
		return conversations.size();
	}

	/** 
	 * This method helps ListView to get data for each row. 
	 * The parameter passed is the row number starting from 0. 
	 * In our List of Objects, this method will return the object at the passed index.
	 */
	@Override
	public Object getItem(int position) {
		return conversations.get(position);
	}

	/**
	 * This in general helps ListView to map its rows to the data set elements.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * This is the most important method. This method will be called to get the View for each row.
	 * This is the method where we can use our custom listitem and bind it with the data. 
	 * - The fist argument passed to getView is the listview item position ie row number. 
	 * - The second parameter is recycled view reference(as we know listview recycles a view, you can confirm through this parameter).
	 * - Third parameter is the parent to which this view will get attached to.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_conversation, parent, false);
        }

        TextView userInfo = (TextView)convertView.findViewById(R.id.txtViewUserInfo);
        TextView userMesage = (TextView)convertView.findViewById(R.id.txtViewLastMessage);
        TextView date = (TextView)convertView.findViewById(R.id.textViewDateOfLastMessage);
        TextView smsCount = (TextView)convertView.findViewById(R.id.textViewNumberOfMessages);
        TextView brojNeprocitanih = (TextView) convertView.findViewById(R.id.textViewNumNewMesages);
        
        Conversation conv = conversations.get(position);

        // broj ne procitanih
//        brojNeprocitanih.setText(conv.getSmslist());
        
        // broj telefona ili ime ako postoji
        userInfo.setText(conv.getSenderName());
        
        // sadrzaj poslednje poruke
        userMesage.setText(conv.getSnippet());
        
        // datum poslednje poruke
        date.setText(new SimpleDateFormat("hh:mm dd/MM").format(conv.getTimeForLastSMS()));
        
        // broj poruka u konverzaciji
        smsCount.setText(conv.getSmsCount().toString());
        
        getFilter();
        
        
        return convertView;
	}
	
	
	
	public List<Conversation> getConversations() {
		return conversations;
	}

	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}

	@Override
	public Filter getFilter() {
		if(smsFilter == null)
			smsFilter = new SMSFilter();
		return smsFilter;
	}
	
	/**
	 * Filter za poruke iz konverzacija
	 * @author albica
	 *
	 */
	private class SMSFilter extends Filter{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// Samo za test, pretraga treba da se radi po porukama - ne po konverzacijama
			FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Conversation> tempList = new ArrayList<Conversation>();

                // search conversation in conversations
                for (Conversation conversation : conversations) {
                    if (conversation.getSenderName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(conversation);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = conversations.size();
                filterResults.values = conversations;
            }

            return filterResults;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			conversations = (ArrayList<Conversation>) results.values;
            notifyDataSetChanged();
		}
		
	}

}
