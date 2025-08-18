package com.example.subforest.network;

import android.content.Context;
import android.content.SharedPreferences;

public final class TokenStore {
    private static final String PREF = "auth_pref";
    private static final String KEY = "jwt";
    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) { sp.edit().putString(KEY, token).apply(); }
    public String getToken() { return sp.getString(KEY, null); }
    public void clear() { sp.edit().remove(KEY).apply(); }
}
