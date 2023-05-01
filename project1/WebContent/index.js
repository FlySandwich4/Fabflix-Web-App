/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML +=
            "<div class='yue-card-continer'>" +
                "<div class='yue-card'>" +
                    "<div class='yue-movie-title'>" +
                        '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                        + resultData[i]["movie_title"] +     // display star_name for the link text
                        '</a>' +
                    "</div>" +
                    "<div class='yue-movie-row'>" +
                        "<div class='yue-movie-row-item'><span class='yue-deco'>Year</span> :" +
                        resultData[i]["movie_year"]+"</div>"+
                        "<div class='yue-movie-row-item'><span class='yue-deco'>Director</span> :" +
                        resultData[i]["movie_director"]+"</div>"+
                        "<div class='yue-movie-row-item'><span class='yue-deco'>Rating</span> :" +
                        resultData[i]["movie_rating"]+"</div>"+
                    "</div>" +
                    "<div class='yue-seperate-line'></div>" +
                    // 3 genres
                    "<div class='yue-one-to-more'>" +
                        "<div class='yue-one yue-deco'>First 3 Genres: " +
                        "</div>" +
                        "<div class='yue-movie-row yue-start-left-flex'>"
        for(let j=0; j<Math.min(resultData[i]["gen"].length, 3);j++){
            rowHTML +=      "<div class='yue-movie-row-item yue-star-link'>" +
                "<a href='search.html?back=1&search=genre&genre="+
                resultData[i]["gen"][j]["id"] + "'>" +
                resultData[i]["gen"][j]["name"]+"</a></div>"
        }
        rowHTML +=
                        "</div>" +
                    "</div>" +
                    "<div class='yue-seperate-line'></div>"

                    // 3 stars
        rowHTML +=  "<div class='yue-one-to-more'>" +
                        "<div class='yue-one yue-deco'>First 3 Stars: " +
                        "</div>" +
                        "<div class='yue-movie-row yue-start-left-flex'>"
        for(let j=0; j<Math.min(resultData[i]["star"].length, 3);j++){
            rowHTML +=      "<div class='yue-movie-row-item yue-star-link'><a href='single-star.html?id=" +
                            resultData[i]["star"][j]["id"]+"'>" +
                            resultData[i]["star"][j]["name"]+"</a></div>"
        }
                    rowHTML +=
                        "</div>" +
                    "</div>"+
                "<div class='yue-seperate-line'></div>" +
                "<div class='yue-add-cart'><a href='#' onclick='submitCartAdd(\""+
                resultData[i]["movie_id"] + "\",\"" + resultData[i]["movie_title"]
                +"\")'>Add to cart</a></div>" +
                "</div>"+
            "</div>"

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be 56o43executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


