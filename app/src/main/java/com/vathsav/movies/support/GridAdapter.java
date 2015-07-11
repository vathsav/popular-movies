package com.vathsav.movies.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vathsav.movies.R;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private ArrayList<GridItem> gridArray = new ArrayList<GridItem>();
    private Context context;

    public GridAdapter(Context context, ArrayList<GridItem> array) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.gridArray = array;
    }

    @Override
    public int getCount() {
        return gridArray.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return gridArray.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.grid_item, null);
        }

        final ImageView image = (ImageView) convertView.findViewById(R.id.grid_item_image);
        final TextView text = (TextView) convertView.findViewById(R.id.grid_item_text);

        Picasso.with(context)
                .load("http://image.tmdb.org/t/p/w342/" + gridArray
                        .get(position)
                        .getImage())
                .into(image);

        text.setText(gridArray.get(position).getTitle());

        final int pos = position;

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add all content to a bundle that can be retrieved by the other activity
                Intent callDetailsActivity = new Intent("com.vathsav.movies.DETAILS");
                callDetailsActivity.putExtra("Poster Path", gridArray.get(pos).getImage());
                callDetailsActivity.putExtra("Release Date", gridArray.get(pos).getReleaseDate());
                callDetailsActivity.putExtra("Synopsis", gridArray.get(pos).getSynopsis());
                callDetailsActivity.putExtra("Rating", gridArray.get(pos).getUserRating());
                callDetailsActivity.putExtra("Title", gridArray.get(pos).getTitle());
                context.startActivity(callDetailsActivity);
                ((Activity) context).overridePendingTransition(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom);
            }
        });

        return convertView;
    }
}
