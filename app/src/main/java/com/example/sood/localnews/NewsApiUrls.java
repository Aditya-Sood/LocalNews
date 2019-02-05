package com.example.sood.localnews;

/**
 * Created by sood on 1/30/19.
 */

/**/

public class NewsApiUrls {

    private static final String API_KEY = "9a274165beeb47e4a7493e149d6ab586";
    private static final String LANGUAGE = "en";
    private static final String COUNTRY = "in";
    private static final String PAGE_SIZE = "20";

    private static final String PROTOCOL = "https";
    private static final String HOST = "newsapi.org";
    private static final String PATH = "v2";

    private static final String ENDPOINT_TOPHEADLINES = "top-headlines";
    private static final String ENDPOINT_EVERYTHING = "everything";

    private static final String BASE_URL_TOPHEADLINES = PROTOCOL+"://"+HOST+"/"+PATH+"/"+ENDPOINT_TOPHEADLINES+"?apiKey="+API_KEY+"&language="+LANGUAGE+"&pageSize="+PAGE_SIZE;
    private static final String BASE_URL_EVERYTHING = PROTOCOL+"://"+HOST+"/"+PATH+"/"+ENDPOINT_EVERYTHING+"?apiKey="+API_KEY+"&language="+LANGUAGE+"&pageSize="+PAGE_SIZE+"&q=";

    public static String getBaseUrlTopheadlines() {
        return  BASE_URL_TOPHEADLINES;
    }
    public static String getBaseUrlEverything(String keyword) { return BASE_URL_EVERYTHING+"\""+keyword+"\"";   }

}
