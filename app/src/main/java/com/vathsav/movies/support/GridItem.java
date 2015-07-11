package com.vathsav.movies.support;

import android.os.Parcel;
import android.os.Parcelable;

public class GridItem implements Parcelable {
    private String movie_release_date;
    private String movie_synopsis;
    private String movie_user_rating;
    private String movie_title;
    private String movie_image;

    public GridItem(String title, String release_date, String user_rating, String image, String synopsis) {
        this.movie_title = title;
        this.movie_release_date = release_date;
        this.movie_user_rating = user_rating;
        this.movie_image = image;
        this.movie_synopsis = synopsis;
    }

    private GridItem(Parcel parcel) {
        movie_release_date = parcel.readString();
        movie_synopsis = parcel.readString();
        movie_user_rating = parcel.readString();
        movie_title = parcel.readString();
        movie_image = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movie_release_date);
        dest.writeString(movie_synopsis);
        dest.writeString(movie_user_rating);
        dest.writeString(movie_title);
        dest.writeString(movie_image);
    }

    public final Parcelable.Creator<GridItem> CREATOR = new Parcelable.Creator<GridItem>() {
        @Override
        public GridItem[] newArray(int size) {
            return new GridItem[size];
        }

        @Override
        public GridItem createFromParcel(Parcel source) {
            return new GridItem(source);
        }
    };

    public String getTitle() {
        return this.movie_title;
    }

    public String getReleaseDate() {
        return this.movie_release_date;
    }

    public String getUserRating() {
        return this.movie_user_rating;
    }

    public String getImage() {
        return this.movie_image;
    }

    public String getSynopsis() {
        return this.movie_synopsis;
    }
}
