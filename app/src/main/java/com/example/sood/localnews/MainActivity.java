package com.example.sood.localnews;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

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

        ArrayList<NewsItem> newsItemArrayList = NewsApiRequest.getNewsArticles(getApplicationContext());

/*
        for(int i = 0; i < 10; i++)
            newsItemArrayList.add(new NewsItem("Title "+i, "Source "+i, "DD-MM-YY", ""));
*/

        if(newsItemArrayList != null) {
            ListView newsList = findViewById(R.id.news_list_view);
            NewsItemAdapter adapter = new NewsItemAdapter(this, newsItemArrayList);
            newsList.setAdapter(adapter);
        }

    }
}
