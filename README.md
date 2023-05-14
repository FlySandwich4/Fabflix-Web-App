# s23-122b-flysandwich
s23-122b-flysandwich created by GitHub Classroom

# CURRENT LINK OF THE WEBSITE #

http://54.183.234.77:8080/fabflix/

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
       
    
    
