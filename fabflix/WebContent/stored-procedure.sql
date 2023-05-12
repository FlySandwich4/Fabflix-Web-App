
-- USE moviedb;

-- DROP PROCEDURE IF EXISTS add_movie;

CREATE PROCEDURE add_movie (
    IN mv_title VARCHAR(100),
    IN mv_year INT,
    IN mv_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN star_year INT,
    IN genre_name VARCHAR(100)
)
BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE mv_id VARCHAR(10);

    DECLARE maxStar VARCHAR(10);
    DECLARE maxGenre INT;
    DECLARE maxMovie VARCHAR(10);



    -- Add star and add to stars_in_movies
    SELECT MAX(id) INTO maxStar FROM stars;
    SET star_id = CONCAT('nm', LPAD(CAST(SUBSTRING(maxStar, 3) AS UNSIGNED) + 1, 7, '0'));
    INSERT INTO stars(id, name, birthYear) VALUES (star_id, star_name,star_year);



    -- Add genre if it is not existed
    SELECT id INTO genre_id FROM genres WHERE name = genre_name;
    IF genre_id IS NULL THEN
        SELECT MAX(id) INTO maxGenre FROM genres;
        SET genre_id = maxGenre + 1;
        INSERT INTO genres(id, name) VALUES (genre_id, genre_name);
    END IF;

    -- Add new movie to the database
    SELECT id INTO mv_id FROM movies WHERE title = mv_title AND director = mv_director AND year = mv_year;
    IF mv_id is NULL THEN
        SELECT MAX(id) INTO maxMovie FROM movies;
        SET mv_id = CONCAT('tt', LPAD(CAST(SUBSTRING(maxMovie, 3) AS UNSIGNED) + 1, 7, '0'));
        INSERT INTO movies(id, title, director, year) VALUES (mv_id, mv_title, mv_director, mv_year);
    END IF;

    INSERT INTO stars_in_movies(starId, movieId) VALUES (star_id, mv_id);
    IF NOT EXISTS (SELECT * FROM genres_in_movies WHERE genreId = genre_id AND movieId = mv_id) THEN
        INSERT INTO genres_in_movies(genreId, movieId) VALUES (genre_id, mv_id);
    END IF;

END;