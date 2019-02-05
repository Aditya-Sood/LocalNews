package com.example.sood.localnews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sood on 1/30/19.
 */

/**
 * Class denoting news articles as entities for the news list
 * */

public class NewsItem {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    private String title;
    private String source;
    private Date date;
    private String url;
    private String urlToIcon;

    public NewsItem(String title, String source, String date, String url, String urlToIcon) {

        if(title != null)   this.title = title;
        else                this.title = "Title unavailable";

        if(source != null)  this.source = source;
        else                this.source = "Source unavailable";

        if(url != null)     this.url = url;
        else                this.url = "Url unavailable";

        try {
            this.date = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            this.date = null;
        }

        if(urlToIcon != null && !urlToIcon.isEmpty())   this.urlToIcon = urlToIcon;
        else                                            this.urlToIcon = null;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {

        if(date != null) {
            return (SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
        } else {
            return "Date unavailable";
        }

    }

    public String getUrl() {
        return url;
    }

    public String getUrlToIcon() {
        return  urlToIcon;
    }
}
