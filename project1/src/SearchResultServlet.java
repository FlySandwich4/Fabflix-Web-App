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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SearchResultServlet", urlPatterns = "/api/searchResult")
public class SearchResultServlet extends HttpServlet {
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

        String seachType = request.getParameter("search");
        String genreId = request.getParameter("genre");
        String letter = request.getParameter("letter");

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        String limit = request.getParameter("limit");
        String page = request.getParameter("page");

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
                    "    GROUP_CONCAT(DISTINCT s.name, ':' , s.id,':',num_movies ORDER BY num_movies DESC, s.name ASC SEPARATOR ', ' ) AS stars,\n" +
                    "    GROUP_CONCAT(DISTINCT g.name, ':' , g.id ORDER BY g.name ASC SEPARATOR ', ' ) AS genres,\n" +
                    "    r.rating \n" +

                    "FROM movies AS mv, \n" +
                    "    genres AS g, \n" +
                    "    stars AS s, \n" +
                    "    genres_in_movies AS gim, \n" +
                    "    stars_in_movies AS sim, \n" +
                    "    ratings AS r, " +
                    "   (" +
                    "       SELECT starId, COUNT(sim.movieId) AS num_movies\n" +
                    "       FROM stars_in_movies AS sim\n" +
                    "       GROUP BY starId\n" +
                    "    ) AS sm  \n" +
                    "WHERE \n" +
                    "    mv.id=gim.movieId \n" +
                    "    AND mv.id=r.movieId \n" +
                    "    AND gim.genreId=g.Id \n" +
                    "    AND mv.id=sim.movieId \n" +
                    "    AND sim.starId=s.id \n" +
                    "    AND sm.starId=s.id \n"
                    ;

            if(seachType == null){
                Object objseachType = request.getSession().getAttribute("searchType");
                seachType = objseachType != null ? objseachType.toString() : null;

                Object objgenreId = request.getSession().getAttribute("genre");
                genreId = objgenreId != null ? objgenreId.toString() : null;

                Object objletter = request.getSession().getAttribute("letter");
                letter = objletter != null ? objletter.toString() : null;

                Object objtitle = request.getSession().getAttribute("title");
                title = objtitle != null ? objtitle.toString() : null;

                Object objyear = request.getSession().getAttribute("year");
                year  = objyear != null ? objyear.toString() : null;

                Object objdirector = request.getSession().getAttribute("director");
                director  = objdirector != null ? objdirector.toString() : null;

                Object objstar = request.getSession().getAttribute("star");
                star = objstar != null ? objstar.toString() : null;

                Object objpage = request.getSession().getAttribute("page");
                page = objpage != null ? objpage.toString() : null;

                System.out.println("Session Read For Back: "+seachType);
            }
            if(seachType.equals("genre")){
                request.getSession().setAttribute("searchType","genre");
                request.getSession().setAttribute("genre",genreId);
                query += "AND EXISTS (\n" +
                        "            SELECT *\n" +
                        "            FROM genres AS g2, movies AS mv2, genres_in_movies AS gim2\n" +
                        "            WHERE g2.id=gim2.genreId AND gim2.movieId=mv.id AND g2.id=" +
                        genreId +       " \n)";
            }
            else if(seachType.equals("letter")){
                request.getSession().setAttribute("searchType","letter");
                request.getSession().setAttribute("letter",letter);
                query += "AND mv.title LIKE '"+letter+"%' \n";
            }
            else if(seachType.equals("search")){
                request.getSession().setAttribute("searchType","search");
                request.getSession().setAttribute("title",title);
                request.getSession().setAttribute("star",star);
                request.getSession().setAttribute("year",year);
                request.getSession().setAttribute("director",director);

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
                    query += "AND mv.year LIKE '" + year + "' \n";
                }
                if(director != null){
                    query += "AND mv.director LIKE '%" + director + "%' \n";
                }
            }

            query += " GROUP BY mv.id \n";

            if(sort != null){
                if(sort.equals("rating")){
                    query += "ORDER BY r.rating DESC\n";
                }
                if(sort.equals("title")){
                    query += "ORDER BY mv.title \n";
                }
                if(sort.equals("genre")){
                    query += "ORDER BY genres \n";
                }
            }
            if(limit != null){
                query += "LIMIT "+limit+" \n";
            }

            if(page != null){
                request.getSession().setAttribute("page",page);
                int numPage = Integer.parseInt(page);
                if(numPage > 1){
                    query += "OFFSET "+ (numPage-1)*10 +" \n";
                }
            }
            query += ";";



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
                String rsMovieId = rs.getString("mv.id");

                jsonObj.addProperty("id", rsMovieId);
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
                    starAndIdJson.addProperty("total-movies",starAndId[2]);
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
