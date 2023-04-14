/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    document.title = resultData["title"];
    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let content = jQuery("#content");
    // append two html <p> created to the h3 body, which will refresh the page
    let html_content = "";

    html_content +=
        "<div class='yue-content'><span class='yue-strong'>Movie Name</span> : "+ resultData["title"]+"</div>"+
        "<div class='yue-content'><span class='yue-strong'>Year</span> : "+ resultData["year"] +"</div>"+
        "<div class='yue-content'><span class='yue-strong'>Director</span> : "+ resultData["director"] +"</div>"+
        "<div class='yue-content'><span class='yue-strong'>Rating</span> : "+ resultData["rating"] +"</div>"

    content.append(html_content);

    let gen_html = jQuery("#genres");
    for(let i=0; i<resultData["gen"].length; i++){
        gen_html.append("<div class='yue-deco'>"+resultData["gen"][i]["name"]+"</div>")
    }

    let star_html = jQuery("#stars");
    for(let i=0; i<resultData["star"].length; i++){
        star_html.append("<div class='yue-star-link yue-deco'> <a href='single-star.html?id="+resultData["star"][i]["starId"] + "'>"
            +resultData["star"][i]["name"]+"</a></div>")
    }


}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});