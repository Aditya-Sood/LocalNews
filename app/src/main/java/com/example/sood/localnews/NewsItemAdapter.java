package com.example.sood.localnews;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sood on 1/30/19.
 */

public class NewsItemAdapter extends ArrayAdapter<NewsItem> {

    /*
    * TODO:
    * 1. Respond to clicks
    * */

    public NewsItemAdapter(Activity context, ArrayList<NewsItem> newsItemList) {
        super(context, 0, newsItemList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;

        if(listItemView == null)
        {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        NewsItem currentNewsItem = getItem(position);

        TextView newsTitleTextView = listItemView.findViewById(R.id.news_item_title_text_view);
        newsTitleTextView.setText(currentNewsItem.getTitle());

        TextView newsSourceTextView = listItemView.findViewById(R.id.news_item_source_text_view);
        newsSourceTextView.setText(currentNewsItem.getSource());

        TextView newsDateTextView = listItemView.findViewById(R.id.news_item_date_text_view);
        newsDateTextView.setText(currentNewsItem.getDate());

        /*
        ImageView newsIconImageView = listItemView.findViewById(R.id.news_item_icon_image_view);
        newsIconImageView.setImageBitmap(currentNewsItem.getIcon());
        */

        return listItemView;
    }

}
