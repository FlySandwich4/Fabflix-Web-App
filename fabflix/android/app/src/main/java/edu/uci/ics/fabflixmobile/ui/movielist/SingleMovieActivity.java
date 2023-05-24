package edu.uci.ics.fabflixmobile.ui.movielist;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySingleBinding;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SingleMovieActivity extends AppCompatActivity {

    private TextView b_title, b_dir, b_year, b_rating, b_stars, b_genres;
    private final String host = "3.101.28.218";
    private final String port = "8443";
    private final String domain = "fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySingleBinding binding = ActivitySingleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        b_title = binding.title;
        b_dir = binding.director;
        b_year = binding.year;
        b_rating = binding.rating;
        b_stars = binding.star;
        b_genres = binding.genre;


                Intent intent = getIntent();
        String id = intent.getStringExtra("id");




        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id="+id,
                response -> {

                    Log.d("single-movie.start","trying...");
                    try {

                        JSONObject rs = new JSONObject(response);
                        parse(rs,intent);

                    } catch (Exception e) {
                        Log.d("ERROR",e.toString());
                    }


                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);

    }

    private void parse(JSONObject eachObj, Intent intent){

        try {
            String title = eachObj.getString("title");
            String year = eachObj.getString("year");
            String director = eachObj.getString("director");
            JSONArray genres = (JSONArray) eachObj.get("gen") ;
            JSONArray stars = (JSONArray) eachObj.get("star") ;
            String rating = "Rating: " + eachObj.getString("rating");

            String starStr = "Stars: ";
            for(int j=0; j<stars.length();j++){
                JSONObject eachStar = (JSONObject) stars.get(j);
                starStr += eachStar.getString("name") + "    ";
            }

            String genreStr = "Genres: ";
            for(int j=0; j<genres.length();j++){
                JSONObject eachGenre = (JSONObject) genres.get(j);
                genreStr += eachGenre.getString("name") + "    ";
            }



            b_title.setText(title);
            b_dir.setText(director);
            b_year.setText(year);
            b_rating.setText(rating);
            b_stars.setText(starStr);
            b_genres.setText(genreStr);


        } catch (Exception e) {
            Log.d("search.Error",e.toString());
        }


        System.out.println("KILL!!!");




    }
}