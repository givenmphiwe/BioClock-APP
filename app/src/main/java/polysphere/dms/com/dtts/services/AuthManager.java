package polysphere.dms.com.dtts.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import polysphere.dms.com.dtts.Environments.Env;


public class AuthManager {
    private static final String PREFS = "app_prefs";
    private final Context ctx;

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

    public void login(String industryNumber, String password, boolean biometric, final Callback callback) {
        final String indNum = industryNumber;
        final String pwd = password;
        final boolean bio = biometric;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("industry_number", indNum);
                    payload.put("password", pwd);
                    payload.put("type", bio ? "biometric_auth" : "password_auth");
                    payload.put("grant_type", "client_credentials");


                    String resp = ApiClient.post("login", payload.toString(), null);
                    JSONObject obj = new JSONObject(resp);
                    String token = obj.optString("access_token");
                    if (token == null || token.isEmpty()) {
                        callback.onError("No token in response");
                        return;
                    }
                    saveToken(token);
                    callback.onSuccess(obj);
                } catch (Exception e) {
                    Log.e("AuthManager", "login error", e);
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public interface Callback {
        void onSuccess(JSONObject userInfo);
        void onError(String error);
    }
}
