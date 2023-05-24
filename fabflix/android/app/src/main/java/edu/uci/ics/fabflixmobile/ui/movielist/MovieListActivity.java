package edu.uci.ics.fabflixmobile.ui.movielist;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    private final String host = "3.101.28.218";
    private final String port = "8443";
    private final String domain = "fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);



        Intent intent = getIntent();

        String query = intent.getStringExtra("query");
        int page = intent.getIntExtra("page",0);



        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/fulltextsearch?query="+query+"&page="+page,
                response -> {

                    Log.d("search.start","trying...");
                    try {

                        JSONArray rs = new JSONArray(response);
                        parse(page,rs,intent);

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

    private void parse(int page, JSONArray rs, Intent intent){
        final ArrayList<Movie> movies = new ArrayList<>();

        for(int i=0; i<Math.min(rs.length(),20);i++){
            try {
                JSONObject eachObj = (JSONObject) rs.get(i);
                String id = eachObj.getString("id");
                String title = (i+1) + ". " + eachObj.getString("title");
                short year = (short) eachObj.getInt("year");
                JSONArray genres = (JSONArray) eachObj.get("genres") ;
                JSONArray stars = (JSONArray) eachObj.get("stars") ;

                String starStr = "Stars: ";
                for(int j=0; j<Math.min(stars.length(),3);j++){
                    JSONObject eachStar = (JSONObject) stars.get(j);
                    starStr += eachStar.getString("name") + "    ";
                }

                String genreStr = "Genres: ";
                for(int j=0; j<Math.min(genres.length(),3);j++){
                    JSONObject eachGenre = (JSONObject) genres.get(j);
                    genreStr += eachGenre.getString("name") + "    ";
                }

                String rating = "Rating: " + eachObj.getString("rating");

                Movie eachMv = new Movie(id,title,year,starStr,genreStr,rating);
                movies.add(eachMv);

            } catch (Exception e) {
                Log.d("search.Error",e.toString());
                break;
            }

        }
        System.out.println("KILL!!!");
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
//            Movie movie = movies.get(position);
//            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Intent SingleMovie = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            SingleMovie.putExtra("id",movies.get(position).getId());
            startActivity(SingleMovie);
        });


        // prev next buttons
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(v -> {
            int currentPage = intent.getIntExtra("page", 0);
            int previousPage = currentPage - 1;
            if(previousPage < 0){
                String message = "No more previous page";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            // Update the extra data
            intent.putExtra("page", previousPage);
            // Restart the activity to perform a new search with the updated page number
            finish();
            startActivity(intent);
        });

        nextButton.setOnClickListener(v -> {
            int currentPage = intent.getIntExtra("page", 0);
            int nextPage = currentPage + 1;
            // Update the extra data
            if(movies.size()<20){
                String message = "No more next page";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("page", nextPage);
            // Restart the activity to perform a new search with the updated page number
            finish();
            startActivity(intent);
        });

    }
}