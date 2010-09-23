package com.connectsy.categories;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;
import com.connectsy.utils.DateUtils;

public class CategoryManager extends DataManager {

	public static class Category{
		public String name;
		public int id;
		public ArrayList<Category> categories;
		
		public Category(String json) throws JSONException{
			JSONObject catJSON = new JSONObject(json);
			name = catJSON.getString("name");
			if (catJSON.has("categories"))
				categories = Category.deserializeList(catJSON.getString("categories"));
		}
		
		public String serialize(){
			JSONObject ret = new JSONObject();
			try {
				ret.put("name", name);
				if (categories != null)
					ret.put("categories", Category.serializeList(categories));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret.toString();
		}
		
		public static ArrayList<Category> deserializeList(String catsStr) throws JSONException{
			JSONArray catsJSON = new JSONArray(catsStr);
			ArrayList<Category> cats = new ArrayList<Category>();
			for(int i=0;i<catsJSON.length();i++)
				cats.add(new Category(catsJSON.getString(i)));
			return cats;
		}
		
		public static String serializeList(ArrayList<Category> cats){
			JSONArray catsJSON = new JSONArray();
			for (int i=0;i<cats.size();i++)
				catsJSON.put(cats.get(i).serialize());
			return catsJSON.toString();
		}
	}
	
	public CategoryManager(Context c, DataUpdateListener l) {
		super(c, l);
	}
	
	private ApiRequest getRequest(){
		return new ApiRequest(this, context, Method.GET, "/categories/", false, 0);
	}

	public ArrayList<Category> getCategories(){
		ArrayList<Category> cats = new ArrayList<Category>();
		try {
			ApiRequest r = getRequest();
			if (r.getCached() != null){
				cats = Category.deserializeList(r.getCached());
			}else{
				r.execute();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cats;
	}

	public void refreshCategories(){
		getRequest().execute();
	}
	
	public static void precacheCategories(Context c){
		ApiRequest r = new ApiRequest(null, c, Method.GET, "/categories/", false, 0);
		long cached = DataManager.getCache(c).getLong("categories_cached", 0);
		if (DateUtils.isCacheExpired(new Date(cached), 48) || r.getCached() == null){
			r.execute();
			DataManager.getCache(c).edit()
					.putLong("categories_cached", new Date().getTime()).commit();
		}
	}
}
