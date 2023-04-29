import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "Cart", urlPatterns = "/api/cart")
public class Cart extends HttpServlet {
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

        if(request.getSession().getAttribute("cart") == null){
            request.getSession().setAttribute("cart",new JsonObject());
        }
        try{
            response.getWriter().write(request.getSession().getAttribute("cart").toString());
        }catch (Exception e){
            response.getWriter().write(e.toString());
        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String movieId = req.getParameter("movieId");
        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");

            if (cartInfo.has(movieId)) {
                cartInfo.addProperty(movieId, cartInfo.get(movieId).getAsInt() + 1);
            } else {
                cartInfo.addProperty(movieId, 1);
            }

            resp.getWriter().write("{ok:1}");
            resp.setStatus(200);
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }

            /**
             * @movieId : in params
             * @updateType : in params
             * @cartInfo : in Session
             */
            String movieId = req.getParameter("movieId");
            String updateType = req.getParameter("updateType");
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");

            if(updateType.equals("increment")){
                cartInfo.addProperty(movieId, cartInfo.get(movieId).getAsInt() + 1);
            }
            else if(updateType.equals("decrement")){
                cartInfo.addProperty(movieId, cartInfo.get(movieId).getAsInt() + 1);
                if(cartInfo.get(movieId).getAsInt()==0){
                    cartInfo.remove(movieId);
                }
            }
            resp.getWriter().write("{ok:1}");
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }

            /**
             * @movieId : in params
             * @cartInfo : in Session
             */
            String movieId = req.getParameter("movieId");
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");

            cartInfo.remove(movieId);

            resp.getWriter().write("{ok:1}");
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }
}
