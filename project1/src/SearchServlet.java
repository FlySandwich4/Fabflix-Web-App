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

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        // The log message can be found in localhost log
        request.getServletContext().log("title: " + title);
        request.getServletContext().log("year: " + year);
        request.getServletContext().log("director: " + director);
        request.getServletContext().log("star: " + star);


        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String query = "SELECT mv.id,\n" +
                    "    mv.title,\n" +
                    "    mv.director,\n" +
                    "    mv.year,\n" +
                    "    GROUP_CONCAT(DISTINCT s.name, ':' , s.id SEPARATOR ', ') AS stars,\n" +
                    "    GROUP_CONCAT(DISTINCT g.name, ':' , g.id SEPARATOR ', ') AS genres,\n" +
                    "    r.rating \n" +

                    "FROM movies AS mv, \n" +
                    "    genres AS g, \n" +
                    "    stars AS s, \n" +
                    "    genres_in_movies AS gim, \n" +
                    "    stars_in_movies AS sim, \n" +
                    "    ratings AS r " +
                    "WHERE \n" +
                    "    mv.id=gim.movieId \n" +
                    "    AND mv.id=r.movieId \n" +
                    "    AND gim.genreId=g.Id \n" +
                    "    AND mv.id=sim.movieId \n" +
                    "    AND sim.starId=s.id \n"
                    ;

            if(title != null){
                query += "AND mv.title LIKE '%"+title+"%' \n";
            }
            if(star != null){
                query += "AND EXISTS (\n" +
                        "            SELECT *\n" +
                        "            FROM stars AS s2, movies AS mv2, stars_in_movies AS sim2\n" +
                        "            WHERE s2.id=sim2.starId AND sim2.movieId=mv.id AND s2.name LIKE '%" +
                        star +       "%'\n)";
            }
            if(year != null){
                query += "AND mv.year LIKE '%" + year + "%' \n";
            }
            if(director != null){
                query += "AND mv.director LIKE '%" + director + "%' \n";
            }
            query += " GROUP BY mv.id;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            // Iterate through each row of rs
            while (rs.next()) {
                JsonObject jsonObj = new JsonObject();
                String rsTitle = rs.getString("mv.title");
                String rsYear = rs.getString("mv.year");
                String rsDirector = rs.getString("mv.director");
                String rsStars = rs.getString("stars");
                String rsGenres = rs.getString("genres");
                String rsRating = rs.getString("r.rating");

                jsonObj.addProperty("title", rsTitle);
                jsonObj.addProperty("year", rsYear);
                jsonObj.addProperty("director", rsDirector);
                // System.out.println("stars: " + rsStars.split(", "));
                String[] stars = rsStars.split(", ");
                JsonArray starArr = new JsonArray();
                for(String eachStar: stars){
                    String[] starAndId = eachStar.split(":");
                    JsonObject starAndIdJson = new JsonObject();
                    starAndIdJson.addProperty("name",starAndId[0]);
                    starAndIdJson.addProperty("id",starAndId[1]);
                    starArr.add(starAndIdJson);
                }
                jsonObj.add("stars", starArr);

                String[] genres = rsGenres.split(", ");
                JsonArray genreArr = new JsonArray();
                for(String eachGenre: genres){
                    String[] genreAndId = eachGenre.split(":");
                    JsonObject genreAndIdJson = new JsonObject();
                    genreAndIdJson.addProperty("name",genreAndId[0]);
                    genreAndIdJson.addProperty("id",genreAndId[1]);
                    genreArr.add(genreAndIdJson);
                }
                jsonObj.add("genres", genreArr);
                jsonObj.addProperty("rating", rsRating);
                jsonArray.add(jsonObj);
            }
            // System.out.println(jsonObj.toString());
            rs.close();
            statement.close();

            // Write JSON string to output
            response.getWriter().write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
