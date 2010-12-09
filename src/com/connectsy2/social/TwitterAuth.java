package com.connectsy2.social;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.connectsy2.data.Analytics;
import com.connectsy2.data.ApiRequest;
import com.connectsy2.data.ApiRequest.ApiRequestListener;
import com.connectsy2.data.ApiRequest.Method;
import com.connectsy2.settings.Settings;
import com.connectsy2.users.UserManager;

public class TwitterAuth extends Activity implements ApiRequestListener {

	private static final String TAG = "TwitterAuth";
	private static final int GET_CREDS = 0;
	private ProgressDialog loadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Analytics.pageView(this, this.getClass().getName());

		loadingDialog = ProgressDialog.show(this, 
				"", "Authorizing...", true);
		
        ApiRequest r = new ApiRequest(this, this, Method.GET, "/social/twitter/"
        		+UserManager.currentUsername(this), true, GET_CREDS);
        String resp = r.getCached();
        if (resp != null)
        	useCred(resp);
        else
        	r.execute();
	}

	public void onApiRequestError(int httpStatus, String response, int retCode) {
		if (httpStatus == 404) renewAuth();
	}

	private void renewAuth() {
//		OAuthSignpostClient client = new OAuthSignpostClient(
//				Settings.TWITTER_KEY, Settings.TWITTER_SECRET, 
//				"consy://twitter-done");
//		client.setProvider(new CommonsHttpOAuthProvider(
//                "https://api.twitter.com/oauth/request_token",
//                "https://api.twitter.com/oauth/access_token",
//                "https://api.twitter.com/oauth/authorize"));
//		try{
//			URI url = client.authorizeUrl();
//	        Log.d(TAG, "forward to url: "+url);
//		}catch(TwitterException e){
//			e.printStackTrace();
//			e.getCause().printStackTrace();
//			e.getCause().getCause().printStackTrace();
//		}
		
		Log.d(TAG, "key: "+Settings.TWITTER_KEY+
				" secret: "+Settings.TWITTER_SECRET);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				Settings.TWITTER_KEY, Settings.TWITTER_SECRET);
		CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");
		
		try {
			String authUrl = provider.retrieveRequestToken(consumer, 
					"consy://twitter-done");
			Log.d(TAG, "got URL: "+authUrl);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        //TODO: get the user's response
	}

	public void onApiRequestFinish(int status, String response, int code) {
		useCred(response);
	}
	
	private void useCred(String response){
		try {
			JSONObject resp = new JSONObject(response);

			Intent i = new Intent();
			i.putExtra("com.connectsy2.social.twitter.TOKEN", 
					resp.getString("token"));
			i.putExtra("com.connectsy2.social.twitter.SECRET", 
					resp.getString("sectet"));
			setResult(RESULT_OK, i);
			if (loadingDialog != null) loadingDialog.dismiss();
			finish();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
