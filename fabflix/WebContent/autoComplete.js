/*
 * CS 122B Project 4. Autocomplete Example.
 *
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * This example implements the basic features of the autocomplete search, features that are
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */


/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "hero-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    window.location.href = `single-movie.html?id=${suggestion["data"]["id"]}`
    //console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["heroID"])
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        console.log(query)
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        console.log(suggestion)
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button


function handleSearchButton(){
    let url="api/fulltextsearch?query="
    let query = $('#autocomplete').val()
    $.ajax(url+query,{
        success: handleFullTextSearch
    })
}

function handleFullTextSearch(searchResult) {
    console.log(searchResult);
    // If login succeeds, it will redirect the user to index.html
    if (searchResult["status"] === "fail") {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(searchResult["errorMessage"]);
        $("#errorMessage").text(searchResult["errorMessage"]);
    } else {
        let searchResultDiv = jQuery("#searchResult");
        searchResultDiv.empty()
        for (let i = 0; i < searchResult.length; i++) {
            let rowHTML = ""
            rowHTML +=
                "<div class='yue-card-continer'>" +
                "<div class='yue-card'>" +
                "<div class='yue-movie-title'>" +
                '<a href="single-movie.html?id=' + searchResult[i]['id'] + '">'
                + searchResult[i]["title"] +     // display star_name for the link text
                '</a>' +
                "</div>" +
                "<div class='yue-movie-row'>" +
                "<div class='yue-movie-row-item'><span class='yue-deco'>Year</span> :" +
                searchResult[i]["year"] + "</div>" +
                "<div class='yue-movie-row-item'><span class='yue-deco'>Director</span> :" +
                searchResult[i]["director"] + "</div>" +
                "<div class='yue-movie-row-item'><span class='yue-deco'>Rating</span> :" +
                searchResult[i]["rating"] + "</div>" +
                "</div>" +
                "<div class='yue-seperate-line'></div>" +
                // 3 genres
                "<div class='yue-one-to-more'>" +
                "<div class='yue-one yue-deco'>First 3 Genres: " +
                "</div>" +
                "<div class='yue-movie-row yue-start-left-flex'>"
            for (let j = 0; j < Math.min(searchResult[i]["genres"].length, 3); j++) {
                rowHTML += "<div class='yue-movie-row-item yue-star-link'>" +
                    "<a href='#' onclick='submitGenreSearch(" +
                    searchResult[i]["genres"][j]["id"] + ")'>" +
                    searchResult[i]["genres"][j]["name"] + "</a></div>"
            }
            rowHTML +=
                "</div>" +

                "</div>" +
                "<div class='yue-seperate-line'></div>"

            // 3 stars
            rowHTML +=
                "<div class='yue-one-to-more'>" +
                "<div class='yue-one yue-deco'>First 3 Stars: " +
                "</div>" +
                "<div class='yue-movie-row yue-start-left-flex'>"
            for (let j = 0; j < Math.min(searchResult[i]["stars"].length, 3); j++) {
                rowHTML += "<div class='yue-movie-row-item yue-star-link'><a href='single-star.html?id=" +
                    searchResult[i]["stars"][j]["id"] + "'>" +
                    searchResult[i]["stars"][j]["name"] + "</a></div>"
            }
            rowHTML +=
                "</div>" +
                "</div>" +
                "<div class='yue-seperate-line'></div>" +
                "<div class='yue-add-cart'><a href='#' onclick='submitCartAdd(\"" +
                searchResult[i]["id"] + "\",\"" + searchResult[i]["title"]
                + "\")'>Add to cart</a></div>" +
                "</div>" +
                "</div>"

            searchResultDiv.append(rowHTML);
        }
    }
}

