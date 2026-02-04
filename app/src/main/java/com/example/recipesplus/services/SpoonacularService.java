package com.example.recipesplus.services;

import android.content.Context;
import android.util.Log;

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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SpoonacularService {

    public interface RecipeCallback {
        void onSuccess(List<OnlineRecipe> recipes);
        void onError(String message);
    }

    public interface IngredientsCallback {
        void onSuccess(List<String> ingredients);
        void onError(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void search(Context context, String query, RecipeCallback callback) {
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

    public void searchByIngredients(Context context, List<String> ingredients, RecipeCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String apiKey = context.getString(R.string.spoonacular_api_key);
                String ingredientsStr = URLEncoder.encode(ingredients.stream().map(String::trim).collect(Collectors.joining(",")), "UTF-8");

                // --- FIX START ---

                // STEP 1: Find recipe IDs by ingredients
                String findUrlStr = "https://api.spoonacular.com/recipes/findByIngredients"
                        + "?apiKey=" + apiKey
                        + "&ingredients=" + ingredientsStr
                        + "&number=10"; // Limit to 10 results to be kind to the API

                URL findUrl = new URL(findUrlStr);
                conn = (HttpURLConnection) findUrl.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    callback.onError("HTTP " + conn.getResponseCode());
                    return;
                }

                BufferedReader findReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder findSb = new StringBuilder();
                String findLine;
                while ((findLine = findReader.readLine()) != null) {
                    findSb.append(findLine);
                }
                findReader.close();
                conn.disconnect();

                JSONArray findResults = new JSONArray(findSb.toString());
                if (findResults.length() == 0) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Collect the IDs of the found recipes
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < findResults.length(); i++) {
                    ids.add(findResults.getJSONObject(i).getString("id"));
                }

                // STEP 2: Get the full details for the found recipe IDs
                String idsStr = String.join(",", ids);
                String bulkUrlStr = "https://api.spoonacular.com/recipes/informationBulk"
                        + "?apiKey=" + apiKey
                        + "&ids=" + URLEncoder.encode(idsStr, "UTF-8");

                URL bulkUrl = new URL(bulkUrlStr);
                conn = (HttpURLConnection) bulkUrl.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    callback.onError("HTTP " + conn.getResponseCode());
                    return;
                }

                BufferedReader bulkReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder bulkSb = new StringBuilder();
                String bulkLine;
                while ((bulkLine = bulkReader.readLine()) != null) {
                    bulkSb.append(bulkLine);
                }
                bulkReader.close();

                JSONArray bulkResults = new JSONArray(bulkSb.toString());
                List<OnlineRecipe> list = new ArrayList<>();
                for (int i = 0; i < bulkResults.length(); i++) {
                    JSONObject r = bulkResults.getJSONObject(i);
                    String title = r.optString("title", "");
                    String summary = stripHtml(r.optString("summary", ""));
                    String instructions = stripHtml(r.optString("instructions", ""));
                    String ingredientsList = extractIngredients(r.optJSONArray("extendedIngredients"));
                    list.add(new OnlineRecipe(title, summary, instructions, ingredientsList));
                }
                callback.onSuccess(list);
                // --- FIX END ---

            } catch (Exception e) {
                Log.e("SpoonacularService", "Error in searchByIngredients", e);
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public void getPopularIngredients(Context context, IngredientsCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String apiKey = context.getString(R.string.spoonacular_api_key);
                String urlStr = "https://api.spoonacular.com/recipes/complexSearch"
                        + "?apiKey=" + apiKey
                        + "&number=100"
                        + "&sort=popularity"
                        + "&addRecipeInformation=true"
                        + "&fillIngredients=true";

                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    callback.onError("HTTP " + conn.getResponseCode());
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray results = root.getJSONArray("results");
                HashSet<String> ingredients = new HashSet<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject recipe = results.getJSONObject(i);
                    JSONArray usedIngredients = recipe.optJSONArray("extendedIngredients");
                    if (usedIngredients != null) {
                        for (int j = 0; j < usedIngredients.length(); j++) {
                            JSONObject ingredient = usedIngredients.getJSONObject(j);
                            String name = ingredient.optString("name");
                            if (name != null && !name.trim().isEmpty()) {
                                ingredients.add(name.trim().toLowerCase());
                            }
                        }
                    }
                }
                callback.onSuccess(new ArrayList<>(ingredients));

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
        return input.replaceAll("<[^>]*>", "").trim();
    }
}