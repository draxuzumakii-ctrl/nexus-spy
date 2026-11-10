package com.nexus.spy;
import okhttp3.*;
import org.json.*;
import java.util.Base64;
public class GithubApi {
    static final String OWNER = "draxuzumakii-ctrl";
    static final String REPO  = "nexus-spy";
    static final String TOKEN = "ISI_TOKEN_DI_SINI";
    static final String BRANCH = "main";
    static final String BASE = "https://api.github.com/repos/" + OWNER + "/" + REPO;
    static final OkHttpClient client = new OkHttpClient();
    static final MediaType JSON = MediaType.get("application/json");
    public static JSONObject read(String path) {
        try {
            Request req = new Request.Builder().url(BASE + "/contents/" + path + "?ref=" + BRANCH)
                .header("Authorization", "token " + TOKEN)
                .header("Accept", "application/vnd.github+json").build();
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) return null;
            JSONObject obj = new JSONObject(res.body().string());
            byte[] decoded = Base64.getDecoder().decode(obj.getString("content").replaceAll("\\s",""));
            return new JSONObject(new String(decoded));
        } catch (Exception e) { return null; }
    }
    public static JSONArray readArray(String path) {
        try {
            Request req = new Request.Builder().url(BASE + "/contents/" + path + "?ref=" + BRANCH)
                .header("Authorization", "token " + TOKEN)
                .header("Accept", "application/vnd.github+json").build();
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) return new JSONArray();
            JSONObject obj = new JSONObject(res.body().string());
            byte[] decoded = Base64.getDecoder().decode(obj.getString("content").replaceAll("\\s",""));
            return new JSONArray(new String(decoded));
        } catch (Exception e) { return new JSONArray(); }
    }
    public static boolean write(String path, String jsonContent, String commitMsg) {
        try {
            String sha = null;
            Request getReq = new Request.Builder().url(BASE + "/contents/" + path + "?ref=" + BRANCH)
                .header("Authorization", "token " + TOKEN).build();
            Response getRes = client.newCall(getReq).execute();
            if (getRes.isSuccessful()) sha = new JSONObject(getRes.body().string()).optString("sha", null);
            JSONObject body = new JSONObject();
            body.put("message", commitMsg);
            body.put("content", Base64.getEncoder().encodeToString(jsonContent.getBytes()));
            body.put("branch", BRANCH);
            if (sha != null) body.put("sha", sha);
            Request putReq = new Request.Builder().url(BASE + "/contents/" + path)
                .header("Authorization", "token " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .put(RequestBody.create(body.toString(), JSON)).build();
            return client.newCall(putReq).execute().isSuccessful();
        } catch (Exception e) { return false; }
    }
}
