package com.example.sood.localnews;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;


import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private final int PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new android.os.Handler());
    private String currentRegion;

    private Toolbar topToolbar;
    private EditText keywordEditText;
    private Button keywordButton;

    private ListView newsList;
    private boolean newsListSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topToolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);
        ActivityHelper.initialize(this);

        newsList = findViewById(R.id.news_list_view);
        keywordEditText = findViewById(R.id.search_keyword_edit_text);
        keywordButton = findViewById(R.id.search_keyword_button);
        keywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordEditText.getText().toString();
                NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, keyword);
            }
        });

        TextView newsApiTextView = findViewById(R.id.news_api_text_view);
        newsApiTextView.setClickable(true);
        newsApiTextView.setMovementMethod(LinkMovementMethod.getInstance());
        newsApiTextView.setText(android.text.Html.fromHtml("<a href='https://newsapi.org/'>Powered by News API</a>"));

        buildGoogleApiClient();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(60 * 60 * 1000)
                .setFastestInterval(30 * 60 * 1000);

        if(!newsListSet)
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

/*
        for(int i = 0; i < 10; i++)
            newsItemArrayList.add(new NewsItem("Title "+i, "Source "+i, "DD-MM-YY", ""));
*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_local_news) {
            NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, currentRegion);
            Toast.makeText(MainActivity.this, "Fetching local news", Toast.LENGTH_LONG).show();
            return true;
        }
        else if(id == R.id.action_top_news) {
            NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, null);
            Toast.makeText(MainActivity.this, "Fetching top news", Toast.LENGTH_LONG).show();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Toast.makeText(getApplicationContext(), "Permission Achieved", Toast.LENGTH_LONG).show();


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    //#######For connecting to Google API
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, "Location services connected.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_COARSE_LOCATION);

        }
        else
            Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_LONG).show();

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {

            Toast.makeText(getApplicationContext(), "Null Location Object", Toast.LENGTH_LONG).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            /*
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            */

        }
        else {
            reverseGeocode(location);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(getApplicationContext(), "Google API connection failed", Toast.LENGTH_LONG).show();

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    //#######Google API

    //#######For LocationListener
    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);

        reverseGeocode(location);
    }

    //#######For LocationListener

    private void reverseGeocode(Location location) {
        mLastLocation = location;
        String msg = "Latitude: "+String.valueOf(mLastLocation.getLatitude() + "\nLongitude: " + String.valueOf(mLastLocation.getLongitude()));
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

        if (!Geocoder.isPresent()) {
            Toast.makeText(MainActivity.this,
                    "no_geocoder_available",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //In callback of requestLocationUpdate
        // Start service and update UI to reflect new location
        startIntentService();
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
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
            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String city = resultData.getString(Constants.RESULT_DATA_KEY);
            if (city == null) {
                city = "";
            }

            TextView regionTextView = findViewById(R.id.current_region_text_view);
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //Toast.makeText(getApplicationContext(), "City: "+city, Toast.LENGTH_LONG).show();
                regionTextView.setText("Region: "+city);
                currentRegion = city;
                //NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, MainActivity.this, city);
                newsListSet = true;
            }
            else {
                //Toast.makeText(getApplicationContext(), "City Error", Toast.LENGTH_LONG).show();
                regionTextView.setText("Region Error");
            }
        }
    }
}
