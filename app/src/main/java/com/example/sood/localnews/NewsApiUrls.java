package com.example.sood.localnews;

/**
 * Created by sood on 1/30/19.
 */

public class NewsApiUrls {

    private static final String API_KEY = "9a274165beeb47e4a7493e149d6ab586";
    private static final String LANGUAGE = "en";

    private static final String PROTOCOL = "https";
    private static final String HOST = "newsapi.org";
    private static final String PATH = "v2";
    private static final String ENDPOINT = "top-headlines";

    private static final String BASE_URL = PROTOCOL+"://"+HOST+"/"+PATH+"/"+ENDPOINT+"?apiKey="+API_KEY+"&language="+LANGUAGE;

    public static String getBaseUrl() {
        return  BASE_URL;
    }

    //public static final String
    //public static final String

}
