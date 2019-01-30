package com.example.sood.localnews;

import android.graphics.Bitmap;

/**
 * Created by sood on 1/30/19.
 */

public class NewsItem {

    private String title;
    private String source;
    private String date;
    private Bitmap icon;
    private String url;
    private String urlToIcon;

    public NewsItem(String title, String source, String date, String url) {

        this.title = title;
        this.source = source;
        this.date = date;
        this.url = url;

        //TODO: Decide how and put value in image
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {
        return date;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }
}
