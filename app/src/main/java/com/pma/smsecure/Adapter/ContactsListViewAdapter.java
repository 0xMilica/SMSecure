package com.pma.smsecure.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.SMS;
import com.pma.smsecure.Helper.ContactPma;
import com.pma.smsecure.Helper.SMSHelper;
import com.pma.smsecure.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 24-Dec-16.
 */
public class ContactsListViewAdapter extends BaseAdapter{

    private List<ContactPma> contacts = new ArrayList<ContactPma>();

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_contact, parent, false);
        }
        TextView contactName = (TextView)convertView.findViewById(R.id.txtViewContactName);
        TextView phoneNumber = (TextView)convertView.findViewById(R.id.txtViewContactPhoneNum);
        ContactPma contact = contacts.get(position);
        String phoneNum = contact.getPhoneNumber();
        contactName.setText(contact.getName());
        phoneNumber.setText(phoneNum);
        return convertView;
    }

    public void setContacts(List<ContactPma> contacts) {
        this.contacts = contacts;
    }

    public List<ContactPma> getContacts() {
        return contacts;
    }
}
