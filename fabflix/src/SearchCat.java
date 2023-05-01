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


@WebServlet(name = "SearchCatServlet", urlPatterns = "/api/searchCat")
public class SearchCat extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        String seachType = request.getParameter("search");
        String genreId = request.getParameter("genre");
        String letter = request.getParameter("letter");

        try(Connection conn = dataSource.getConnection()){
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
            if(seachType.equals("genre")){
                query += "AND g.id="+genreId+" \n";
            }
            else if(seachType.equals("letter")){
                query += "AND mv.title LIKE '"+letter+"%' \n";
            }
            query += " GROUP BY mv.id \n";


        }catch (Exception e){
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
