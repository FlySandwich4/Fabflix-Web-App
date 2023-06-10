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
import java.util.Random;


@WebServlet(name = "Conformation", urlPatterns = "/api/Conformation")
public class Conformation extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            Random rand = new Random();
            int db_choice = rand.nextInt(2);
            if (db_choice == 0) {
                // Master choice
                System.out.println("Choosing Master's DB connection");
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master-moviedb");
            } else {
                // Slave choice
                System.out.println("Choosing Slave's DB connection");
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slave-moviedb");
            }
            // dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // { movieId: {num: num of movie, name: name of the movie}}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

            if (request.getSession().getAttribute("cart") == null) {
                request.getSession().setAttribute("cart", new JsonObject());
            }
            try {
                response.getWriter().write(request.getSession().getAttribute("cart").toString());
            } catch (Exception e) {
                response.getWriter().write(e.toString());
            }



    }

}
