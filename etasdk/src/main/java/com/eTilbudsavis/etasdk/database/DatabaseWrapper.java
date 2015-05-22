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
package com.eTilbudsavis.etasdk.database;

import android.content.Context;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.utils.ListUtils;
import com.eTilbudsavis.etasdk.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseWrapper {
	
	public static final String TAG = Constants.getTag(DatabaseWrapper.class);

	private static DatabaseWrapper mWrapper;
    private DataSource mDataSource;

    private DatabaseWrapper(Context c) {
        mDataSource = new DataSource(c);
    }

    public static DatabaseWrapper getInstance(Context c) {
        if (mWrapper == null) {
            mWrapper = new DatabaseWrapper(c);
        }
        return mWrapper;
    }

    public static DatabaseWrapper getInstance(Eta eta) {
        return getInstance(eta.getContext());
	}

    public void open() {
        mDataSource.open();
    }

    public void close() {
        mDataSource.close();
    }

    private boolean successId(long id) {
        return id > -1;
    }

    private boolean successCount(int count) {
        return count > 0;
    }

    /**
	 * Clears the whole DB. This cannot be undone.
	 */
	public int clear() {
        return mDataSource.clear();
	}

	public int clear(int userId) {
        return mDataSource.clear(userId);
	}

	/**
	 * Insert new shopping list into DB
	 * @param sl to insert
     * @return true, if operation succeeded, else false
	 */
	public boolean insertList(Shoppinglist sl, User user) {
        long id = mDataSource.insertList(sl, String.valueOf(user.getUserId()));
		if (id > -1) {
			cleanShares(sl, user);
		}
        return successId(id);
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id, User user) {
        Shoppinglist sl = mDataSource.getList(id, String.valueOf(user.getUserId()));
		if (sl != null) {
			sl.putShares(getShares(sl, user, false));
            /* Remove the list, if the user isn't in the shares.
            This happens when the user, have removed him/her self from shares,
            or deletes a list, and the action haven't been synced to the API yet */
            if (!sl.getShares().containsKey(user.getEmail())) {
                return null;
            }
		}
		return sl;
	}
	
	/**
	 * Get all shoppinglists, deleted lists are not included
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getLists(User user) {
		return getLists(user, false);
    }

    /**
     *
     * @param user The user to fetch lists from
	 * @param includeDeleted Whether to include deleted items
	 * @return A list of Shoppinglist
	 */
	public List<Shoppinglist> getLists(User user, boolean includeDeleted) {
		List<Shoppinglist> lists = mDataSource.getLists(String.valueOf(user.getUserId()), includeDeleted);
		Iterator<Shoppinglist> it = lists.iterator();
		while (it.hasNext()) {
			Shoppinglist sl = it.next();
			sl.putShares(getShares(sl, user, includeDeleted));
            /* Remove the list, if the user isn't in the shares.
            This happens when the user, have removed him/her self from shares,
            or deletes a list, and the action haven't been synced to the API yet */
			if (!sl.getShares().containsKey(user.getEmail())) {
				it.remove();
			}
		}
        // they should be sorted from the DB
//		Collections.sort(lists);
		return lists;
	}

    /**
     * Delete a (all) shoppinglist where both the Shoppinglist.id, and user.id matches
     * @param sl A shoppinglist
     * @param user A user
     * @return true, if operation succeeded, else false
     */
	public boolean deleteList(Shoppinglist sl, User user) {
		return deleteList(sl.getId(), user);
	}

    /**
     * Delete a (all) shoppinglist where both the Shoppinglist.id, and user.id matches
     * @param shoppinglistId A shoppinglist id
     * @param user A user id
     * @return true, if operation succeeded, else false
     */
    public boolean deleteList(String shoppinglistId, User user) {
        return deleteList(shoppinglistId, String.valueOf(user.getUserId()));
    }

    /**
     * Delete a list, from the db
     * @param shoppinglistId to delete
     * @return true, if operation succeeded, else false
     */
    public boolean deleteList(String shoppinglistId, String userId) {
        // TODO: Do we need to remove shares?
        int count = mDataSource.deleteList(shoppinglistId, userId);
        return successCount(count);
    }

    /**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param sl that have been edited
     * @return true, if operation succeeded, else false
	 */
	public boolean editList(Shoppinglist sl, User user) {
        long id = mDataSource.insertList(sl, String.valueOf(user.getUserId()));
        return successId(id);
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
     * @return true, if operation succeeded, else false
	 */
	public boolean insertItem(ShoppinglistItem sli, User user) {
        long id = mDataSource.insertItem(sli, String.valueOf(user.getUserId()));
        return successId(id);
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return number of affected rows
	 */
	public int insertItems(List<ShoppinglistItem> items, User user) {
        return mDataSource.insertItem(items, String.valueOf(user.getUserId()));
	}

	/**
	 * Get a shoppinglistitem from the db
	 * @param itemId to get from db
	 * @return A shoppinglistitem or null if no match is found
	 */
	public ShoppinglistItem getItem(String itemId, User user) {
        return mDataSource.getItem(itemId, String.valueOf(user.getUserId()));
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user) {
        return getItems(sl.getId(), user, false);
	}

	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user, boolean includeDeleted) {
		return getItems(sl.getId(), user, includeDeleted);
	}
	
	/**
	 * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
	 * @param shoppinglistId from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(String shoppinglistId, User user, boolean includeDeleted) {
        return mDataSource.getItems(shoppinglistId, String.valueOf(user.getUserId()), includeDeleted);
	}

	public ShoppinglistItem getFirstItem(String shoppinglistId, User user) {
		return getItemPrevious(shoppinglistId, ListUtils.FIRST_ITEM, user);
	}
	
	public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, User user) {
        return mDataSource.getItemPrevious(shoppinglistId, previousId, String.valueOf(user.getUserId()));
	}

	public Shoppinglist getFirstList(User user) {
		return getListPrevious(ListUtils.FIRST_ITEM, user);
	}
	
	public Shoppinglist getListPrevious(String previousId, User user) {
        return mDataSource.getListPrevious(previousId, String.valueOf(user.getUserId()));
	}
	
	/**
	 * Deletes an {@link ShoppinglistItem} from db
	 * @param sli An item to delete
     * @return true, if operation succeeded, else false
	 */
	public boolean deleteItem(ShoppinglistItem sli, User user) {
        int count = mDataSource.deleteItem(sli.getId(), String.valueOf(user.getUserId()));
        return successCount(count);
	}

	/**
	 * Deletes all items, in a given state, from a {@link Shoppinglist}
	 * 
	 * <ul>
	 * 		<li>{@code true} - delete ticked items</li>
	 * 		<li>{@code false} - delete unticked items</li>
	 * 		<li>{@code null} - delete all items</li>
	 * </ul>
	 * 
	 * @param shoppinglistId to remove items from
	 * @param state that items must have to be removed
	 * @return number of affected rows
	 */
	public int deleteItems(String shoppinglistId, Boolean state, User user) {
        return mDataSource.deleteItems(shoppinglistId, state, String.valueOf(user.getUserId()));
	}

    /**
     * replaces an item in db
     * @param sli to insert
     * @return true, if operation succeeded, else false
     */
    public boolean editItem(ShoppinglistItem sli, User user) {
        long id = mDataSource.insertItem(sli, String.valueOf(user.getUserId()));
        return successId(id);
    }

    /**
     * replaces an item in db
     * @param list to insert
     * @return number of affected rows
     */
    public int editItem(List<ShoppinglistItem> list, User user) {
        long s = System.currentTimeMillis();
        int count = 0;
        for (ShoppinglistItem sli : list) {
            if (editItem(sli, user)) {
                count++;
            }
        }
        long f = System.currentTimeMillis() - s;
        EtaLog.d(TAG, String.format("Edited %s items in %s ms", list.size(), f));
        return count;
//        return mDataSource.insertItem(list, String.valueOf(user.getUserId()));
    }

    /**
	 * 
	 * @param sl A shoppinglist
	 * @param user A user
	 * @param includeDeleted Whether to include deleted shares
	 * @return A list of Share
	 */
	public List<Share> getShares(Shoppinglist sl, User user, boolean includeDeleted) {
        return mDataSource.getShares(sl.getId(), String.valueOf(user.getUserId()), includeDeleted);
	}

    public boolean insertShare(Share s, User user) {
        long id = mDataSource.insertShare(s, String.valueOf(user.getUserId()));
        return successId(id);
    }

    public int cleanShares(Shoppinglist sl, User user) {
		deleteShares(sl, user);
		int count = 0;
		for (Share s: sl.getShares().values()) {
            if (editShare(s, user) ) {
                count++;
            }
        }
		return count;
	}

	public boolean editShare(Share s, User user) {
		deleteShare(s, user);
		return insertShare(s, user);
	}
	
	public int deleteShare(Share s, User user) {
        return mDataSource.deleteShare(s, user);
	}

	public int deleteShares(Shoppinglist sl, User user) {
        return deleteShares(sl.getId(), String.valueOf(user.getUserId()));
	}

    public int deleteShares(String shoppinglistId, User user) {
        return deleteShares(shoppinglistId, String.valueOf(user.getUserId()));
    }

    public int deleteShares(String shoppinglistId, String userId) {
        return mDataSource.deleteShares(shoppinglistId, userId);
    }

    public void allowEditOrThrow(ShoppinglistItem sli, User user) {
        allowEditOrThrow(sli.getShoppinglistId(), user);
    }


    public void allowEditOrThrow(Shoppinglist sl, User user) {
        allowEditOrThrow(sl.getId(), user);
    }

    public void allowEditOrThrow(String shoppinglistId, User user) {
        Shoppinglist sl = getList(shoppinglistId, user);
        PermissionUtils.allowEditOrThrow(sl, user);
    }

    public void allowEditItemsOrThrow(List<ShoppinglistItem> items, User user) {
        HashSet<String> ids = new HashSet<>(items.size());
        for (ShoppinglistItem sli : items) {
            ids.add(sli.getShoppinglistId());
        }
        allowEditOrThrow(ids, user);
    }

    public void allowEditListOrThrow(List<Shoppinglist> lists, User user) {
        HashSet<String> ids = new HashSet<>(lists.size());
        for (Shoppinglist sl : lists) {
            ids.add(sl.getId());
        }
        allowEditOrThrow(ids, user);
    }

    public void allowEditOrThrow(Set<String> shoppinglistIds, User user) {
        HashMap<String, Shoppinglist> map = new HashMap<>(shoppinglistIds.size());
        for (String id : shoppinglistIds) {
            if (!map.containsKey(id)) {
                Shoppinglist sl = getList(id, user);
                map.put(sl.getId(), sl);
            }
        }
        for (Map.Entry<String, Shoppinglist> e : map.entrySet()) {
            PermissionUtils.allowEditOrThrow(e.getValue(), user);
        }
    }

}
