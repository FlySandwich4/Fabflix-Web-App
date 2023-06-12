# s23-122b-flysandwich
s23-122b-flysandwich created by GitHub Classroom

# CURRENT LINK OF THE WEBSITE #

https://3.101.28.218:8443/fabflix/

# PROJECT 5 #

- # General
    - #### Team#: FlySandwich
    
    - #### Names: Yue Wu
    
    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment: 
    - git clone repo
    - change to branch "HighPerform"
    - cd into fabflix folder
    - mvn package to build the war file
    - sudo cp ./target/*.war /var/lib/tomcat10/webapps/
    - web is online now

    - #### Collaborations and Work Distribution: Yue Wu did everything.


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - In WebContent/META-INF/context.xml I config the Pooling by changing url, maxTotal, maxIdle, and maxWaitMillis.
    
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - In every servlet, I use lookup for conneting to pooling, checking if there is a connection in the pool.
    - "dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master-moviedb");"
    
    - #### Explain how Connection Pooling works with two backend SQL.
    - The request will pick one of the existing resource in the pool
    - The request can establish a connection and this can be reuse in the future
    - The resource can be reuse, like some same request and same queries
    - If the pool is empty (no connection for new request) the new request will have to wait
    

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - BELOW FILES ARE IN src/
    - AndroidLogin
    - Cart
    - Conformation
    - Dashboard_addStar
    - EmployeeLoginServlet
    - FullTextSearch
    - HeroSuggestion
    - LoginServlet
    - MoviesServlet
    - Payment
    - SearchInit
    - SearchResultServlet
    - SingleMovieServlet
    - SingleStarServlet

    - #### How read/write requests were routed to Master/Slave SQL?
    - If a request is read, it will be randomly transfer to different databases. Like search, fulltextsearch, or login...
    - If a request is write, it will only go to master database, like payment, dashboard_addstar...
    - The master database is connected with the slave database, everytime master is changed, the slave changed.
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    - log_processing.py is in fabflix/performance/ 
    - This is a file that reads files from some path and return the avg of TS and TJ
    - The function is defined as: def calculate_average(filename)
    - This function can be generally used by inputing a filename parameter (which is the path)
    - The format of the file must be like:

Query: Forrest Gump
    TS : 1434851
    TJ : 1303391

Query: The Sixth Sense
    TS : 1572198
    TJ : 1439526

Query: Bruce Almighty
    TS : 1549232
    TJ : 1420076

Query: Batman Begins
    TS : 1457384
    TJ : 1330263

Query: Million Dollar Baby
    TS : 1193598
    TJ : 1120995



- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](fabflix/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

# PROJECT 4 Video Demo Link: #
https://youtu.be/J6qJtmFomug

# PROJECT 3 Video Demo Link: #
https://youtu.be/OAN3ERCI0m8

### procedure sql is in WebContent/ ###

       Prepared Statement filenames:
       - CastParser
       - Dashboard_addStar
       - EmployeeLoginServlet
       - LoginServlet
       - MovieParser
       - MoviesServlet
       - Payment
       - SearchInit
       - SearchResultServlet
       - SingleMovieServlet
       - SingleStarServlet
       
       Optimizing Strategies:
       - Using batch, addBatch(query) and executeBatch(), to combine all insert query to one
       - Using Hashmaps for duplicate checking, including mv_id, genres, stars, genres_in_movies, and stars_in_movies
       - Using ID record (int id=something) for assigning new id for mvs, genres, stars instead of querying MAX(id) every time
       
       I use Two files for Parsing XML:
       
       MovieParser.java (for main.xml)
       - For Movies and Genres and Genres_in_moives:
            [RESULT]
              |  [Movies]
              |    |  Movies inserted: 11040
              |    |  Duplicate movies: 1045
              |    |  Movies err: 27
              |    |  Movies without related info in xml: 7
              |  
              |  [Relation]
              |    |  Genres_in_movies inserted: 8959
              |    |  already exist relationship: 1
              |    |  Movie without genre: 1
              |    |  new Genres: 130
              |  
            [END]
            
       CastParser.java  (for casts.xml and actor.xml)
       - For Stars and Stars_in_movies
            [RESULT]
              |  [Stars]
              |    |  Stars inserted: 5972
              |    |  Duplicate stars: 855
              |    |  Stars not found(xml or other issue): 36
              |    |  Stars without Birth Year: 2143
              |    |  Stars without Name: 0
              |  
              |  [Relation]
              |    |  Stars_in_movies inserted: 31880
              |    |  Movie without star: 0
              |    |  Relation Information not Enough: 16632
              |    |  Relation Already Exist: 426
              |  
            [END]
       
    

# PROJECT 1 Video Demo Link: #
https://youtu.be/BRLU-ovfwyc

# PROJECT 2 Video Demo Link: #
https://youtu.be/5VlfRMb-SyM

    SubString Match Design

    For search:

    - Title:    "abc" matches "%abc%"
    - Star:     "tom" matches "%tom%"
    - Director: "tom" matches "%tom%"
    - Year      "2007" matches specific "2007", no vague search

    For search by character (a,b,c...):
    - "a" matches "a%" , case insensitive

    For search by genres (drama...) 
    - "drama" specificly matches "drama"

    For "*" in main page
    - Regular Expression searching all symbols (no letters or numbers)
    

    
