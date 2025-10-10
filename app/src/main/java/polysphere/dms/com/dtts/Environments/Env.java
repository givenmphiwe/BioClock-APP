package polysphere.dms.com.dtts.Environments;

import polysphere.dms.com.dtts.BuildConfig;

public class Env {
    public static String apiUrl() { return BuildConfig.API_URL; }
    public static String baseUrl() { return BuildConfig.BASE_URL; }
    public static String tokenKey() { return BuildConfig.TOKEN_KEY; }
    public static boolean offlineEnabled() { return BuildConfig.OFFLINE_ENABLED; }
    public static boolean forceOffline() { return BuildConfig.FORCE_OFFLINE; }
    public static String offlineToken() { return BuildConfig.OFFLINE_TOKEN; }
}