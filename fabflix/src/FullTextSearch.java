import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@WebServlet(name = "FullTextSearch", urlPatterns = "/api/fulltextsearch")
public class FullTextSearch extends HttpServlet {
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
        String search = request.getParameter("query");
        String page = request.getParameter("page");
        if(search==null){
            return;
        }




        try(Connection conn = dataSource.getConnection()){
//            String query = "SELECT mv.id,\n" +
//                "    mv.title,\n" +
//                "    mv.director,\n" +
//                "    mv.year,\n" +
//                "    GROUP_CONCAT(DISTINCT s.name, ':' , s.id,':',num_movies ORDER BY num_movies DESC, s.name ASC SEPARATOR ', ' ) AS stars,\n" +
//                "    GROUP_CONCAT(DISTINCT g.name, ':' , g.id ORDER BY g.name ASC SEPARATOR ', ' ) AS genres,\n" +
//                "    r.rating \n" +
//                "FROM movies AS mv\n " +
//                "JOIN genres_in_movies AS gim ON mv.id = gim.movieId\n " +
//                "JOIN stars_in_movies AS sim ON mv.id = sim.movieId\n " +
//                "JOIN genres AS g ON g.id = gim.genreId\n " +
//                "JOIN stars AS s ON sim.starId = s.id\n " +
//                "LEFT JOIN ratings AS r ON mv.id = r.movieId\n " +
//                "JOIN (\n" +
//                "    SELECT starId, COUNT(sim.movieId) AS num_movies\n " +
//                "    FROM stars_in_movies AS sim\n " +
//                "    GROUP BY starId\n " +
//                ") AS sm ON s.id = sm.starId " +
//
//                "WHERE MATCH (mv.title) AGAINST (? IN BOOLEAN MODE) "+
//                " GROUP BY mv.id " +
//                    " LIMIT 20 OFFSET ? ;"
//                ;
            String query = "SELECT mv.id, " +
                    "mv.title, " +
                    "mv.director, " +
                    "mv.year, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.name, ':', s.id, ':', num_movies) ORDER BY num_movies DESC, s.name ASC SEPARATOR ', ') AS stars, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(g.name, ':', g.id) ORDER BY g.name ASC SEPARATOR ', ') AS genres, " +
                    "r.rating " +
                    "FROM (" +
                    "   SELECT * FROM movies AS m " +
                    "   WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) " +
                    "   LIMIT 50 OFFSET ? " +
                    ") AS mv " +
                    "LEFT JOIN ratings AS r ON mv.id = r.movieId " +
                    "JOIN genres_in_movies AS gim ON mv.id = gim.movieId " +
                    "JOIN genres AS g ON g.id = gim.genreId " +
                    "JOIN stars_in_movies AS sim ON mv.id = sim.movieId " +
                    "JOIN stars AS s ON sim.starId = s.id " +

                    "JOIN ( " +
                    "    SELECT starId, COUNT(sim.movieId) AS num_movies " +
                    "    FROM stars_in_movies AS sim " +
                    "    GROUP BY starId " +
                    ") AS sm ON s.id = sm.starId " +

                    "GROUP BY mv.id ;" ;


            PreparedStatement statement = conn.prepareStatement(query);
            System.out.println(statement);

            String[] words = search.split("\\s+");

            String fulltextQuery = Arrays.stream(words)
                    .map(word -> "+" + word + "*")
                    .collect(Collectors.joining(" "));

            //System.out.println(fulltextQuery);

            statement.setString(1,fulltextQuery);
            statement.setInt(2,Integer.parseInt(page)*20);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObj = new JsonObject();
                String rsTitle = rs.getString("mv.title");
                String rsYear = rs.getString("mv.year");
                String rsDirector = rs.getString("mv.director");
                String rsStars = rs.getString("stars");
                String rsGenres = rs.getString("genres");
                String rsRating = rs.getString("r.rating");
                String rsMovieId = rs.getString("mv.id");

                jsonObj.addProperty("id", rsMovieId);
                jsonObj.addProperty("title", rsTitle);
                jsonObj.addProperty("year", rsYear);
                jsonObj.addProperty("director", rsDirector);
                // System.out.println("stars: " + rsStars.split(", "));
                String[] stars = rsStars.split(", ");
                JsonArray starArr = new JsonArray();
                for(String eachStar: stars){
                    //System.out.println(eachStar);
                    String[] starAndId = eachStar.split(":");
                    JsonObject starAndIdJson = new JsonObject();
                    try{
                        starAndIdJson.addProperty("name",starAndId[0]);
                        starAndIdJson.addProperty("id",starAndId[1]);
                        starAndIdJson.addProperty("total-movies",starAndId[2]);
                        starArr.add(starAndIdJson);
                    }catch (Exception e){
                        continue;
                    }

                }
                jsonObj.add("stars", starArr);

                String[] genres = rsGenres.split(", ");
                JsonArray genreArr = new JsonArray();
                for(String eachGenre: genres){
                    //System.out.println(eachGenre);
                    String[] genreAndId = eachGenre.split(":");
                    JsonObject genreAndIdJson = new JsonObject();
                    try{
                        genreAndIdJson.addProperty("name",genreAndId[0]);
                        genreAndIdJson.addProperty("id",genreAndId[1]);
                        genreArr.add(genreAndIdJson);
                    }catch (Exception e){
                        continue;
                    }
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

        }catch(Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }



}