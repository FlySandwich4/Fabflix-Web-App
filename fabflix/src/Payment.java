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
                // cartInfo: {cart : { movieId: {...}, movieId: {...}}}
                JsonObject cartInfo = (JsonObject)request.getSession().getAttribute("cart");
                JsonObject cartInfoCopy = (JsonObject)request.getSession().getAttribute("cart");
                for(Map.Entry<String, JsonElement> each: cartInfo.entrySet()){
                    // each: {movieID : {num: 1, name: title}
                    String insertSale = "INSERT INTO sales (customerId, movieId,saleDate) VALUES('" +
                            rs.getString("c.id") + "','" +
                            each.getKey() + "','"+
                            java.sql.Date.valueOf(LocalDate.now()) + "');";
                    PreparedStatement insertSales = conn.prepareStatement(insertSale);
                    JsonObject prop = (JsonObject) each.getValue();
                    // prop: {num:1, name: title}
                    JsonArray saleId = new JsonArray();
                    for(int i=0; i<prop.get("num").getAsInt();i++){
                        insertSales.executeUpdate();
                        String getLastSaleId = "SELECT id FROM sales WHERE customerId = ? ORDER BY id DESC LIMIT 1";
                        PreparedStatement getLastSaleIdStmt = conn.prepareStatement(getLastSaleId);
                        getLastSaleIdStmt.setString(1, rs.getString("c.id"));
                        ResultSet lastOne = getLastSaleIdStmt.executeQuery();
                        if(lastOne.next()){
                            saleId.add(lastOne.getString("id"));
                        }
                    }
                    System.out.println("here" + prop);
                    // prop: {num: 1, name: title, saleId: [ id, id, id]}
                    prop.add("saleId",saleId);
                    // add prop to cartCopy
                    cartInfoCopy.add(each.getKey(),prop);

                    insertSales.close();
                    System.out.println("inserted: " + insertSale);
                }
                request.getSession().setAttribute("cart",cartInfoCopy);
                JsonObject res = new JsonObject();
                System.out.println(request.getSession().getAttribute("cart"));
                res.addProperty("success",1);
                response.getWriter().write(res.toString());
            }else{
                JsonObject res = new JsonObject();
                res.addProperty("success",0);
                response.getWriter().write(res.toString());
            }

            rs.close();
            statement.close();
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
