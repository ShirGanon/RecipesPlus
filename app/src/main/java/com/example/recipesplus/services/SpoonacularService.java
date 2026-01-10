package com.example.recipesplus.services;

import android.content.Context;

import com.example.recipesplus.R;
import com.example.recipesplus.model.OnlineRecipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpoonacularService {

    public interface Callback {
        void onSuccess(List<OnlineRecipe> recipes);
        void onError(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void search(Context context, String query, Callback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String apiKey = context.getString(R.string.spoonacular_api_key);
                String q = URLEncoder.encode(query, "UTF-8");

                String urlStr =
                        "https://api.spoonacular.com/recipes/complexSearch"
                                + "?apiKey=" + apiKey
                                + "&query=" + q
                                + "&number=20"
                                + "&addRecipeInformation=true"
                                + "&fillIngredients=true";

                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    callback.onError("HTTP " + code);
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray results = root.getJSONArray("results");

                List<OnlineRecipe> list = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject r = results.getJSONObject(i);

                    String title = r.optString("title", "");
                    String summary = stripHtml(r.optString("summary", ""));
                    String instructions = stripHtml(r.optString("instructions", ""));
                    String ingredients = extractIngredients(r.optJSONArray("extendedIngredients"));

                    list.add(new OnlineRecipe(title, summary, instructions, ingredients));
                }

                callback.onSuccess(list);

            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static String extractIngredients(JSONArray arr) {
        if (arr == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;

            String item = obj.optString("original", "");
            if (item.isEmpty()) continue;

            if (sb.length() > 0) sb.append("\n");
            sb.append(item);
        }
        return sb.toString();
    }

    private static String stripHtml(String input) {
        if (input == null) return "";
        // Spoonacular sometimes returns HTML tags in summary/instructions
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
