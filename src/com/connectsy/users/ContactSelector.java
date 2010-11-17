package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.users.ContactCursor.Contact;

public class ContactSelector extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static String TAG = "ContactSelector";
	ContactAdapter adapter;
    private ArrayList<Contact> chosenContacts;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selector);
        
        Button done = (Button)findViewById(R.id.user_select_done);
        done.setOnClickListener(this);
        done.setText("Select Contacts");
        
        Bundle e = getIntent().getExtras();
        if (e != null){
			try {
				if (e.containsKey("com.connectsy.contacts"))
					chosenContacts = Contact.deserializeList(
							e.getString("com.connectsy.contacts"));
		        
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
        }
		
        update();
    }

	private void update(){
        adapter = new ContactAdapter(this);
        if (chosenContacts != null)
        	adapter.setSelectedContacts(chosenContacts);
        ListView lv = (ListView)findViewById(R.id.user_list);
        lv.setAdapter(adapter);
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.user_select_done){
			String contacts = Contact.serializeList(adapter.getSelectedContacts());
			Intent i = new Intent();
			i.putExtra("com.connectsy.contacts",contacts);
			setResult(RESULT_OK, i);
			finish();
		}
	}
}