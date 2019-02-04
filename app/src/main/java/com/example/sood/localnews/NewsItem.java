package com.example.sood.localnews;

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sood on 1/30/19.
 */

public class NewsItem {

    private String title;
    private String source;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private Date date;
    private Bitmap icon;
    private String url;
    private String urlToIcon = "https://media1.s-nbcnews.com/j/newscms/2019_05/2734146/190129-cold-weather-ac-929p_a786534c16a83cc04ae303de51bd693a.nbcnews-fp-1200-630.jpg";

    public NewsItem(String title, String source, String date, String url, String urlToIcon) {

        if(title != null)   this.title = title;
        if(source != null)  this.source = source;
        if(url != null)     this.url = url;

        try {
            this.date = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            this.date = null;
        }

        if(urlToIcon != null && !urlToIcon.isEmpty())   this.urlToIcon = urlToIcon;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {
        return (SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
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
