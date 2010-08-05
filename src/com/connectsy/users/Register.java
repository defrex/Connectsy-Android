package com.connectsy.users;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;

public class Register extends Activity implements OnClickListener, ApiRequestListener {
	private ProgressDialog loadingDialog;
	private static final String TAG = "Register";
	
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.auth_register);

        Button register = (Button)findViewById(R.id.auth_register_button);
        register.setOnClickListener(this);
    }

	public void onClick(View arg0) {
    	EditText usernameText = (EditText)findViewById(R.id.auth_register_username);
    	EditText passwordText = (EditText)findViewById(R.id.auth_register_password);
    	String username = usernameText.getText().toString();
    	String password = passwordText.getText().toString();
    	
        try {
        	JSONObject body = new JSONObject();
        	body.put("password", password);
        	
			new ApiRequest(this, this, Method.PUT, "/users/"+username+"/", 
					body.toString(), null, false, 0).execute();
		} catch (JSONException e) {
			e.printStackTrace();
		}
        loadingDialog = ProgressDialog.show(this, "", "Registering...", true);
	}

	public void onApiRequestFinish(int status, String response, int code) {
		loadingDialog.dismiss();
		if (status == 200){
			EditText usernameText = (EditText)findViewById(R.id.auth_register_username);
			EditText passwordText = (EditText)findViewById(R.id.auth_register_password);
		    
			Intent intent = new Intent(this, Register.class);
			intent.putExtra("username", usernameText.getText().toString());
			intent.putExtra("password", passwordText.getText().toString());
			
		    setResult(RESULT_OK, intent);
		    this.finish();
		}else{
			AlertDialog.Builder alert = new AlertDialog.Builder(this); 
			alert.setMessage("An Error occured, please try again."); 
			alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				    dialog.dismiss();
				}
			});
			alert.show();
		}
	}

	public void onApiRequestError(int httpStatus, int code) {
		Log.e(TAG, "an API error occured");
		loadingDialog.dismiss();
	}
}
