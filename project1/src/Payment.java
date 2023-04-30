import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;


@WebServlet(name = "Payment", urlPatterns = "/api/payment")
public class Payment extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCard = request.getParameter("creditCard");
        String Date = request.getParameter("expiration");

        System.out.println(firstName+" "+lastName+" "+creditCard+" "+Date);

        try(Connection conn = dataSource.getConnection()){

            String query = "SELECT *" +
                    "FROM creditcards AS cd, customers AS c " +
                    "WHERE cd.id=c.ccId " +
                    "AND cd.firstName LIKE '" + firstName + "' \n" +
                    "AND cd.lastName LIKE '" + lastName + "' \n" +
                    "AND cd.id LIKE '" + creditCard + "' \n" +
                    "AND DATE_FORMAT(cd.expiration, '%Y-%m-%d') LIKE '" + Date + "'; \n" ;
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray genreList = new JsonArray();

            if(rs.next()){
                JsonObject cartInfo = (JsonObject)request.getSession().getAttribute("cart");
                for(Map.Entry<String, JsonElement> each: cartInfo.entrySet()){
                    String insertSale = "INSERT INTO sales (customerId, movieId,saleDate) VALUES('" +
                            rs.getString("c.id") + "','" +
                            each.getKey() + "','"+
                            java.sql.Date.valueOf(LocalDate.now()) + "');";
                    PreparedStatement insertSales = conn.prepareStatement(insertSale);
                    JsonObject prop = (JsonObject) each.getValue();
                    for(int i=0; i<prop.get("num").getAsInt();i++){
                        insertSales.executeUpdate();
                    }
                    insertSales.close();
                    System.out.println("inserted: " + insertSale);
                }
                JsonObject res = new JsonObject();
                res.addProperty("ok",1);
                response.getWriter().write(res.toString());
            }else{
                JsonObject res = new JsonObject();
                res.addProperty("ok",0);
                response.getWriter().write(res.toString());
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
