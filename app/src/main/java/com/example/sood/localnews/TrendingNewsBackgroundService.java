package com.example.sood.localnews;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by sood on 2/3/19.
 */

public class TrendingNewsBackgroundService extends Service {

    private static int count = 1;
    private Handler mHandler;
    // default interval for syncing data
    public static final long DEFAULT_SYNC_INTERVAL = 1000;//60 * 60;

    private Runnable runnableService = new Runnable() {
        @Override
        public void run() {
            syncData();
            // Repeat this runnable code block again every ... min
            mHandler.postDelayed(runnableService, DEFAULT_SYNC_INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the Handler object
        mHandler = new Handler();
        // Execute a runnable task as soon as possible
        mHandler.post(runnableService);

        return START_STICKY;
    }

    private synchronized void syncData() {

        //Toast.makeText(getApplicationContext(), "Notification", Toast.LENGTH_SHORT).show();

        String baseUrl = NewsApiUrls.getBaseUrlTopheadlines();
        //String baseUrl = NewsApiUrls.getBaseUrlEverything(""+count);

        CacheRequest cacheRequest = new CacheRequest(0, baseUrl, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse networkResponse) {
                try {
                    final String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                    JSONObject response = new JSONObject(jsonString);

                    int totalResults = -1;

                    try {

                        if(response.getString("status") == "error") {

                            String msg = response.getString("code")+": "+response.getString("message");
                            throw new java.lang.RuntimeException(msg);
                        }

                        totalResults = response.getInt("totalResults");

                        JSONArray articlesArray = response.getJSONArray("articles");
                        int articleCount = articlesArray.length();

                        if(articleCount > 0) {
                            createNotificationChannel();

                            String notificationMessage = count + "\n" + articlesArray.getJSONObject(0).getString("title");
                            count++;

                            createNotification(notificationMessage);
                        }
                        else {
                            throw new java.lang.RuntimeException("Empty response");
                        }

                    }
                    catch (RuntimeException e) {
                        //Toast.makeText(applicationCtx, "Runtime Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    catch (JSONException e) {
                        //Toast.makeText(applicationCtx, "JSON Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                    //mTextView.setText(jsonObject.toString(5));
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText(error.toString());
                // TODO: Handle error
                //Toast.makeText(applicationCtx, "Network Response Error", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(cacheRequest);

        /*JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, baseUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //mTextView.setText("Response: " + response.toString());

                        //TODO: Parse JSON response and handle errors (status, no articles)
                        int totalResults = -1;

                        try {

                            if(response.getString("status") == "error") {

                                String msg = response.getString("code")+": "+response.getString("message");
                                throw new java.lang.RuntimeException(msg);
                            }

                            totalResults = response.getInt("totalResults");

                            JSONArray articlesArray = response.getJSONArray("articles");
                            int articleCount = articlesArray.length();

                            if(articleCount > 0) {
                                createNotificationChannel();

                                String notificationMessage = articlesArray.getJSONObject(0).getString("title");

                                createNotification(notificationMessage);
                            }
                            else {
                                throw new java.lang.RuntimeException("Empty response");
                            }

                            //Toast.makeText(applicationCtx, "Article Count: "+articleCount, Toast.LENGTH_SHORT).show();

                            *//*for(int i = 0; i < articleCount; i++) {

                                JSONObject article = articlesArray.getJSONObject(i);

                                String source = article.getJSONObject("source").getString("name");
                                String title = article.getString("title");
                                String date = article.getString("publishedAt");
                                String url = article.getString("url");
                                String urlToIcon = article.getString("urlToImage");

                                newsArticlesList.add(new NewsItem(title, source, date, url, urlToIcon));
                            }*//*


                        }
                        catch (RuntimeException e) {
                            //Toast.makeText(applicationCtx, "Runtime Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        catch (JSONException e) {
                            //Toast.makeText(applicationCtx, "JSON Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        //Toast.makeText(context, "Total Results: "+totalResults, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        //Toast.makeText(applicationCtx, "Network Response Error", Toast.LENGTH_SHORT).show();

                    }
                });

        // Access the RequestQueue through your singleton class.
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
*/
        // call your rest service here


    }

    private void createNotification(String notificationContent) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Trending")
                .setSmallIcon(R.drawable.ic_trending_up_black_24dp)
                .setContentTitle("Trending")
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent)) //Expandable notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Trending";
            String description = "Notifications for trending news";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel trendingChannel = new NotificationChannel("Trending", name, importance);
            trendingChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(trendingChannel);

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
