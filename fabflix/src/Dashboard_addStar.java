import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Stream;

import org.jasypt.util.password.StrongPasswordEncryptor;


@WebServlet(name = "Dashboard_addStar", urlPatterns = "/dashboard/api/addstar")
public class Dashboard_addStar extends HttpServlet {
    private DataSource dataSource;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();

        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try(Connection conn = dataSource.getConnection()){
            String query = "SHOW TABLES FROM moviedb";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while(rs.next()){
                String tableName = rs.getString("Tables_in_moviedb");
                System.out.println(tableName);

                //responseJsonObject.addProperty(tableName, "yes");

                String metaQuery = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM information_schema.columns " +
                        "WHERE TABLE_SCHEMA='moviedb' AND TABLE_NAME=?;";
                PreparedStatement metaStatement = conn.prepareStatement(metaQuery);
                metaStatement.setString(1,tableName);
                ResultSet metaData = metaStatement.executeQuery();

                JsonArray columnsArray = new JsonArray();
                while(metaData.next()){
                    JsonObject column= new JsonObject();
                    column.addProperty("name",metaData.getString("COLUMN_NAME"));
                    column.addProperty("type",metaData.getString("DATA_TYPE"));
                    columnsArray.add(column);
                }

                metaStatement.close();
                metaData.close();

                responseJsonObject.add(tableName,columnsArray);
            }

            statement.close();
            rs.close();
            response.setStatus(200);

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("exception happened");
            responseJsonObject.addProperty("success", "no");

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally{
            response.getWriter().write(responseJsonObject.toString());
        }

    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        response.setContentType("application/json"); // Response mime type

        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        if(type != null){
            if(type.equals("addStar")){
                addStar(request,response);
            }
            else if(type.equals("addMovie")){
                addMovie(request,response);
            }
        }



    }

    protected void addStar(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String starName = request.getParameter("name");
        String birthYear = request.getParameter("birth-year");
        JsonObject responseJsonObject = new JsonObject();


        try(Connection conn = dataSource.getConnection()){
            String query = "SELECT * from stars ORDER BY id DESC";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Get the new ID for the star
            String newId = "";
            if(rs.next()){
                newId = rs.getString("id");
                int tail = (Integer.parseInt(newId.substring(2))+1);
                newId = newId.substring(0,2) + tail;

            }
            System.out.println(newId);

            // INSERT data to star
            String addStarQuery = "INSERT INTO stars (id,name,birthYear) VALUES (?,?,?)";
            PreparedStatement addStatement = conn.prepareStatement(addStarQuery);
            addStatement.setString(1,newId);
            addStatement.setString(2,starName);
            if(birthYear!=null&& !birthYear.equals("")){
                addStatement.setInt(3,Integer.parseInt(birthYear));
            }else{
                addStatement.setNull(3,java.sql.Types.INTEGER);
            }
            addStatement.executeUpdate();
            responseJsonObject.addProperty("success", "yes");


        } catch (Exception e){
            e.printStackTrace();
            System.out.println("exception happened");
            responseJsonObject.addProperty("success", "no");

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally{
            response.getWriter().write(responseJsonObject.toString());
        }
    }



    protected void addMovie(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star-name");
        String starYear = request.getParameter("star-year");
        String genre = request.getParameter("year");

        JsonObject responseJsonObject = new JsonObject();



        try(Connection conn = dataSource.getConnection()){

            try{

                InputStream content = getServletContext().getResourceAsStream("/stored-procedure.sql");
                String result = "";

                if (content != null) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }

                    reader.close();
                    result = sb.toString();
                }

                System.out.println(result);

                Statement storeProcedure = conn.createStatement();
                storeProcedure.executeUpdate(result);
                System.out.println("Successfully Store Procedure");

            }catch (Exception e){
                System.out.println("Error with procedure, might already exist");
                System.out.println(e);
            }

            if(title==null || title.equals("") ||
                    year==null || year.equals("") ||
                    director==null || director.equals("") ||
                    star==null || star.equals("") ||
                    genre==null || genre.equals("")
            ){
                responseJsonObject.addProperty("success", "no");
                responseJsonObject.addProperty("message", "Some Field Lost");
                //response.getWriter().write(responseJsonObject.toString());
                response.setStatus(200);
                return;
            }

            String checkMovieQuery = "SELECT COUNT(*) AS c FROM movies WHERE title=?;";
            PreparedStatement checkMovieStatement = conn.prepareStatement(checkMovieQuery);
            checkMovieStatement.setString(1,title);
            ResultSet checkRs = checkMovieStatement.executeQuery();
            if (checkRs.next()) {
                int count = checkRs.getInt("c");
                checkMovieStatement.close();
                checkRs.close();
                if (count > 0) {
                    responseJsonObject.addProperty("success", "no");
                    responseJsonObject.addProperty("message", "Movie already exists");
                    response.setStatus(200);
                    return;
                }
            }

            // INSERT data to star
            String addMovieQuery = "CALL add_movie(?,?,?,?,?,?); ";
            PreparedStatement addStatement = conn.prepareStatement(addMovieQuery);
            addStatement.setString(1,title);
            addStatement.setInt(2,Integer.parseInt(year));
            addStatement.setString(3,director);
            addStatement.setString(4,star);
            addStatement.setInt(5,Integer.parseInt(starYear));
            addStatement.setString(6,genre);

            addStatement.executeUpdate();
            responseJsonObject.addProperty("success", "yes");


        } catch (Exception e){
            e.printStackTrace();
            System.out.println("exception happened");
            responseJsonObject.addProperty("success", "no");
            responseJsonObject.addProperty("message", "Unexpected Backend Error");

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            // response.setStatus(200);
        }
        finally{
            response.getWriter().write(responseJsonObject.toString());
        }
    }
}
