## CS 122B Project 1 API example

This example shows how frontend and backend are separated by implementing a star list page, and a single star page with movie list.

### Before running the example

#### If you do not have USER `mytestuser` setup in MySQL, follow the below steps to create it:

 - login to mysql as a root user 
    ```
    local> mysql -u root -p
    ```

 - create a test user and grant privileges:
    ```
    mysql> CREATE USER 'mytestuser'@'localhost' IDENTIFIED BY 'My6$Password';
    mysql> GRANT ALL PRIVILEGES ON * . * TO 'mytestuser'@'localhost';
    mysql> quit;
    ```

#### prepare the database `moviedbexample`
 

```
local> mysql -u mytestuser -p
mysql> CREATE DATABASE IF NOT EXISTS moviedbexample;
mysql> USE moviedbexample;
mysql> CREATE TABLE IF NOT EXISTS stars(
               id varchar(10) primary key,
               name varchar(100) not null,
               birthYear integer
           );

mysql> INSERT IGNORE INTO stars VALUES('755011', 'Arnold Schwarzeneggar', 1947);
mysql> INSERT IGNORE INTO stars VALUES('755017', 'Eddie Murphy', 1961);

mysql> CREATE TABLE if not exists movies(
       	    id VARCHAR(10) DEFAULT '',
       	    title VARCHAR(100) DEFAULT '',
       	    year INTEGER NOT NULL,
       	    director VARCHAR(100) DEFAULT '',
       	    PRIMARY KEY (id)
       );

mysql> INSERT IGNORE INTO movies VALUES('1111', 'The Terminator', 1984, 'James Cameron');
mysql> INSERT IGNORE INTO movies VALUES('2222', 'Coming To America', 1988, 'John Landis');

mysql> CREATE TABLE IF NOT EXISTS stars_in_movies(
       	    starId VARCHAR(10) DEFAULT '',
       	    movieId VARCHAR(10) DEFAULT '',
       	    FOREIGN KEY (starId) REFERENCES stars(id),
       	    FOREIGN KEY (movieId) REFERENCES movies(id)
       );

mysql> INSERT IGNORE INTO stars_in_movies VALUES('755017', '2222');
mysql> INSERT IGNORE INTO stars_in_movies VALUES('755011', '1111');
mysql> quit;
```

### To run this example: 
1. Clone this repository using `git clone`
2. Open IntelliJ -> Import Project -> Choose the project you just cloned (The root path must contain the pom.xml!) -> Choose Import project from external model -> choose Maven -> Click on Finish -> The IntelliJ will load automatically
3. For "Root Directory", right click "cs122b-project1-api-example" -> Mark Directory as -> sources root
4. In `WebContent/META-INF/context.xml`, make sure the mysql username is `mytestuser` and password is `mypassword`.
5. Also make sure you have the `moviedbexample` database.
6. To run the example, follow the instructions in canvas.

### Brief Explanation
- `StarsServlet.java` is a Java servlet that talks to the database and get the stars. It returns a list of stars in the JSON format. 
The name of star is generated as a link to Single Star page.

- `index.js` is the main Javascript file that initiates an HTTP GET request to the `StarsServlet`. After the response is returned, `index.js` populates the table using the data it gets.

- `index.html` is the main HTML file that imports jQuery, Bootstrap, and `index.js`. It also contains the initial skeleton for the table.

- `SingleStarServlet.java` is a Java servlet that talks to the database and get information about one Star and all the movie this Star performed. It returns a list of Movies in the JSON format. 

- `single-star.js` is the Javascript file that initiates an HTTP GET request to the `SingleStarServlet`. After the response is returned, `single-star.js` populates the table using the data it gets.

- `single-star.html` is the HTML file that imports jQuery, Bootstrap, and `single-star.js`. It also contains the initial skeleton for the movies table.

### Separating frontend and backend
- For project 1, you are recommended to separate frontend and backend. Backend Java Servlet only provides API in JSON format. Frontend Javascript code fetches the data through HTTP (ajax) requests and then display the data on the webpage. 

- This example uses `jQuery` for making HTTP requests and manipulate DOM. jQuery is relatively easy to learn compared to other frameworks. This example also includes `Bootstrap`, a popular UI framework to let you easily make your webpage look fancy. 


### DataSource
- For project 1, you are recommended to use tomcat to manage your DataSource instead of manually define MySQL connection in each of the servlet.

- `WebContent/META-INF/context.xml` contains a DataSource, with database information stored in it.
`WEB-INF/web.xml` registers the DataSource to name jdbc/moviedbexample, which could be referred to anywhere in the project.

- In both `SingleStarServlet.java` and `StarsServlet.java`, a private DataSource reference dataSource is created with `@Resource` annotation. It is a reference to the DataSource `jdbc/moviedbexample` we registered in `web.xml`

- To use DataSource, you can create a new connection to it by `dataSource.getConnection()`, and you can use the connection as previous examples.
