package com.example.sood.localnews;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private final int PERMISSIONS_REQUEST_COARSE_LOCATION = 1;

    protected static Location mLastLocation;
    private static LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new android.os.Handler());
    private static String currentRegion;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    private Toolbar topToolbar;
    private EditText keywordEditText;
    private Button keywordButton;

    private ListView newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityHelper.initialize(this);

        if (savedInstanceState != null) {
            currentRegion = savedInstanceState.getString("currentRegion");

            if(currentRegion != null) {
                Log.d(TAG, "Saved region: "+currentRegion);
                TextView regionTextView = findViewById(R.id.current_region_text_view);
                regionTextView.setText("Region: "+currentRegion);
            }
        }

        while(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_COARSE_LOCATION);

        }

        topToolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);

        newsList = findViewById(R.id.news_list_view);
        keywordEditText = findViewById(R.id.search_keyword_edit_text);

        keywordButton = findViewById(R.id.search_keyword_button);
        keywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordEditText.getText().toString();
                NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, keyword);
                Toast.makeText(MainActivity.this, "Fetching search results", Toast.LENGTH_LONG).show();
            }
        });

        startNotificationJob();

        TextView newsApiTextView = findViewById(R.id.news_api_text_view);
        newsApiTextView.setClickable(true);
        newsApiTextView.setMovementMethod(LinkMovementMethod.getInstance());
        newsApiTextView.setText(android.text.Html.fromHtml("<a href='https://newsapi.org/'>Powered by News API</a>"));


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "Callback locationResult is null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    Log.d(TAG, "Callback locationResult is being processed");
                    reverseGeocode(location);
                }
            }
        };

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            reverseGeocode(location);
                        }
                        else
                            createLocationRequest();
                            startLocationUpdates();
                            Log.d(TAG, "Location null");
                    }
                });

        Log.d(TAG, "buildClient called");

        if(currentRegion == null)
            NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, this, null);

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NewsItem selectedItem = (NewsItem) newsList.getItemAtPosition(position);

                /*
                * Using an implicit intent instead of a webview to avoid redundancy
                * and keep app size small
                * */
                Uri webpage = Uri.parse(selectedItem.getUrl());
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    startActivity(webIntent);
                }

            }
        });

        newsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                NewsItem selectedItem = (NewsItem) newsList.getItemAtPosition(position);

                Intent urlShareIntent = new Intent();
                urlShareIntent.setAction(Intent.ACTION_SEND);
                urlShareIntent.putExtra(Intent.EXTRA_TEXT, selectedItem.getUrl());
                urlShareIntent.setType("text/plain");
                startActivity(Intent.createChooser(urlShareIntent, "Share article with:"));

                return true;
            }
        });
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(1)
                .setInterval(10 * 1000)
                .setFastestInterval(5 * 1000);
    }

    private void startNotificationJob() {

        ComponentName componentName = new ComponentName(this, TrendingNewsBackgroundService.class);
        JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                .setPeriodic(1000*60*60*4) //Update every 4 hours
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);

        if(resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "###########################Notification job scheduled");
        } else {
            Log.d(TAG, "###########################Notification job not scheduled");
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_local_news) {
            if(currentRegion != null)
                NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, currentRegion);
            startLocationUpdates();
            Toast.makeText(MainActivity.this, "Fetching local news", Toast.LENGTH_LONG).show();
            return true;
        }
        else if(id == R.id.action_top_news) {
            NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, null);
            Toast.makeText(MainActivity.this, "Fetching top headlines", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void startIntentService() {

        Log.d(TAG, "sendingIntent for background calculation");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    private void reverseGeocode(Location location) {

        Log.d(TAG, "reverseGeocoding");
        mLastLocation = location;
        String msg = "Latitude: "+String.valueOf(mLastLocation.getLatitude() + " Longitude: " + String.valueOf(mLastLocation.getLongitude()));
        Log.d(TAG, msg);

        if (!Geocoder.isPresent()) {
            Log.d(TAG, "no_geocoder_available");
            return;
        }

        //In callback of requestLocationUpdate
        // Start service and update UI to reflect new location
        startIntentService();
    }

    class AddressResultReceiver extends ResultReceiver {
        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            Log.d(TAG, "geocoder result received");
            if (resultData == null) {
                Log.d(TAG, "geocoder result data is null");
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String city = resultData.getString(Constants.RESULT_DATA_KEY);

            TextView regionTextView = findViewById(R.id.current_region_text_view);
            if (resultCode == Constants.SUCCESS_RESULT && city != null) {

                Log.d(TAG, "geocoder result SUCCESS");
                regionTextView.setText("Region: "+city);
                currentRegion = city;
                NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, city);
            }
            else {

                Log.d(TAG, "geocoder result FAILURE");
                regionTextView.setText("Region Error");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(currentRegion != null) {
            NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, currentRegion);
        }
        createLocationRequest();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)   return;

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void onSaveInstanceState (Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putString("currentRegion", currentRegion);

    }
}
