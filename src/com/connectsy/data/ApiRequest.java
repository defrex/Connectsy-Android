package com.connectsy.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
import android.util.Log;

import com.connectsy.settings.Settings;

public class ApiRequest extends AsyncTask<Void, Void, HttpResponse> {
	
	public interface ApiRequestListener{
		/**
		 * Called when an ApiRequest completes successfully.
		 * 
		 * @param status HTTP status code
		 * @param response Response body
		 * @param code retCode passed in the ApiRequest constructor
		 */
		public void onApiRequestFinish(int status, String response, int code);
		/**
		 * Called when an ApiRequest completes unsuccessfully.
		 * 
		 * @param httpStatus HTTP status code
		 * @param retCode retCode passed in the ApiRequest constructor
		 */
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
	private boolean authorized;
	private int retCode;
	private String body;
	private InputStream stream;
	private Long streamLength;
	private List<NameValuePair> getArgs = new ArrayList<NameValuePair>();
	private List<NameValuePair> headers = new ArrayList<NameValuePair>();
	
	public static enum Method { GET, PUT, POST, DELETE }
	
	/**
	 * Initializes a new API request
	 * 
	 * @param listener Listener object
	 * @param c Context
	 * @param pMethod HTTP method
	 * @param pPath Request path, including any query parameters
	 * @param pAuthorized If true, requests will be made with an auth header
	 * @param returnCode Integer passed to the callback function, used to identify the request type
	 */
	public ApiRequest(ApiRequestListener listener, Context c, Method pMethod, 
			String pPath, boolean pAuthorized, int returnCode){
		apiListener = listener;
		data = DataManager.getCache(c);
		method = pMethod;
		path = pPath;
		authorized = pAuthorized;
		retCode = returnCode;
	}
	
	private void prepRequest(){
		try {
			url = Settings.API_DOMAIN+path+"?";
			for (NameValuePair arg : getArgs)
				url += URLEncoder.encode(arg.getName())+"="+URLEncoder.encode(arg.getValue())+"&";
			
			if (method == Method.GET){
				request = new HttpGet(url);
			}else if (method == Method.POST){
				HttpPost post = new HttpPost(url);
				if (body != null)
					post.setEntity(new StringEntity(body));
				request = post;
			}else if (method == Method.PUT){
				HttpPut post = new HttpPut(url);
				if (body != null){
					post.setEntity(new StringEntity(body));
				}else if (stream != null){
					post.setEntity(new InputStreamEntity(stream, streamLength));
				}
				request = post;
			}else if (method == Method.DELETE){
				// TODO
			}
			if (authorized){
				String token = data.getString("token", "tokenfail");
				request.addHeader("Authenticate", "Token auth="+token);
			}

			for (NameValuePair header : headers)
				request.addHeader(header.getName(), header.getValue());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public List<NameValuePair> getGetArgs(){
		return getArgs;
	}
	public void setGetArgs(List<NameValuePair> pGetArgs){
		getArgs = pGetArgs;
	}

	public void setHeader(String headerName, String headerValue){
		headers.add(new BasicNameValuePair(headerName, headerValue));
	}

	public void setBodyString(String pBody){
		body = pBody;
	}
	public void setBodyFile(AssetFileDescriptor file) throws IOException{
		stream = file.createInputStream();
		streamLength = file.getLength();
	}
	
	public String getCached(){
		prepRequest();
		return data.getString(url, null);
	}
	
	public void addGetArg(String key, String value){
		getArgs.add(new BasicNameValuePair(key, value));
	}
	
	@Override
	protected void onPreExecute() {
		prepRequest();
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
			if (apiListener != null) apiListener.onApiRequestError(0, retCode);
			return;
		}else if(response.getStatusLine().getStatusCode()-200 > 100){
			if (apiListener != null) apiListener.onApiRequestError(
					response.getStatusLine().getStatusCode(), retCode);
			return;
		}
		String responseString = getResponseString(response);
		if (responseString != null && request.getMethod() == "GET"){
			data.edit().putString(url, responseString).commit();
		}
		if (apiListener != null) apiListener.onApiRequestFinish(
			response.getStatusLine().getStatusCode(), responseString, retCode);
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
