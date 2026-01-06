package com.example.recipesplus.services;

import android.content.Context;

import com.example.recipesplus.R;
import com.example.recipesplus.model.Recipe;

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
        void onSuccess(List<Recipe> recipes);
        void onError(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void search(Context context, String query, Callback callback) {
        executor.execute(() -> {
            try {
                String apiKey = context.getString(R.string.spoonacular_api_key);
                String q = URLEncoder.encode(query, "UTF-8");

                String urlStr =
                        "https://api.spoonacular.com/recipes/complexSearch"
                                + "?apiKey=" + apiKey
                                + "&query=" + q
                                + "&number=20"
                                + "&addRecipeInformation=true";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

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

                List<Recipe> list = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject r = results.getJSONObject(i);

                    String title = r.optString("title", "");
                    String summary = r.optString("summary", "");
                    // instructions sometimes exist as "instructions" string
                    String instructions = r.optString("instructions", "");

                    // ingredients not returned by default in this endpoint; keep empty for now
                    Recipe recipe = new Recipe(title, "", instructions.isEmpty() ? summary : instructions);
                    list.add(recipe);
                }

                callback.onSuccess(list);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
}
