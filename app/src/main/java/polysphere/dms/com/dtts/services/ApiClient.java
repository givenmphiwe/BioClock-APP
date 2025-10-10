package polysphere.dms.com.dtts.services;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public final class ApiClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // OkHttp 3.12 (Android 4.1+) — works with Gradle 4.6
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    public static String baseUrl() {
        return polysphere.dms.com.dtts.BuildConfig.BASE_URL;
    }

    public static String post(String path, String json, Map<String, String> headers) throws IOException {
        // ensure exactly one slash between base and path
        String base = baseUrl();
        if (base.endsWith("/") && path.startsWith("/")) path = path.substring(1);
        else if (!base.endsWith("/") && !path.startsWith("/")) path = "/" + path;

        RequestBody body = RequestBody.create(JSON, json);
        Request.Builder b = new Request.Builder()
                .url(base + path)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8");

        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                b.header(e.getKey(), e.getValue());
            }
        }

        Response r = client.newCall(b.build()).execute();
        String resp = r.body() != null ? r.body().string() : "";
        if (!r.isSuccessful()) {
            // propagate server error so caller can show it
            throw new IOException("HTTP " + r.code() + " " + r.message() + " • " + resp);
        }
        return resp;
    }
}
