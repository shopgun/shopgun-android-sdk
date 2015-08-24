/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Api.JsonKey;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class Dimension implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Dimension.class);

    public static final double DEF_DIMENSION = 0d;
    public static Parcelable.Creator<Dimension> CREATOR = new Parcelable.Creator<Dimension>() {
        public Dimension createFromParcel(Parcel source) {
            return new Dimension(source);
        }

        public Dimension[] newArray(int size) {
            return new Dimension[size];
        }
    };
    private double mWidth = DEF_DIMENSION;
    private double mHeight = DEF_DIMENSION;

    public static Dimension fromBitmap(Bitmap b) {
        Dimension d = new Dimension();
        d.setWidth(1); // magic number... always one
        double h = (double)((float)b.getHeight()/(float)b.getWidth());
        d.setHeight(h);
        return d;
    }

    public Dimension() {
    }

    private Dimension(Parcel in) {
        this.mWidth = in.readDouble();
        this.mHeight = in.readDouble();
    }

    public static Dimension fromJSON(JSONObject dimension) {
        Dimension d = new Dimension();
        if (dimension == null) return d;

        d.setWidth(Json.valueOf(dimension, JsonKey.WIDTH, DEF_DIMENSION));
        d.setHeight(Json.valueOf(dimension, JsonKey.HEIGHT, DEF_DIMENSION));

        return d;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKey.HEIGHT, Json.nullCheck(getHeight()));
            o.put(JsonKey.WIDTH, Json.nullCheck(getWidth()));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return o;
    }

    public Double getWidth() {
        return mWidth;
    }

    public Dimension setWidth(double width) {
        mWidth = width;
        return this;
    }

    public Double getHeight() {
        return mHeight;
    }

    public Dimension setHeight(double height) {
        mHeight = height;
        return this;
    }

    public boolean isSet() {
        return mWidth > DEF_DIMENSION && mHeight > DEF_DIMENSION;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(mHeight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mWidth);
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
        Dimension other = (Dimension) obj;
        if (Double.doubleToLongBits(mHeight) != Double
                .doubleToLongBits(other.mHeight))
            return false;
        if (Double.doubleToLongBits(mWidth) != Double
                .doubleToLongBits(other.mWidth))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mWidth);
        dest.writeDouble(this.mHeight);
    }


}
