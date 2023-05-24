package edu.uci.ics.fabflixmobile.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name,id;
    private final short year;
    private final String genres,stars,rating;


    public Movie(String id, String name, short year, String stars, String genres, String rating) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.stars = stars;
        this.genres = genres;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getGenres(){
//        String rs = "";
//        for(int i=0; i<Math.max(genres.length(),3);i++){
//            JSONObject eachGenre = (JSONObject) genres.get(i);
//            rs += eachGenre.getString("name");
//        }
//        return rs;
        return genres;
    }

    public String getStars() {
        return stars;
    }

    public String getRating() {
        return rating;
    }
}