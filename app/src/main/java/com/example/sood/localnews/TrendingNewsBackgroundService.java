package com.example.sood.localnews;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by sood on 2/3/19.
 */

public class TrendingNewsBackgroundService extends JobService {

    private static final String TAG = TrendingNewsBackgroundService.class.getSimpleName();
    boolean isWorking = false;
    boolean jobCancelled = false;

    // Called by the Android system when it's time to run the job
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started!");
        isWorking = true;
        // We need 'jobParameters' so we can call 'jobFinished'
        startWorkOnNewThread(jobParameters); // Services do NOT run on a separate thread

        return isWorking;
    }

    private void startWorkOnNewThread(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            public void run() {
                doWork(jobParameters);
            }
        }).start();
    }

    private void doWork(JobParameters jobParameters) {

        if (jobCancelled)
            return;

        displayTrendingNotification();

        Log.d(TAG, "Job finished!");
        isWorking = false;
        boolean needsReschedule = false;
        jobFinished(jobParameters, needsReschedule);
    }

    // Called if the job was cancelled before being finished
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(jobParameters, needsReschedule);
        return needsReschedule;
    }

    private void displayTrendingNotification() {

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

                            String notificationMessage = articlesArray.getJSONObject(0).getString("title");

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

}
