package com.vathsav.movies;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MovieDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getIntent().getStringExtra("Title"));
        }

        Picasso.with(getApplicationContext())
                .load("http://image.tmdb.org/t/p/w342/" + getIntent().getStringExtra("Poster Path"))
                .into((ImageView) findViewById(R.id.details_image_view));
        ((TextView) findViewById(R.id.details_release_date)).setText(getIntent().getStringExtra("Release Date"));
        ((TextView) findViewById(R.id.details_synopsis)).setText(getIntent().getStringExtra("Synopsis"));
        ((TextView) findViewById(R.id.details_title)).setText(getIntent().getStringExtra("Title"));
        ((RatingBar) findViewById(R.id.details_rating)).setRating(Float.parseFloat(getIntent().getStringExtra("Rating"))/2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom);
        super.onBackPressed();
    }
}
