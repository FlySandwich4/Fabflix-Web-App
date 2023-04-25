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
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        String sd = request.getParameter("sd");
        System.out.println(sd);

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String query = "SELECT * from stars WHERE stars.id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            JsonObject jsonObj = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {
                String starId = rs.getString("id");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");

                jsonObj.addProperty("star_id", starId);
                jsonObj.addProperty("star_name", starName);
                jsonObj.addProperty("star_dob", starDob);
            }
            rs.close();
            statement.close();

            /**
             *  Check every movie a star act
             */
            query = "SELECT * from stars, stars_in_movies as sm, movies WHERE sm.starId = stars.id  AND sm.starId = ? AND sm.movieId = movies.id;";
            statement = conn.prepareStatement(query);
            statement.setString(1, id);
            rs = statement.executeQuery();
            JsonArray movie_arr = new JsonArray();
            while(rs.next()){
                JsonObject movie_obj = new JsonObject();
                movie_obj.addProperty("title",rs.getString("movies.title"));
                movie_obj.addProperty("movieId",rs.getString("movies.id"));
                movie_arr.add(movie_obj);
            }
            jsonObj.add("movies", movie_arr);
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObj.toString());
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
