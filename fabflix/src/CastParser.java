import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jasypt.util.password.StrongPasswordEncryptor;


public class CastParser {

    //    List<Employee> employees = new ArrayList<>();
    Document castDom, starDom;
    HashMap<String,String> starExistence = new HashMap<>();
    HashMap<String,String> movieExistence = new HashMap<>();
    HashSet<String> connectionExistence = new HashSet<>();

    int mv_id;
    int s_id;


    public void runExample() throws Exception{

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        setUpMovie(connection);
        setUpStar(connection);
        setUpConnect(connection);
        parseXmlFile();


    }

    public void addStar(Connection conn) throws Exception{
        String addStarQuery = "insert into stars (id,name,birthYear) values (?,?,?)";
        PreparedStatement addStarState = conn.prepareStatement(addStarQuery);

        Element starDoc = starDom.getDocumentElement();
        NodeList

    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            castDom = documentBuilder.parse("casts124.xml");
            starDom = documentBuilder.parse("actors63.xml");


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


    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    private void setUpMovie(Connection conn) throws Exception {
        String query = "SELECT * FROM movies ORDER BY id DESC";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        if(rs.next()){
            mv_id = Integer.parseInt(rs.getString("id").substring(2))+1;
            movieExistence.put(rs.getString("title"),rs.getString("id"));
            System.out.println(mv_id);
        }
        while(rs.next()){
            movieExistence.put(rs.getString("title"),rs.getString("id"));
        }

    }

    private void setUpStar(Connection conn) throws Exception {
        String query = "SELECT * FROM stars ORDER BY id DESC";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        if(rs.next()){
            s_id = Integer.parseInt(rs.getString("id"))+ 1;
            starExistence.put(rs.getString("name"),rs.getString("id"));
            System.out.println(s_id);
        }
        while(rs.next()){
            starExistence.put(rs.getString("name"),rs.getString("id"));
        }
    }

    private void setUpConnect(Connection conn) throws Exception {
        String query = "SELECT * FROM stars_in_movies";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        while(rs.next()){
            connectionExistence.add(rs.getInt("starId") +"-" + rs.getString( "movieId") );
        }
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

}
