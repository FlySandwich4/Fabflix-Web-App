import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.jasypt.util.password.StrongPasswordEncryptor;


public class MovieParser {

//    List<Employee> employees = new ArrayList<>();
    Document dom;
    HashMap<String,String> movieExistence = new HashMap<>();
    HashMap<String,Integer> genreExistence = new HashMap<>();

    HashSet<String> connectionExistence = new HashSet();

    int mv_id;
    int g_id;

    int mv_in=0, mv_dup=0, mv_no=0, mv_incon=0,mv_no_gen=0,gen_in_mv=0,dir_err=0,new_gen=0,relation_exist=0;

    public void runExample() throws Exception{

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        // Set up hashmaps for movies, genres, genres_in_movies
        setUpMovie(connection);
        setUpGenre(connection);
        setUpConnect(connection);


        connection.setAutoCommit(false);
        String query = "insert into movies (id, director, title, year) values(?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(query);

        String addGenre = "insert into genres (id, name) values(?,?)";
        String addMovieGen = "insert into genres_in_movies (movieId, genreId) values(?,?)";
        PreparedStatement addGenreState = connection.prepareStatement(addGenre);
        PreparedStatement addMovieGenState = connection.prepareStatement(addMovieGen);

        // parse the xml file and get the dom object
        parseXmlFile();

        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList directorFilmsList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < directorFilmsList.getLength(); i++) {

            try {
                Element directorFilmsElement = (Element) directorFilmsList.item(i);

                String directorName = getTextValue(directorFilmsElement, "dirname");
                System.out.println("[EXECUTE] ADDING movies from Director: "+directorName);
                if(directorName == null){
                    mv_incon++;
                    continue;
                }
                //System.out.println(directorName);
                NodeList films = directorFilmsElement.getElementsByTagName("film");
                for (int j = 0; j < films.getLength(); j++) {
                    try {
                        Element filmElement = (Element) films.item(j);

                        String title = getTextValue(filmElement, "t");
                        String year = getTextValue(filmElement, "year");
                        //System.out.println("    tt" + mv_id + "  |  " + title + "  |  " + year);

                        if(title == null || year == null){
                            mv_incon++;
                            continue;
                        }

                        if(movieExistence.containsKey(title)){
                            //System.out.println("    Movie: "+ title +"  already exist");
                            mv_dup++;
                            continue;
                        }else{
                            String mv_string_id = "tt" + mv_id;
                            while(mv_string_id.length() <9){
                                mv_string_id = mv_string_id.substring(0,2) + "0" + mv_string_id.substring(2);
                            }
                            movieExistence.put(title,mv_string_id);

                            statement.setString(1,movieExistence.get(title));
                            statement.setString(2,directorName);
                            statement.setString(3,title);
                            statement.setInt(4,Integer.parseInt(year));

                            statement.addBatch();
                            mv_in++;
                            mv_id++;
                        }

                        // Handle Genre Addition
                        Element cats = (Element) filmElement.getElementsByTagName("cats").item(0);
                        NodeList catList = cats.getElementsByTagName("cat");
                        for(int eachCat = 0; eachCat < catList.getLength(); eachCat++){
                            Element singleCat = (Element) catList.item(eachCat);
                            String catName = singleCat.getFirstChild().getNodeValue();
                            if(catName == null){
                                mv_no_gen++;
                                continue;
                            }
                            if(!genreExistence.containsKey(catName)){
                                genreExistence.put(catName,g_id);
                                //System.out.println("    Cat added: " + catName);

                                addGenreState.setInt(1,genreExistence.get(catName));
                                addGenreState.setString(2,catName);
                                addGenreState.addBatch();

                                new_gen++;
                                g_id++;
                            }
                            int catId = genreExistence.get(catName);
                            String movie_id = movieExistence.get(title);
                            if(!connectionExistence.contains(catId+"-"+ movie_id)){
                                connectionExistence.add(catId + "-" + movie_id);
                                addMovieGenState.setString(1,movieExistence.get(title));
                                addMovieGenState.setInt(2,genreExistence.get(catName));
                                addMovieGenState.addBatch();
                                gen_in_mv++;
                            }else {
                                relation_exist++;
                            }
                        }



                    }catch (Exception e){
                        //System.out.println("    filmElement ERROR: " + e);
                        mv_no++;
                        continue;
                    }
                }

            }catch (Exception e){
                //System.out.println("    directorFilmsElement ERROR: " + e);
                dir_err++;
                continue;
            }

        }

        movieExistence.clear();
        genreExistence.clear();
        connectionExistence.clear();
        // Start adding things to DB
        System.out.println("[EXECUTE]Adding Movie");
        statement.executeBatch();
        statement.close();
        System.out.println("[EXECUTE]Adding Genre");
        addGenreState.executeBatch();
        addGenreState.close();
        System.out.println("[EXECUTE]Adding Genre_Movie relationship");
        addMovieGenState.executeBatch();
        addMovieGenState.close();
        connection.commit();
        connection.close();
        System.out.println("\n[RESULT]");
        System.out.println("  |  [Movies]");
        System.out.println("  |    |  Movies inserted: "+mv_in);
        System.out.println("  |    |  Duplicate movies: "+mv_dup);
        System.out.println("  |    |  Movies err: "+ mv_no);
        System.out.println("  |    |  Movies without related info in xml: "+mv_incon);
        System.out.println("  |  ");
        System.out.println("  |  [Relation]");
        System.out.println("  |    |  Genres_in_movies inserted: "+gen_in_mv);
        System.out.println("  |    |  already exist relationship: "+relation_exist);
        System.out.println("  |    |  Movie without genre: "+mv_no_gen);
        System.out.println("  |    |  new Genres: "+new_gen);
        System.out.println("  |  ");
        System.out.println("[END]");

    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }


    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            textVal = nodeList.item(0).getFirstChild().getNodeValue();

        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void setUpMovie(Connection conn) throws Exception {
        String query = "SELECT * FROM movies ORDER BY id DESC";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        if(rs.next()){
            mv_id = Integer.parseInt(rs.getString("id").substring(2))+1;
            movieExistence.put(rs.getString("title"),rs.getString("id"));
            //System.out.println(mv_id);
        }
        while(rs.next()){
            movieExistence.put(rs.getString("title"),rs.getString("id"));
        }
        s.close();
        rs.close();
    }

    private void setUpGenre(Connection conn) throws Exception {
        String query = "SELECT * FROM genres ORDER BY id DESC";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        if(rs.next()){
            g_id = rs.getInt("id") + 1;
            genreExistence.put(rs.getString("name"),rs.getInt("id"));
            //System.out.println(g_id);
        }
        while(rs.next()){
            genreExistence.put(rs.getString("name"),rs.getInt("id"));
        }
        s.close();
        rs.close();
    }

    private void setUpConnect(Connection conn) throws Exception {
        String query = "SELECT * FROM genres_in_movies";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        while(rs.next()){
            connectionExistence.add(rs.getInt("genreId") +"-" + rs.getString( "movieId") );
        }
        s.close();
        rs.close();
    }

    public static void main(String[] args) {
        // create an instance
        MovieParser domParserExample = new MovieParser();

        // call run example
        try{
            domParserExample.runExample();
        }catch (Exception e){
            System.out.println(e);
        }

    }

    //    private void parseDocument() {
//        // get the document root Element
//        Element documentElement = dom.getDocumentElement();
//
//        // get a nodelist of employee Elements, parse each into Employee object
//        NodeList nodeList = documentElement.getElementsByTagName("Employee");
//        for (int i = 0; i < nodeList.getLength(); i++) {
//
//            // get the employee element
//            Element element = (Element) nodeList.item(i);
//
//            // get the Employee object
//            parseEmployee(element);
//
//            // add it to list
////            employees.add(employee);
//        }
//    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
//    private void parseEmployee(Element element) {
//
//        // for each <employee> element get text or int values of
//        // name ,id, age and name
//        String name = getTextValue(element, "Name");
//        int id = getIntValue(element, "Id");
//        int age = getIntValue(element, "Age");
//        String type = element.getAttribute("type");
//
//        // create a new Employee with the value read from the xml nodes
////        return new Employee(name, id, age, type);
//    }

}
