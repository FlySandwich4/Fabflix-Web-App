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

        String searchType = request.getParameter("search");
        String genreId = request.getParameter("genre");
        String letter = request.getParameter("letter");

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        String limit = request.getParameter("limit");
        String page = request.getParameter("page");

        String back = request.getParameter("back");

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

            //|| searchType.equals("")
            if(searchType == null ){
                System.out.println("Triggered can not find searchType");

                Object objsearchType = request.getSession().getAttribute("searchType");
                searchType = objsearchType != null ? objsearchType.toString() : null;

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

                if(back != null && !back.isEmpty()){
                    Object objpage = request.getSession().getAttribute("page");
                    page = objpage != null ? objpage.toString() : null;

                    Object objsort = request.getSession().getAttribute("sort");
                    sort = objsort != null ? objsort.toString() : null;

                    Object objlimit = request.getSession().getAttribute("limit");
                    limit = objlimit != null ? objlimit.toString() : null;

                }


                System.out.println("Session Read For Back: "+searchType);
            }
//            if(searchType.equals("genre")){
//                request.getSession().setAttribute("searchType","genre");
//                request.getSession().setAttribute("genre",genreId);
//                query += "AND EXISTS (\n" +
//                        "            SELECT *\n" +
//                        "            FROM genres AS g2, movies AS mv2, genres_in_movies AS gim2\n" +
//                        "            WHERE g2.id=gim2.genreId AND gim2.movieId=mv.id AND g2.id=" +
//                        genreId +       " \n)";
//            }
            if (searchType.equals("genre")) {
                request.getSession().setAttribute("searchType", "genre");
                request.getSession().setAttribute("genre", genreId);
                query += " AND EXISTS ("
                        + " SELECT * "
                        + " FROM genres AS g2, movies AS mv2, genres_in_movies AS gim2 "
                        + " WHERE g2.id=gim2.genreId AND gim2.movieId=mv.id AND g2.id=?) ";
            }
            else if(searchType.equals("letter")){
                request.getSession().setAttribute("searchType", "letter");
                request.getSession().setAttribute("letter", letter);
                if (letter.equals("")) {
                    query += " AND mv.title REGEXP '^[^A-Za-z0-9].*' ";
                } else {
                    query += " AND mv.title LIKE ? ";
                }

            }
            else if(searchType.equals("search")){
                request.getSession().setAttribute("searchType","search");
                request.getSession().setAttribute("title",title);
                request.getSession().setAttribute("star",star);
                request.getSession().setAttribute("year",year);
                request.getSession().setAttribute("director",director);

                System.out.println(title + " " + star + year + director);

                if(title != null && !title.isEmpty()){
                    System.out.println("title");
                    query += "AND mv.title LIKE ? \n";
                }
                if(star != null && !star.isEmpty()){
                    System.out.println("star");
//                    query += "AND EXISTS (\n" +
//                            "            SELECT *\n" +
//                            "            FROM stars AS s2, movies AS mv2, stars_in_movies AS sim2\n" +
//                            "            WHERE s2.id=sim2.starId AND sim2.movieId=mv.id AND s2.name LIKE '%" +
//                            star +       "%'\n)";
                    query += "AND EXISTS ( "
                            + "SELECT * "
                            + "FROM stars AS s2, movies AS mv2, stars_in_movies AS sim2 "
                            + "WHERE s2.id=sim2.starId AND sim2.movieId=mv.id AND s2.name LIKE ?) ";

                }
                if(year != null && !year.isEmpty()){
                    query += "AND mv.year LIKE ? ";
                }
                if(director != null && !director.isEmpty()){
                    //query += "AND mv.director LIKE '%" + director + "%' \n";
                    query += "AND mv.director LIKE ? ";
                }
            }

            query += " GROUP BY mv.id \n";



            // HERE
            // COUNT TOTAL DATA WE GOT !!!!!
            String countTotal = "SELECT COUNT(*) AS count FROM (" + query + ") AS c";

            int sortSelect = 0;

            if(sort != null && !sort.isEmpty()){
                request.getSession().setAttribute("sort",sort);
                if(sort.equals("ta-ra")){
                    query += "ORDER BY mv.title ASC, r.rating ASC \n";
                    sortSelect = 0;
                }
                if(sort.equals("ta-rd")){
                    query += "ORDER BY mv.title ASC, r.rating DESC \n";
                    sortSelect = 1;
                }
                if(sort.equals("td-ra")){
                    query += "ORDER BY mv.title DESC, r.rating ASC \n";
                    sortSelect = 2;
                }
                if(sort.equals("td-rd")){
                    query += "ORDER BY mv.title DESC, r.rating DESC \n";
                    sortSelect = 3;
                }
                if(sort.equals("ra-ta")){
                    query += "ORDER BY r.rating ASC, mv.title ASC \n";
                    sortSelect = 4;
                }
                if(sort.equals("ra-td")){
                    query += "ORDER BY r.rating ASC, mv.title DESC \n";
                    sortSelect = 5;
                }
                if(sort.equals("rd-ta")){
                    query += "ORDER BY r.rating DESC, mv.title ASC \n";
                    sortSelect = 6;
                }
                if(sort.equals("rd-td")){
                    query += "ORDER BY r.rating DESC, mv.title DESC \n";
                    sortSelect = 7;
                }
            }
            int limitSelect = 0;
            if(limit != null && !limit.isEmpty()){
                request.getSession().setAttribute("limit",limit);
                query += "LIMIT "+limit+" \n";
                if(limit.equals("10")){
                    limitSelect = 0;
                }
                if(limit.equals("25")){
                    limitSelect = 1;
                }
                if(limit.equals("50")){
                    limitSelect = 2;
                }
                if(limit.equals("100")){
                    limitSelect = 3;
                }

            }

            if(page != null && !page.isEmpty()){
                request.getSession().setAttribute("page",page);
                System.out.println("page = " + page);
                int numPage = Integer.parseInt(page);
                if(numPage > 1){
                    // query += "OFFSET "+ (numPage-1)* Integer.parseInt(limit) +" \n";
                    query += "OFFSET ? \n";
                }
            }
            query += ";";

            JsonArray jsonArray = new JsonArray();

            PreparedStatement countstatement = conn.prepareStatement(countTotal);

            // SET ? in prepared statement in CountStatement
            int paramIndex = 1;
            if (searchType.equals("genre")) {
                countstatement.setString(paramIndex, genreId);
            } else if (searchType.equals("letter")) {
                if (!letter.equals("*")) {
                    countstatement.setString(paramIndex, letter + "%");
                }
            } else if (searchType.equals("search")) {

                if (title != null && !title.isEmpty()) {
                    countstatement.setString(paramIndex++, "%" + title + "%");
                }
                if (star != null && !star.isEmpty()) {
                    countstatement.setString(paramIndex++, "%" + star + "%");
                }
                if (year != null && !year.isEmpty()) {
                    countstatement.setString(paramIndex++, year);
                }
                if (director != null && !director.isEmpty()) {
                    countstatement.setString(paramIndex, "%" + director + "%");
                }
            }

            ResultSet count = countstatement.executeQuery();
            JsonObject Info = new JsonObject();
            // count is used for store information about total records
            while (count.next()){
                Info.addProperty("count",count.getInt("count"));
                Info.addProperty("current",Integer.parseInt(page));
                Info.addProperty("limit",Integer.parseInt(limit));
            }
            Info.addProperty("sortSelect",sortSelect);
            Info.addProperty("limitSelect",limitSelect);
            jsonArray.add(Info);


            PreparedStatement statement = conn.prepareStatement(query);

            paramIndex = 1;
            if (searchType.equals("genre")) {
                statement.setString(paramIndex++, genreId);
            } else if (searchType.equals("letter")) {
                if (!letter.equals("*")) {
                    statement.setString(paramIndex++, letter + "%");
                }
            } else if (searchType.equals("search")) {

                if (title != null && !title.isEmpty()) {
                    statement.setString(paramIndex++, "%" + title + "%");
                }
                if (star != null && !star.isEmpty()) {
                    statement.setString(paramIndex++, "%" + star + "%");
                }
                if (year != null && !year.isEmpty()) {
                    statement.setString(paramIndex++, year);
                }
                if (director != null && !director.isEmpty()) {
                    statement.setString(paramIndex++, "%" + director + "%");
                }
            }
            if(page != null && !page.isEmpty()){
                int numPage = Integer.parseInt(page);
                if(numPage > 1){
                    // query += "OFFSET "+ (numPage-1)* Integer.parseInt(limit) +" \n";
                    statement.setInt(paramIndex, (numPage-1)* Integer.parseInt(limit));
                }
            }

            ResultSet rs = statement.executeQuery();




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
