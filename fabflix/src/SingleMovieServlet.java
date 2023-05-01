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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // REturn data
            JsonObject jsonRes = new JsonObject();

            // Get a connection from dataSource

            // Single movie general infomation
            // Construct a query with parameter represented by "?"
            String query = "SELECT * from movies, ratings WHERE movies.id=ratings.movieId AND movies.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            rs.next();

            // add to jsonRes
            jsonRes.addProperty("title",rs.getString("movies.title"));
            jsonRes.addProperty("year",rs.getString("movies.year"));
            jsonRes.addProperty("director",rs.getString("movies.director"));
            jsonRes.addProperty("rating",rs.getString("ratings.rating"));
            jsonRes.addProperty("id",rs.getString("movies.id"));

            // Close the rs and statement
            rs.close();
            statement.close();

            /**
             *  Get every genre of the movie
             */
            query = "SELECT * from genres, genres_in_movies as gm WHERE gm.movieId = ? AND gm.genreId = genres.id " +
                    "ORDER BY genres.name;";
            statement = conn.prepareStatement(query);
            statement.setString(1, id);
            rs = statement.executeQuery();
            JsonArray gen_arr = new JsonArray();
            while(rs.next()){
                JsonObject gen_obj = new JsonObject();
                gen_obj.addProperty("name",rs.getString("genres.name"));
                gen_obj.addProperty("genId",rs.getString("genres.id"));
                gen_arr.add(gen_obj);
            }
            jsonRes.add("gen", gen_arr);
            rs.close();
            statement.close();


            /**
             *  Get every stars of the movie
             */
            query = "SELECT * from stars, stars_in_movies as sm, movies, " +
                    "(" +
                    "       SELECT starId, COUNT(sim.movieId) AS num_movies\n" +
                    "       FROM stars_in_movies AS sim\n" +
                    "       GROUP BY starId\n" +
                    "    ) AS sm1  \n"+
                    "WHERE sm.movieId = ? AND sm.starId = stars.id" +
                    " AND sm.movieId = movies.id " +
                    " AND sm1.starId=sm.starId " +
                    " ORDER BY sm1.num_movies DESC, stars.name ASC;";
            statement = conn.prepareStatement(query);
            statement.setString(1, id);
            rs = statement.executeQuery();
            JsonArray star_arr = new JsonArray();
            while(rs.next()){
                JsonObject star_obj = new JsonObject();
                star_obj.addProperty("name",rs.getString("stars.name"));
                star_obj.addProperty("starId",rs.getString("stars.id"));
                star_arr.add(star_obj);
            }
            jsonRes.add("star", star_arr);
            rs.close();
            statement.close();



            // Write JSON string to output
            out.write(jsonRes.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
