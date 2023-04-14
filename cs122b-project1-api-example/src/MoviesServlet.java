import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query =
                    "SELECT * from movies, ratings " +
                            "WHERE movies.id = ratings.movieId " +
                            "ORDER BY ratings.rating DESC " +
                            "LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String rating = rs.getString("rating");
                String director = rs.getString("director");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", director);
                jsonObject.addProperty("movie_rating", rating);

                // Get top 3 genre
                String gen_q = "SELECT DISTINCT genres.name FROM genres " +
                        "INNER JOIN genres_in_movies ON genres.id = genres_in_movies.genreId " +
                        "INNER JOIN movies ON genres_in_movies.movieId = movies.id " +
                        "WHERE movies.id =  '" + movie_id +"';";
                Statement gen_state = conn.createStatement();
                ResultSet gen_rs = gen_state.executeQuery(gen_q);
                JsonArray gen_array = new JsonArray();
                while (gen_rs.next()){
                    gen_array.add(gen_rs.getString("genres.name"));
                }
                jsonObject.add("gen", gen_array);
                gen_rs.close();
                gen_state.close();


                // Get top 3 stars
                String star_q = "SELECT DISTINCT * FROM stars " +
                        "INNER JOIN stars_in_movies ON stars.id = stars_in_movies.starId " +
                        "INNER JOIN movies ON stars_in_movies.movieId = movies.id " +
                        "WHERE movies.id =  '" + movie_id +"';";
                Statement star_state = conn.createStatement();
                ResultSet star_rs = star_state.executeQuery(star_q);
                JsonArray star_arr = new JsonArray();
                while (star_rs.next()){
                    JsonObject star_to_id = new JsonObject();
                    star_to_id.addProperty("name",star_rs.getString("stars.name"));
                    star_to_id.addProperty("id",star_rs.getString("stars.id"));
                    star_arr.add(star_to_id);
                }
                jsonObject.add("star",star_arr);
                star_rs.close();
                star_state.close();


                // END of the loop
                // add this rs.next to array
                jsonArray.add(jsonObject);

            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            //System.out.println(jsonArray.toString());
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
