package com.connectsy.users;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;

public class Register extends Activity implements OnClickListener, ApiRequestListener {
	private ProgressDialog loadingDialog;
	@SuppressWarnings("unused")
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
    	EditText password2Text = (EditText)findViewById(R.id.auth_register_password2);
    	
    	String username = usernameText.getText().toString();
    	String password = passwordText.getText().toString();
    	String password2 = password2Text.getText().toString();
    	
    	String invalid = null;

    	if (username.equals(""))
    		invalid = "Username is required.";
    	else if (password.equals(""))
    		invalid = "Password is required.";
    	else if (!password.equals(password2))
    		invalid = "Passwords don't match.";
    	
    	if (invalid != null){
    		Toast t = Toast.makeText(this, invalid, 5000);
			t.setGravity(Gravity.TOP, 0, 20);
			t.show();
    		return;
    	}
    	
        try {
        	JSONObject body = new JSONObject();
        	body.put("password", password);
        	body.put("number", ((TelephonyManager)getSystemService(
        			Context.TELEPHONY_SERVICE)).getLine1Number());
        	
			ApiRequest r = new ApiRequest(this, this, Method.PUT, "/users/"+username+"/", 
					false, 0);
			r.setBodyString(body.toString());
			r.execute();
		} catch (JSONException e) {
			e.printStackTrace();
		}
        loadingDialog = ProgressDialog.show(this, "", "Registering...", true);
	}

	public void onApiRequestFinish(int status, String response, int code) {
		loadingDialog.dismiss();
		if (status == 201){
			EditText usernameText = (EditText)findViewById(R.id.auth_register_username);
			EditText passwordText = (EditText)findViewById(R.id.auth_register_password);
		    
			Intent intent = new Intent(this, Register.class);
			intent.putExtra("username", usernameText.getText().toString());
			intent.putExtra("password", passwordText.getText().toString());
			
		    setResult(RESULT_OK, intent);
		    this.finish();
		}else{
    		Toast t = Toast.makeText(this, "An Error occured, please try again.", 5000);
			t.setGravity(Gravity.TOP, 0, 20);
			t.show();
		}
	}

	public void onApiRequestError(int httpStatus, String response, int code) {
		loadingDialog.dismiss();
		String message = "Unknown Error: "+httpStatus;
		if (httpStatus == 409)
    		message = "Sorry, that username is taken.";
		else if (httpStatus == 404)
			message = "Invalid username. Only numbers, letters, and underscores please.";
		Toast t = Toast.makeText(this, message, 5000);
		t.setGravity(Gravity.TOP, 0, 20);
		t.show();
	}
}
