package com.example.sood.localnews;
import android.app.Activity;
import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by sood on 1/30/19.
 */

public class NewsApiRequest {

    /*
    * Make API request
    * Parse response
    * Display list
    * */
    public static void setNewsArticlesList(final Context applicationCtx, final ListView newsList, final Activity listActivity, String keyword) {

        String baseUrl;

        if(keyword == null)
            baseUrl = NewsApiUrls.getBaseUrlTopheadlines();
        else
            baseUrl = NewsApiUrls.getBaseUrlEverything(keyword);

        final ArrayList<NewsItem> newsArticlesList = new ArrayList<>();

        CacheRequest cacheRequest = new CacheRequest(0, baseUrl, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse networkResponse) {
                try {
                    final String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                    JSONObject response = new JSONObject(jsonString);

                    try {

                        if(response.getString("status") == "error") {

                            String msg = response.getString("code")+": "+response.getString("message");
                            throw new java.lang.RuntimeException(msg);
                        }

                        newsArticlesList.clear();

                        JSONArray articlesArray = response.getJSONArray("articles");
                        int articleCount = articlesArray.length();

                        for(int i = 0; i < articleCount; i++) {

                            JSONObject article = articlesArray.getJSONObject(i);

                            String source = article.getJSONObject("source").getString("name");
                            String title = article.getString("title");
                            String date = article.getString("publishedAt");
                            String url = article.getString("url");
                            String urlToIcon = article.getString("urlToImage");

                            newsArticlesList.add(new NewsItem(title, source, date, url, urlToIcon));
                        }

                        if(newsArticlesList.size() > 0) {

                            NewsItemAdapter adapter = new NewsItemAdapter(listActivity, newsArticlesList);
                            newsList.setAdapter(adapter);
                        }
                        else {
                            throw new java.lang.RuntimeException("Empty response");
                        }

                    } catch (RuntimeException e) {
                        Toast.makeText(applicationCtx, "Runtime Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Toast.makeText(applicationCtx, "JSON Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(applicationCtx, "Network Response Error", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueueSingleton.getInstance(applicationCtx).addToRequestQueue(cacheRequest);
    }
}