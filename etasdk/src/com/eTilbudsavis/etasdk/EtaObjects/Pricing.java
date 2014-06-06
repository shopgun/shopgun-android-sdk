/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Pricing extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pricing";
	
	private double mPrice = 1.0d;
	private Double mPrePrice;
	private String mCurrency;
	
	public Pricing() {
		
	}
	
	public static Pricing fromJSON(JSONObject pricing) {
		return fromJSON(new Pricing(), pricing);
	}
	
	public static Pricing fromJSON(Pricing p, JSONObject pricing) {
		if (p == null) p = new Pricing();
		if (pricing == null) return p;
		
		try {
			p.setPrice(Json.valueOf(pricing, ServerKey.PRICE, 1.0d));
			p.setPrePrice(pricing.isNull(ServerKey.PREPRICE) ? null : pricing.getDouble(ServerKey.PREPRICE));
			p.setCurrency(Json.valueOf(pricing, ServerKey.CURRENCY));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return p;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.PRICE, getPrice());
			o.put(ServerKey.PREPRICE, Json.nullCheck(getPrePrice()));
			o.put(ServerKey.CURRENCY, Json.nullCheck(getCurrency()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public double getPrice() {
		return mPrice;
	}

	public Pricing setPrice(double price) {
		mPrice = price;
		return this;
	}

	public Double getPrePrice() {
		return mPrePrice;
	}

	public Pricing setPrePrice(Double prePrice) {
		mPrePrice = prePrice;
		return this;
	}

	public String getCurrency() {
		return mCurrency;
	}

	public Pricing setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mCurrency == null) ? 0 : mCurrency.hashCode());
		result = prime * result
				+ ((mPrePrice == null) ? 0 : mPrePrice.hashCode());
		long temp;
		temp = Double.doubleToLongBits(mPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pricing other = (Pricing) obj;
		if (mCurrency == null) {
			if (other.mCurrency != null)
				return false;
		} else if (!mCurrency.equals(other.mCurrency))
			return false;
		if (mPrePrice == null) {
			if (other.mPrePrice != null)
				return false;
		} else if (!mPrePrice.equals(other.mPrePrice))
			return false;
		if (Double.doubleToLongBits(mPrice) != Double
				.doubleToLongBits(other.mPrice))
			return false;
		return true;
	}

	
}
