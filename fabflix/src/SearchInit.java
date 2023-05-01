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


@WebServlet(name = "SearchInitServlet", urlPatterns = "/api/searchInit")
public class SearchInit extends HttpServlet {
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

        try(Connection conn = dataSource.getConnection()){

            String query = "SELECT g.name, g.id\n" +
                    "FROM genres AS g;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray genreList = new JsonArray();
            while(rs.next()) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("name",rs.getString("g.name"));
                jsonObj.addProperty("id",rs.getString("g.id"));
                genreList.add(jsonObj);
            }
            rs.close();
            statement.close();
            response.getWriter().write(genreList.toString());
            response.setStatus(200);

        } catch (Exception e){
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
