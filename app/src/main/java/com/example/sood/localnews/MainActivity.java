package com.example.sood.localnews;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityHelper.initialize(this);

        TextView newsApiTextView = findViewById(R.id.news_api_text_view);
        newsApiTextView.setClickable(true);
        newsApiTextView.setMovementMethod(LinkMovementMethod.getInstance());
        newsApiTextView.setText(android.text.Html.fromHtml("<a href='https://newsapi.org/'> Powered by News API </a>"));

        final ListView newsList = findViewById(R.id.news_list_view);

        NewsApiRequest.setNewsArticlesList(getApplicationContext(), newsList, this);

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
}
