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
import java.awt.*;
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

    // { movieId: {num: num of movie, name: name of the movie, salesid:[...]}}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        if(request.getParameter("updateType")==null){


            if (request.getSession().getAttribute("cart") == null) {
                request.getSession().setAttribute("cart", new JsonObject());
            }
            try {
                response.getWriter().write(request.getSession().getAttribute("cart").toString());
            } catch (Exception e) {
                response.getWriter().write(e.toString());
            }
        }else if(request.getParameter("updateType").equals("delete")){
            try{
                deleteItem(request,response);
            }catch (Exception e){
                System.out.println(e);
            }
        }
        else{
            try{
                putSome(request,response);
            }catch (Exception e){
                System.out.println(e);
                response.getWriter().write(e.toString());
                response.setStatus(500);
            }

        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String movieId = req.getParameter("movieId");
        String name = req.getParameter("name");
        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");

            if (cartInfo.has(movieId)) {
                JsonObject prop = (JsonObject) cartInfo.get(movieId);
                prop.addProperty("num",prop.get("num").getAsInt() + 1);
                cartInfo.add(movieId, prop);
            } else {
                JsonObject prop = new JsonObject();
                prop.addProperty("num", 1);
                prop.addProperty("name", name);
                cartInfo.add(movieId, prop);
            }
            req.getSession().setAttribute("cart",cartInfo);
            resp.getWriter().write(req.getSession().getAttribute("cart").toString());
            resp.setStatus(200);
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }


    protected void putSome(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("put execute");
        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }
            System.out.println("put 1");
            /**
             * @movieId : in params
             * @updateType : in params
             * @cartInfo : in Session
             */
            String movieId = req.getParameter("movieId");
            String updateType = req.getParameter("updateType");
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");
            System.out.println("put 2" + movieId + updateType);

            if(updateType.equals("increment")){
                JsonObject prop = (JsonObject) cartInfo.get(movieId);
                prop.addProperty("num",prop.get("num").getAsInt() + 1);
                cartInfo.add(movieId, prop);
            }
            else if(updateType.equals("decrement")){
                JsonObject prop = (JsonObject) cartInfo.get(movieId);
                prop.addProperty("num",prop.get("num").getAsInt() - 1);

                if(prop.get("num").getAsInt()==0){
                    cartInfo.remove(movieId);
                }else{
                    cartInfo.add(movieId, prop);
                }
            }
            System.out.println("put 3");
            req.getSession().setAttribute("cart",cartInfo);
            resp.getWriter().write(req.getSession().getAttribute("cart").toString());
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }


    protected void deleteItem(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            if (req.getSession().getAttribute("cart") == null) {
                req.getSession().setAttribute("cart", new JsonObject());
            }

            /**
             * @movieId : in params
             * @cartInfo : in Session
             */
            String movieId = req.getParameter("movieId");
            System.out.println(movieId + " Deleted");
            JsonObject cartInfo = (JsonObject) req.getSession().getAttribute("cart");

            cartInfo.remove(movieId);
            req.getSession().setAttribute("cart",cartInfo);
            resp.getWriter().write(req.getSession().getAttribute("cart").toString());
        }catch (Exception e){
            resp.getWriter().write(e.toString());
            resp.setStatus(500);
        }
    }
}
