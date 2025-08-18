package com.example.subforest.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private static final String PREF = "auth_pref";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_UID   = "user_id";

    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        this.sp = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String token) { sp.edit().putString(KEY_TOKEN, token==null?"":token).apply(); }
    public String getAccessToken() { return sp.getString(KEY_TOKEN, ""); }

    public void saveUserId(long id) { sp.edit().putLong(KEY_UID, id).apply(); }
    public long getUserIdOr(long def) { return sp.getLong(KEY_UID, def); }

    public void clear() { sp.edit().remove(KEY_TOKEN).remove(KEY_UID).apply(); }
}