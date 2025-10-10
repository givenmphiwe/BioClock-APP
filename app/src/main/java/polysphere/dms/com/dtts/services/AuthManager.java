package polysphere.dms.com.dtts.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import polysphere.dms.com.dtts.Environments.Env;

public class AuthManager {
    private static final String PREFS = "app_prefs";
    private final Context ctx;
    private final Handler main = new Handler(Looper.getMainLooper());

    public AuthManager(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    public boolean isAuthenticated() {
        return getToken() != null;
    }

    public void saveToken(String token) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Env.tokenKey(), token).apply();
    }

    public String getToken() {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Env.tokenKey(), null);
    }

    public void logout() {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public void login(final String userNameOrEmail, final String password, final boolean biometric, final Callback callback) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("userNameOrEmail", userNameOrEmail);
                    payload.put("password", password);
                    // If your API expects this:
                    // payload.put("biometric", biometric);

                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json; charset=utf-8");

                    final String path = "/api/Auth/login"; // match server casing exactly
                    Log.d("AuthManager", "POST " + ApiClient.baseUrl() + path + " body=" + payload.toString());

                    String resp = ApiClient.post(path, payload.toString(), headers);
                    if (resp == null || resp.length() == 0) {
                        postError(callback, "Empty response");
                        return;
                    }

                    JSONObject obj = new JSONObject(resp);

                    // Try multiple token key styles
                    String token = obj.optString("access_token", "");
                    if (token.length() == 0) token = obj.optString("accessToken", "");
                    if (token.length() == 0) token = obj.optString("token", "");

                    if (token.length() == 0) {
                        Log.w("AuthManager", "No token in response: " + resp);
                        postError(callback, "No token in response");
                        return;
                    }

                    saveToken(token);
                    postSuccess(callback, obj);

                } catch (Exception e) {
                    Log.e("AuthManager", "login error", e);
                    postError(callback, e.getMessage());
                }
            }
        }).start();
    }

    private void postSuccess(final Callback cb, final JSONObject obj) {
        main.post(new Runnable() { @Override public void run() { cb.onSuccess(obj); } });
    }
    private void postError(final Callback cb, final String msg) {
        main.post(new Runnable() { @Override public void run() { cb.onError(msg); } });
    }

    public interface Callback {
        void onSuccess(JSONObject userInfo);
        void onError(String error);
    }
}
