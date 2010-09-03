package com.connectsy.categories;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.connectsy.data.DataManager;

public class CategoryManager extends DataManager {

	public static class Category{
		public String name;
		public int id;
		public ArrayList<Category> categories;
		
		public Category(String json) throws JSONException{
			JSONObject catJSON = new JSONObject(json);
			name = catJSON.getString("name");
			id = catJSON.getInt("id");
			if (catJSON.has("categories"))
				categories = Category.deserializeList(catJSON.getString("categories"));
		}
		
		public String serialize(){
			JSONObject ret = new JSONObject();
			try {
				ret.put("name", name);
				ret.put("id", id);
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
	
	public ArrayList<Category> getCategories(){
		ArrayList<Category> cats = new ArrayList<Category>();
		try {
			cats = Category.deserializeList("[{\"name\": \"Category One\", \"id\": 1}, {\"name\": \"Category Two\", \"id\": 2, \"categories\": [{\"name\": \"Category Two One\", \"id\": 3}, {\"name\": \"Category Two Two\", \"id\": 4}]}]");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cats;
	}
}
