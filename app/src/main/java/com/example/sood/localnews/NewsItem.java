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
    private String urlToIcon = "https://media1.s-nbcnews.com/j/newscms/2019_05/2734146/190129-cold-weather-ac-929p_a786534c16a83cc04ae303de51bd693a.nbcnews-fp-1200-630.jpg";

    public NewsItem(String title, String source, String date, String url, String urlToIcon) {

        this.title = title;
        this.source = source;
        this.date = date;
        this.url = url;

        if(urlToIcon != null && !urlToIcon.isEmpty())
            this.urlToIcon = urlToIcon;
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

    public String getUrlToIcon() {
        return  urlToIcon;
    }
}
