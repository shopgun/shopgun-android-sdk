package com.eTilbudsavis.etasdk.NetworkInterface;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class Cache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";
	
	private static final long ITEM_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;

	// Define catchable types
	Map<String, String>	types = new HashMap<String, String>(4);
	
	private Map<String, Item> mItems = Collections.synchronizedMap(new HashMap<String, Cache.Item>());
	
	public Cache() {
		types.put("catalogs", Request.Param.FILTER_CATALOG_IDS);
		types.put("offers", Request.Param.FILTER_OFFER_IDS);
		types.put("dealers", Request.Param.FILTER_DEALER_IDS);
		types.put("stores", Request.Param.FILTER_STORE_IDS);
	}
	
	public void put(Item c) {
		mItems.put(c.key, c);
	}
	
	public Item get(Request<?> r) {
		if (r.getMethod() != Request.Method.GET)
			return null;
		
		
		return null;
	}
	
	public Item get(String key) {
		Item c = mItems.get(key);
		if (c == null)
			return null;
		
		if ( !c.isExpired() ) {
			mItems.remove(c);
		}
		return c;
	}
	
	public void clear() {
		mItems = new HashMap<String, Cache.Item>();
	}
	
	private Set<String> getFilter(String filterName, Bundle apiParams) {
		String tmp = apiParams.getString(filterName);
		Set<String> list = new HashSet<String>();
		Collections.addAll(list, TextUtils.split(tmp, ","));
		return list;
	}
	
	private String getErnPrefix(String type) {
		return "ern:" + type.substring(0, type.length()-1) + ":";
	}
	
	/*
	 * 
	 * If a request contains order_by, then it's not possible to assemble a new response
	 * from old cache items. But rather save the whole request, and reuse it.
	 * 
	 */
	
	@SuppressWarnings("rawtypes")
	public Response<?> get(String url, Bundle apiParams) {
		
		Response resp = null;
		
		String[] path = url.split("/");
		
		if (types.containsKey(path[path.length-1])) {
			// if last element is a type, then we'll expect a list
			String type = path[path.length-1];
			
			Set<String> ids = new HashSet<String>(0);
			String filter = types.get(type);
			if (apiParams.containsKey(filter)) {
				ids = getFilter(filter, apiParams);
			}
			
			// No ids? no catchable items...
			if (ids.size() == 0)
				return resp;

			// Get all possible items requested from cache
			JSONArray jArray = new JSONArray();
			for (String id : ids) {
				String ern = getErnPrefix(type) + id;
				Item c = get(ern);
				if (c != null) {
					jArray.put((JSONObject)c.object);
				}
			}
			
			// If cache had ALL items, then return the list.
			int size = jArray.length();
			if (size > 0 && (size == ids.size()) ) {
				
//				resp.set(200, jArray.toString());
			}
			
		} else if (types.containsKey(path[path.length-2])) {
			// if second to last element is a valid type, then we'll expect an item id
			// (this isn't always true, but if it isn't then the item-id, shouldn't be in cache )

			String id = path[path.length-1];
			String type = path[path.length-2];
			
			String ern = getErnPrefix(type) + id;
			Item c = get(ern);
			if (c != null) {
//				resp = new EtaResponse();
//				resp.set(c.statuscode, c.object.toString());
			}
		}
		
		return resp;
		
	}
	
	public class EtaResponse {
		
		public void set(int i, String s) {
			
		}
		
	}
	
	public static class Item {
		
		// Time of insertion
		public final long time;
		public final long ttl;
		public final Object object;
		public final String key;
		
		public Item(String key, Object o, long ttl) {
			this.time = System.currentTimeMillis();
			this.ttl = ttl;
			this.object = o;
			this.key = key;
		}
		
		public Item(String key, Object o) {
			this(key, o, ITEM_CACHE_TIME);
		}
		
		/**
		 * Returns true if the Item is still valid.
		 * 
		 * this is based on the time to live factor
		 * @return
		 */
		public boolean isExpired() {
			return (time - ttl) < System.currentTimeMillis();
		}
		
	}
	
}
