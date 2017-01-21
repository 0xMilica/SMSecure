package com.pma.smsecure.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pma.smsecure.Activity.ConversationActivity;
import com.pma.smsecure.Activity.MainActivity;
import com.pma.smsecure.Activity.PublicKeyInfoActivity;
import com.pma.smsecure.Adapter.ContactsListViewAdapter;
import com.pma.smsecure.Dao.Contact;
import com.pma.smsecure.Dao.ContactDao;
import com.pma.smsecure.Dao.Conversation;
import com.pma.smsecure.Fragment.ConversationItemDialog;
import com.pma.smsecure.Helper.ContactPma;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.R;

import java.util.ArrayList;
import java.util.List;
/**
    Ako bude potreno da se prikaze i naziv coveka umesto samo broja onda ti treba poziv
    SMSHelper.getContactName(getBaseContext(), phone_number);
 */
public class LoadContactsTask extends AsyncTask<Void, Void, Void> {

    private ContactsListViewAdapter contactsListViewAdapter;
    private DaoFactory daoFactory = new DaoFactory();
    private Context context;
    private Activity activity;
    private ListView listViewContacts;

    public LoadContactsTask(ContactsListViewAdapter contactsListViewAdapter, Activity activity){
        super();
        this.contactsListViewAdapter = contactsListViewAdapter;
        this.context = activity.getBaseContext();
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<Contact> contacts = getContactsPMAPublicKeyOnly();//ili getContactsFromPMA akozelis da uzmes sve kontakte iz applikacije
        List<ContactPma> newContacts = new ArrayList<ContactPma>();
        for(Contact c : contacts){
            newContacts.add(new ContactPma(c, context));
        }
        contactsListViewAdapter.setContacts(newContacts);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        this.listViewContacts = (ListView) activity.findViewById(R.id.listMainView2);
        listViewContacts.setAdapter(contactsListViewAdapter);
        // ON CLICK
        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int rbrInListView, long id) {
                Intent intent = new Intent(activity.getApplicationContext(), PublicKeyInfoActivity.class);

                ContactPma listItem = (ContactPma) parent.getAdapter().getItem(rbrInListView);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// !! moze praviti kasnije problem, al jbg
                intent.putExtra("name", listItem.getName());
                intent.putExtra("contactId", listItem.getId());
                intent.putExtra("publicKey", listItem.getPublicKey());

                context.startActivity(intent);
            }
        });
//        // ON HOLD
//        listViewContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                item_position_clicked = position;
//
//                android.app.FragmentManager manager = getFragmentManager();
//
//                ConversationItemDialog dialog = new ConversationItemDialog();
//                dialog.show(manager, "dialog");
//                return true;
//            }
//        });

    }

    private List<Contact> getContactsFromPMA(){
        ContactDao contactDao = (ContactDao) daoFactory.getDaoObject("ContactDao", context);
        return contactDao.loadAll();
    }

    private List<Contact> getContactsPMAPublicKeyOnly(){
        ContactDao contactDao = (ContactDao) daoFactory.getDaoObject("ContactDao", context);
        List<Contact> ret = new ArrayList<Contact>();
        for(Contact c : contactDao.loadAll()){
            if(c.getPublicKey() != null)
                if(c.getPublicKey().isEmpty())
                    ret.add(c);

        }
        return ret;
    }
}
