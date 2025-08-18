package com.example.subforest.network;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public class TokenStore {
    private static final String PREF = "auth_pref";
    private static final String K_TOKEN = "access_token";
    private static final String K_UID   = "user_id";

    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, long userId) {
        sp.edit().putString(K_TOKEN, token).putLong(K_UID, userId).apply();
    }

    public void saveAccessToken(String token) { sp.edit().putString(K_TOKEN, token).apply(); }
    @Nullable public String getAccessToken()  { return sp.getString(K_TOKEN, null); }
    public long getUserIdOr(long fallback)    { return sp.getLong(K_UID, fallback); }
    public void saveUserId(long userId)       { sp.edit().putLong(K_UID, userId).apply(); }

    public void clear() { sp.edit().clear().apply(); }
}