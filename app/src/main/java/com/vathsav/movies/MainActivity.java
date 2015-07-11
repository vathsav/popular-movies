package com.vathsav.movies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.vathsav.movies.support.GridAdapter;
import com.vathsav.movies.support.GridItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity {

    // Variables for AsyncTask
    ProgressDialog progressDialog;
    ArrayList<GridItem> gridContentArray;
    String jsonResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null || !savedInstanceState.containsKey("JSON Response")) {
            gridContentArray = new ArrayList<GridItem>();
            if (isNetworkAvailable()) {
                new FetchData().execute("");
            } else {
                Toast.makeText(getApplicationContext(), "Unable to access the internet. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        } else {
            gridContentArray = savedInstanceState.getParcelableArrayList("JSON Response");
            GridAdapter gridAdapter = new GridAdapter(MainActivity.this, gridContentArray);
            ((GridView) findViewById(R.id.gridView)).setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rating) {
            new FetchData().execute("vote_average.desc");
            return true;
        } else if (id == R.id.action_popularity) {
            new FetchData().execute("popularity.desc");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String[] parseJSON(String jsonResponse) throws JSONException {
        final String movie_results = "results";
        final String movie_title = "original_title";
        final String movie_release_date = "release_date";
        final String movie_poster_path = "poster_path";
        final String movie_rating = "vote_average";
        final String movie_overview = "overview";

        JSONObject jsonMovieObject = new JSONObject(jsonResponse);
        JSONArray jsonArray = jsonMovieObject.getJSONArray(movie_results);

        int numberOfResults;
        if (jsonArray != null)
            numberOfResults = jsonArray.length();
        else
            numberOfResults = 0;

        gridContentArray = new ArrayList<GridItem>();

        for (int i = 0; i < numberOfResults; i++) {
            String title;
            String release_date;
            String poster_path;
            String rating;
            String overview;

            JSONObject movie = jsonArray.getJSONObject(i);
            title = String.valueOf(movie.getString(movie_title));
            release_date = String.valueOf(movie.getString(movie_release_date));
            poster_path = String.valueOf(movie.getString(movie_poster_path));
            rating = String.valueOf(movie.getString(movie_rating));
            overview = String.valueOf(movie.getString(movie_overview));

            gridContentArray.add(new GridItem(title, release_date, rating, poster_path, overview));
        }
        return new String[12];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("JSON Response", gridContentArray);
        super.onSaveInstanceState(outState);
    }

    public class FetchData extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Fetching Movies");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            // Query vars
            String apiKey = "f1cd8e955ce536c89bfa7aa2726cbc55";
            String sortBy = params[0];

            try {
                // Query parameters
                final String url = "http://api.themoviedb.org/3/discover/movie?";
                final String sort = "sort_by";
                final String api = "api_key";

                Uri uri;
                if (sortBy.isEmpty()) {
                    uri = Uri.parse(url).buildUpon()
                            .appendQueryParameter(api, apiKey)
                            .build();
                } else {
                    uri = Uri.parse(url).buildUpon()
                            .appendQueryParameter(sort, sortBy)
                            .appendQueryParameter(api, apiKey)
                            .build();
                }

                URL finalURL = new URL(uri.toString());
                Log.v("Main Activity", finalURL.toString());

                httpURLConnection = (HttpURLConnection) finalURL.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String jsonText;

                while ((jsonText = bufferedReader.readLine()) != null) {
                    buffer.append(jsonText);
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonResponse = buffer.toString();
            } catch (IOException ex) {
                Log.e("Main Activity", "Error ", ex);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        Log.e("Main Activity", "Error ", ex);
                    }
                }
            }

            try {
                parseJSON(jsonResponse);
            } catch (JSONException ex) {
                Log.e("Error: ", ex.getMessage());
            }
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            GridAdapter gridAdapter = new GridAdapter(MainActivity.this, gridContentArray);
            ((GridView) findViewById(R.id.gridView)).setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }
}
