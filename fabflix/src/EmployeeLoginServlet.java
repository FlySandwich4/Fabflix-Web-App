import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
    private DataSource dataSource;
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        //System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);


        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }


        try(Connection conn = dataSource.getConnection()){
            String query = "SELECT * from employees WHERE employees.email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()){
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "user " + email + " doesn't exist");
                return;
            }
            boolean success = false;

            StrongPasswordEncryptor enc = new StrongPasswordEncryptor();
            System.out.println("checking...");
            success = enc.checkPassword(password, rs.getString("password"));
            System.out.println(success);
            if(success){
                // success login set session
                System.out.println("good password");
                request.getSession().setAttribute("employee", email);

                // success attribute
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                try {
                    RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                } catch (Exception e) {
                    System.out.println("exception happened");
                    responseJsonObject.addProperty("status", "recaptcha");
                    responseJsonObject.addProperty("message", "Please do the verification");
                    // Log error to localhost log
                    request.getServletContext().log("Error:", e);
                    // Set response status to 500 (Internal Server Error)
                    response.setStatus(200);
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }
            }else{
                System.out.println("bad password");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect password");
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("exception happened");
            responseJsonObject.addProperty("status", "fail");

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally{
            response.getWriter().write(responseJsonObject.toString());
        }

    }
}
