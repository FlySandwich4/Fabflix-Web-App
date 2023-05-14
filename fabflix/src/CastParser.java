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

    int s_id;

    int star_in=0, star_dup=0,star_not_found=0,mv_no_star=0,star_no_dob=0,star_no_name=0;
    int star_relation_in=0, relation_no_enough=0,relation_exist=0;


    public void runExample() throws Exception{

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        connection.setAutoCommit(false);

        setUpMovie(connection);
        setUpStar(connection);
        setUpConnect(connection);
        parseXmlFile();

        PreparedStatement addStarState = addStar(connection);
        PreparedStatement addRelationState = addRelation(connection);

        try{
            addStarState.executeBatch();
            addRelationState.executeBatch();
            System.out.println("[SUCCESS]Finished executing Batches");
            connection.commit();
            connection.close();
        }catch (Exception e){
            System.out.println("[ERROR]EXECUTE BATCH ERROR: "+ e);
        }finally {
            System.out.println("\n[RESULT]");
            System.out.println("  |  [Stars]");
            System.out.println("  |    |  Stars inserted: "+star_in);
            System.out.println("  |    |  Duplicate stars: "+star_dup);
            System.out.println("  |    |  Stars not found(xml or other issue): "+star_not_found);
            System.out.println("  |    |  Stars without Birth Year: "+star_no_dob);
            System.out.println("  |    |  Stars without Name: "+star_no_name);
            System.out.println("  |  ");
            System.out.println("  |  [Relation]");
            System.out.println("  |    |  Stars_in_movies inserted: "+star_relation_in);
            System.out.println("  |    |  Movie without star: "+mv_no_star);
            System.out.println("  |    |  Relation Information not Enough: "+relation_no_enough);
            System.out.println("  |    |  Relation Already Exist: "+relation_exist);
            System.out.println("  |  ");
            System.out.println("[END]");

        }


    }

    public PreparedStatement addStar(Connection conn) throws Exception{
        String addStarQuery = "insert into stars (id,name,birthYear) values (?,?,?)";
        PreparedStatement addStarState = conn.prepareStatement(addStarQuery);

        Element starDoc = starDom.getDocumentElement();
        NodeList starList = starDoc.getElementsByTagName("actor");
        for(int eachStarI = 0; eachStarI < starList.getLength(); eachStarI++){
            try{
                //System.out.println("\n[START]Adding actor index: " + eachStarI);
                Element starEle = (Element) starList.item(eachStarI);
                String name = null;
                try {
                    name = getTextValue(starEle, "stagename");
                }catch (Exception e){
//                    System.out.println("  |  [ERROR]name in XML might be error, skip adding");
                    star_no_name++;
                    continue;
                }
                String birthYear;

                // Check if name is null
                if(name==null){
                    //System.out.println("  |  [Warning]Star name is null, skip adding");
                    star_no_name++;
                    continue;
                }

                // Check if starList already contains this star
                if(starExistence.containsKey(name)){
                    //System.out.println("  |  [Warning]Star: " + name + " already exist, skip adding");
                    star_dup++;
                    continue;
                }

                // Try to get the birthYear of star, handle null result
                try{
                    birthYear = getTextValue(starEle, "dob");
                }catch (Exception e){
                    //System.out.println("  |  [Warning]Star: "+name+" doesn't have birthday year, insert as null");
                    star_no_dob++;
                    birthYear = null;
                }

                // Adding record to hashmap, for later star checking
                String star_string_id = "nm" + s_id;
                while(star_string_id.length() <9){
                    star_string_id = star_string_id.substring(0,2) + "0" + star_string_id.substring(2);
                }
                starExistence.put(name,star_string_id);

                // Setting prepared statements, add to batch
                addStarState.setString(1,starExistence.get(name));
                addStarState.setString(2,name);
                if(birthYear == null){
                    addStarState.setNull(3, Types.INTEGER);
                }else{
                    addStarState.setInt(3,Integer.parseInt(birthYear));
                }

                addStarState.addBatch();
                s_id++;
                star_in++;

            }catch (Exception e){
                //System.out.println("\n  |  [ERROR]Add Star Error: " + e + "\n");
                star_not_found++;
                continue;
            }
//            finally {
//                System.out.println("[END]");
//            }
        }
        return addStarState;

    }

    public PreparedStatement addRelation(Connection conn) throws Exception{
        String addConnection = "insert into stars_in_movies (movieId,starId) values (?,?)";
        PreparedStatement addRelationState = conn.prepareStatement(addConnection);

        //Test
        int a = 0;

        Element castDoc = castDom.getDocumentElement();
        NodeList relationList = castDoc.getElementsByTagName("m");
        //System.out.println("   NumOfRelation in Star:"+relationList.getLength());
        for(int eachRelI = 0; eachRelI < relationList.getLength(); eachRelI++){
            //System.out.println("\n[START]Adding actor-movie relationship index: " + eachRelI);
            Element eachM = (Element) relationList.item(eachRelI);

            String title=null, star=null;
            try{
                title = getTextValue(eachM,"t");
                star = getTextValue(eachM,"a");
                //System.out.println("  |  [Log]Adding title:"+title +" star:"+star);
            }catch (Exception e){
                // System.out.println("  |  [ERROR]Title or Star in XML might be error, skip adding");
                relation_no_enough++;
                continue;
            }
            if(title == null || star == null){
                //System.out.println("  |  [Warning]Title or Star might be null, skip adding");
                mv_no_star++;
                continue;
            }


            String movieId = movieExistence.get(title);
            String starId = starExistence.get(star);
            String s_mId = starId+"-"+movieId;

            if(starId==null || movieId==null){
                //System.out.println("  |  [Warning]StarId or MovieId: " +starId + ", " + movieId+" is null, skip adding");
                relation_no_enough++;
                continue;
            }
            if(connectionExistence.contains(s_mId)){
                //System.out.println("  |  [Warning]Relation: " +s_mId+" already exist, skip adding");
                relation_exist++;
                continue;
            }

            connectionExistence.add(s_mId);

            addRelationState.setString(1,movieId);
            addRelationState.setString(2,starId);
            addRelationState.addBatch();
            a++;
            System.out.println(a);
            star_relation_in++;
        }
        return addRelationState;
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

        while(rs.next()){
            movieExistence.put(rs.getString("title"),rs.getString("id"));
        }
        s.close();
        rs.close();

    }

    private void setUpStar(Connection conn) throws Exception {
        String query = "SELECT * FROM stars ORDER BY id DESC";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        if(rs.next()){
            s_id = Integer.parseInt(rs.getString("id").substring(2))+ 1;
            starExistence.put(rs.getString("name"),rs.getString("id"));
            //System.out.println(s_id);
        }
        while(rs.next()){
            starExistence.put(rs.getString("name"),rs.getString("id"));
        }
        s.close();
        rs.close();
    }

    private void setUpConnect(Connection conn) throws Exception {
        String query = "SELECT * FROM stars_in_movies";
        PreparedStatement s = conn.prepareStatement(query);
        ResultSet rs = s.executeQuery();

        while(rs.next()){
            connectionExistence.add(rs.getString("starId") +"-" + rs.getString( "movieId") );
        }
        s.close();
        rs.close();
    }



    public static void main(String[] args) {
        // create an instance
        CastParser domParserExample = new CastParser();

        // call run example
        try{
            domParserExample.runExample();
        }catch (Exception e){
            System.out.println(e);
        }

    }

}
