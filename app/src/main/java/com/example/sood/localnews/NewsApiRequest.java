package com.example.sood.localnews;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sood on 1/30/19.
 */

public class NewsApiRequest {

    /*
    * Make API request
    * Parse response
    * Return ArrayList of news items (articles)
    * */
    public static ArrayList<NewsItem> getNewsArticles(final Context context) {

        String baseUrl = NewsApiUrls.getBaseUrl();
        ArrayList<NewsItem> newsArticlesList = new ArrayList<NewsItem>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, baseUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //mTextView.setText("Response: " + response.toString());

                        //TODO: Parse JSON response and handle errors (status, no articles)
                        int totalResults = -1;

                        try {
                            totalResults = response.getInt("totalResults");
                        }
                        catch (JSONException e) {
                            Toast.makeText(context, "JSON Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(context, "Total Results: "+totalResults, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(context, "Network Response Error", Toast.LENGTH_SHORT).show();

                    }
                });

// Access the RequestQueue through your singleton class.
        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

        return newsArticlesList;
    }
}