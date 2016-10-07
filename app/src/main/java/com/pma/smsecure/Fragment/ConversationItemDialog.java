package com.pma.smsecure.Fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pma.smsecure.R;

public class ConversationItemDialog extends DialogFragment implements OnItemClickListener {

	private ListView mylist;
	private ICommunicator communicator;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.dialog_conversation_fragment, null, false);
		mylist = (ListView) view.findViewById(R.id.dialog_list);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		if (activity instanceof ICommunicator) {
			communicator = (ICommunicator) getActivity();
		} else {
			throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.communicator");
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.dialog_items,
				android.R.layout.simple_list_item_1);

		mylist.setAdapter(adapter);
		mylist.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		dismiss();
		communicator.messageFromDialog(ConversationItemDialog.class, null, position);
	}
}
