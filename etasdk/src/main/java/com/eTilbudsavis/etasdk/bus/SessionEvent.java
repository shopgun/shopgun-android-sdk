package com.eTilbudsavis.etasdk.bus;

/**
 * Created by Danny Hvam - danny@etilbudsavis.dk on 13/05/15.
 */
public class SessionEvent {

    private int mOldUser = 0;
    private int mNewUser = 0;

    public SessionEvent(int oldUser, int newUser) {
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
    }

    public boolean isNewUser() {
        return mOldUser != mNewUser;
    }

    public int getOldUser() {
        return mOldUser;
    }

    public int getNewUser() {
        return mNewUser;
    }

}
