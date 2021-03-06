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

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @deprecated shopping list no longer maintained
 */
@Deprecated
public class Share implements Comparable<Share>, SyncState<Share>, IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Share.class);

    public static final String ACCESS_OWNER = "owner";
    public static final String ACCESS_READWRITE = "rw";
    public static final String ACCESS_READONLY = "r";
    public static final Parcelable.Creator<Share> CREATOR = new Parcelable.Creator<Share>() {
        public Share createFromParcel(Parcel source) {
            return new Share(source);
        }

        public Share[] newArray(int size) {
            return new Share[size];
        }
    };
    private String mName;
    private String mEmail;
    public static Comparator<Share> EMAIL_ASCENDING = new Comparator<Share>() {

        @SuppressLint("DefaultLocale")
        public int compare(Share item1, Share item2) {

            String e1 = item1.getEmail().toLowerCase();
            String e2 = item2.getEmail().toLowerCase();

            //ascending order
            return e1.compareTo(e2);

        }

    };
    private String mAccess;
    private String mShoppinglistId;
    private boolean mAccepted;
    private String mAcceptUrl;
    private int mSyncState = SyncState.TO_SYNC;

    public Share(String email, String access, String acceptUrl) {
        mName = email;
        mEmail = email;
        mAccess = access;
        mAcceptUrl = acceptUrl;
    }

    private Share() {
    }

    private Share(Parcel in) {
        this.mName = in.readString();
        this.mEmail = in.readString();
        this.mAccess = in.readString();
        this.mShoppinglistId = in.readString();
        this.mAccepted = in.readByte() != 0;
        this.mAcceptUrl = in.readString();
        this.mSyncState = in.readInt();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Share}
     * @return A {@link List} of POJO
     */
    public static List<Share> fromJSON(JSONArray array) {
        ArrayList<Share> list = new ArrayList<Share>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Share.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Share}
     * @return A {@link Share}, or {@code null} if {@code object} is {@code null}
     */
    public static Share fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Share s = new Share();

        // Consider changing structure of Share to match API
        // But the 'user'-key indicates it's a User-object which it isn't
        SgnJson u = new SgnJson(o.getJSONObject(SgnJson.USER));
        s.setEmail(u.getEmail());
        s.setName(u.getName());

        s.setAccess(o.getAccess());
        s.setAccepted(o.getAccepted());
        if (o.has(SgnJson.ACCEPT_URL)) {
            s.setAcceptUrl(o.getAcceptUrl());
        }

        o.getStats().log(TAG);

        return s;
    }

    public JSONObject toJSON() {
        SgnJson u = new SgnJson()
                .setEmail(getEmail())
                .setName(getName());

        SgnJson o = new SgnJson()
                .put(SgnJson.USER, u.toJSON())
                .setAccepted(getAccepted())
                .setAccess(getAccess());

        if (getAcceptUrl() != null) {
            o.setAcceptUrl(getAcceptUrl());
        }

        return o.toJSON();
    }

    /**
     * Get the e-mail of this share
     * @return An e-mail, or {@code null}
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Set the e-mail of this share
     * @param email An e-mail, not {@code null}
     * @return This object
     */
    public Share setEmail(String email) {
        if (email != null) {
            mEmail = email;
        }
        return this;
    }

    /**
     * Get the name of this share
     * @return A name, or {@code null}
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the name of this share
     * @param name A name, not {@code null}
     * @return This object
     */
    public Share setName(String name) {
        if (name != null) {
            mName = name;
        }
        return this;
    }

    /**
     * Get the access parmission for this share
     * <p>Current valid access permissions are {@link Share#ACCESS_OWNER},
     * {@link Share#ACCESS_READONLY}, and {@link Share#ACCESS_READWRITE}.
     * Other strings will be ignored by the API.</p>
     * @return An access permission
     */
    public String getAccess() {
        return mAccess;
    }

    /**
     * Set the access level for this share.
     * <p>Current valid access permissions are {@link Share#ACCESS_OWNER},
     * {@link Share#ACCESS_READONLY}, and {@link Share#ACCESS_READWRITE}.
     * Other strings will be ignored by the API.</p>
     * @param permission An permission for this share
     * @return This object
     */
    public Share setAccess(String permission) {
        if (permission != null) {
            mAccess = permission;
        }
        return this;
    }

    public boolean isAccessOwner() {
        return ACCESS_OWNER.equals(mAccess);
    }

    public boolean isAccessReadWrite() {
        return ACCESS_READWRITE.equals(mAccess);
    }

    public boolean isAccessReadOnly() {
        return ACCESS_READONLY.equals(mAccess);
    }

    public boolean haveAcceptedInvite() {
        return !(mEmail == null || mName == null) && !mEmail.equals(mName);
    }

    /**
     * Whether or not the share/user have accepted the invitation to the
     * {@link Shoppinglist}, and are currently receiving updates about the
     * {@link Shoppinglist}
     * @return {@code true} if user have accepted, else {@code false}
     */
    public boolean getAccepted() {
        return mAccepted;
    }

    /**
     * Whether or not the share/user have accepted the invitation.
     * <p>This shouldn't be handled by the app, or SDK, as this is something
     * being determined by the API</p>
     * @param accepted {@code true} if user have accepted the invitation, else {@code false}
     * @return This object
     */
    public Share setAccepted(boolean accepted) {
        mAccepted = accepted;
        return this;
    }

    /**
     * Get the absolute URL to redirect the user to when he/she accepts to
     * share the {@link Shoppinglist}.
     * @return An URL
     */
    public String getAcceptUrl() {
        return mAcceptUrl;
    }

    /**
     * Set the absolute URL to redirect the user to when he/she accepts to
     * share the {@link Shoppinglist}.
     * @param url The URL
     * @return This object
     */
    public Share setAcceptUrl(String url) {
        mAcceptUrl = url;
        return this;
    }

    /**
     * The {@link Shoppinglist#getId() id} of the shopping list associated with
     * this share.
     * @return The shopping list id, or {@code null}
     */
    public String getShoppinglistId() {
        return mShoppinglistId;
    }

    /**
     * Set the {@link Shoppinglist#getId() id} if the associated shoppinglist
     * @param id An id of a {@link Shoppinglist}
     * @return This object
     */
    public Share setShoppinglistId(String id) {
        mShoppinglistId = id;
        return this;
    }

    public int getState() {
        return mSyncState;
    }

    public Share setState(int state) {
        mSyncState = state;
        return this;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public int compareTo(Share another) {
        return 0;
    }

    public boolean same(Object obj) {
        return compare(obj, false);
    }

    public boolean compare(Object obj, boolean syncState) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Share other = (Share) obj;
        if (mAcceptUrl == null) {
            if (other.mAcceptUrl != null)
                return false;
        } else if (!mAcceptUrl.equals(other.mAcceptUrl))
            return false;
        if (mAccepted != other.mAccepted)
            return false;
        if (mAccess == null) {
            if (other.mAccess != null)
                return false;
        } else if (!mAccess.equals(other.mAccess))
            return false;
        if (mEmail == null) {
            if (other.mEmail != null)
                return false;
        } else if (!mEmail.equals(other.mEmail))
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        if (mShoppinglistId == null) {
            if (other.mShoppinglistId != null)
                return false;
        } else if (!mShoppinglistId.equals(other.mShoppinglistId))
            return false;

        if (syncState) {
            if (mSyncState != other.mSyncState)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mAcceptUrl == null) ? 0 : mAcceptUrl.hashCode());
        result = prime * result + (mAccepted ? 1231 : 1237);
        result = prime * result + ((mAccess == null) ? 0 : mAccess.hashCode());
        result = prime * result + ((mEmail == null) ? 0 : mEmail.hashCode());
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result
                + ((mShoppinglistId == null) ? 0 : mShoppinglistId.hashCode());
        result = prime * result + mSyncState;
        return result;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return compare(obj, true);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mEmail);
        dest.writeString(this.mAccess);
        dest.writeString(this.mShoppinglistId);
        dest.writeByte(mAccepted ? (byte) 1 : (byte) 0);
        dest.writeString(this.mAcceptUrl);
        dest.writeInt(this.mSyncState);
    }

}

