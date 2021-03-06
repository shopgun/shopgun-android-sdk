package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONArray;

import java.util.List;

/**
 * @deprecated No longer maintained
 */
@Deprecated
public class ListArrayListener extends JSONArrayListener<List<Shoppinglist>> {

    public ListArrayListener(SgnDatabase database, User user, List<Shoppinglist> local) {
        super(database, user, local);
    }

    @Override
    public void onComplete(JSONArray response, ShopGunError error) {
        if (response != null) {
            onSuccess(Shoppinglist.fromJSON(response));
        } else {
            onError(error);
        }
    }

    @Override
    public void onSuccess(List<Shoppinglist> response) {

    }

    @Override
    public void onError(ShopGunError error) {

    }

}