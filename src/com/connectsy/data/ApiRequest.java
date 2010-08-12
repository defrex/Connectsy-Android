package com.connectsy.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.connectsy.settings.Settings;

public class ApiRequest extends AsyncTask<Void, Void, HttpResponse> {
	
	public interface ApiRequestListener{
		public void onApiRequestFinish(int status, String response, int code);
		public void onApiRequestError(int httpStatus, int retCode);
	}

	private static final String TAG = "ApiRequest";

	private String url;
	private HttpRequestBase request;
	private DefaultHttpClient client = new DefaultHttpClient();
	private ApiRequestListener apiListener;
	private SharedPreferences data;
	private Method method;
	private String path;
	private String body;
	private List<NameValuePair> getArgs;
	private boolean authorized;
	private int retCode;
	
	public static enum Method { GET, PUT, POST, DELETE }
	
	public ApiRequest(ApiRequestListener listener, Context c, Method pMethod, 
			String pPath, String pBody, List<NameValuePair> pGetArgs, 
			boolean pAuthorized, int returnCode){
		apiListener = listener;
		data = DataManager.getCache(c);
		method = pMethod;
		path = pPath;
		body = pBody;
		getArgs = pGetArgs;
		authorized = pAuthorized;
		retCode = returnCode;
		
		prepRequest();
	}
	
	private void prepRequest(){
		try {
			url = Settings.API_DOMAIN+path+"?";
			if (getArgs == null)
				getArgs = new ArrayList<NameValuePair>();
			for (NameValuePair arg : getArgs)
				url += arg.getName()+"="+arg.getValue()+"&";
			
			if (method == Method.GET){
				request = new HttpGet(url);
			}else if (method == Method.POST){
				HttpPost post = new HttpPost(url);
				post.setEntity(new StringEntity(body));
				request = post;
			}else if (method == Method.PUT){
				HttpPut post = new HttpPut(url);
				post.setEntity(new StringEntity(body));
				request = post;
			}else if (method == Method.DELETE){
				// TODO
			}
			if (authorized){
				String token = data.getString("token", "tokenfail");
				request.addHeader("Authenticate", "Token "+token);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public List<NameValuePair> getGetArgs(){
		return getArgs;
	}
	public void setGetArgs(List<NameValuePair> pGetArgs){
		getArgs = pGetArgs;
		prepRequest();
	}
	
	public String getCached(){
		return data.getString(url, null);
	}
	
	public void addGetArg(String key, String value) throws URISyntaxException{
		url += key+"="+value+"&";
		request.setURI(new URI(url));
	}
	
	protected HttpResponse doInBackground(Void...arg0) {
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			response = null;
			Log.d(TAG, "hit ClientProtocolException in ApiRequest");
		} catch (IOException e) {
			e.printStackTrace();
			response = null;
			Log.d(TAG, "hit IOException in ApiRequest");
		}
		return response;
	}

	protected void onPostExecute(HttpResponse response) {
		if (response == null){
			apiListener.onApiRequestError(0, retCode);
			return;
		}else if(response.getStatusLine().getStatusCode()-200 > 100){
			apiListener.onApiRequestError(response.getStatusLine().getStatusCode(), 
					retCode);
			return;
		}
		String responseString = getResponseString(response);
		if (responseString != null && request.getMethod() == "GET"){
			data.edit().putString(url, responseString).commit();
		}
		apiListener.onApiRequestFinish(response.getStatusLine().getStatusCode(), 
				responseString, retCode);
	}
	
	private String getResponseString(HttpResponse response){
		String resp = null;
		try {
			resp = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return resp;
	}
}
