import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

@WebServlet("/hero-suggestion")
public class HeroSuggestion extends HttpServlet {

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
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			
			// search on superheroes and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
			try(Connection conn = dataSource.getConnection()){
				String sql = "SELECT id, title FROM movies m WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;";
				PreparedStatement statement = conn.prepareStatement(sql);

				String[] words = query.split("\\s+");

				String fulltextQuery = Arrays.stream(words)
						.map(word -> "+" + word + "*")
						.collect(Collectors.joining(" "));

				System.out.println(fulltextQuery);

				statement.setString(1,fulltextQuery);
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					jsonArray.add(generateJsonObject(rs.getString("id"),rs.getString("title")));
				}
			}


			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String id, String title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", title);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("id", id);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
