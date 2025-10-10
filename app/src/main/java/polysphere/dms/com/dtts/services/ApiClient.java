package polysphere.dms.com.dtts.services;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import polysphere.dms.com.dtts.Environments.Env;


public class ApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String post(String path, String jsonBody, String token) throws Exception {
        String url = Env.apiUrl();
        if (!url.endsWith("/")) url += "/";
        url += path;

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, jsonBody));

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Response response = client.newCall(builder.build()).execute();

        // ✅ Read the response body once and reuse it
        String body = response.body() != null ? response.body().string() : "";

        if (!response.isSuccessful()) {
            String msg = "HTTP " + response.code() + ": " + response.message() + " Body: " + body;
            response.close(); // still good practice
            throw new Exception(msg);
        }

        response.close();
        return body;
    }

}