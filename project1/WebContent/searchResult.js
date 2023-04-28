let searchForm = $("#searchForm");

function handleSearchResult(searchResult) {
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
        for(let i=0; i < searchResult.length; i++){
            rowHTML = ""
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
                searchResult[i]["year"]+"</div>"+
                "<div class='yue-movie-row-item'><span class='yue-deco'>Director</span> :" +
                searchResult[i]["director"]+"</div>"+
                "<div class='yue-movie-row-item'><span class='yue-deco'>Rating</span> :" +
                searchResult[i]["rating"]+"</div>"+
                "</div>" +
                "<div class='yue-seperate-line'></div>" +
                // 3 genres
                "<div class='yue-one-to-more'>" +
                "<div class='yue-one yue-deco'>First 3 Genres: " +
                "</div>" +
                "<div class='yue-movie-row yue-start-left-flex'>"
            for(let j=0; j<Math.min(searchResult[i]["genres"].length, 3);j++){
                rowHTML +=      "<div class='yue-movie-row-item'>" +
                    searchResult[i]["genres"][j]["name"]+"</div>"
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
            for(let j=0; j<Math.min(searchResult[i]["stars"].length, 3);j++){
                rowHTML +=      "<div class='yue-movie-row-item yue-star-link'><a href='single-star.html?id=" +
                    searchResult[i]["stars"][j]["id"]+"'>" +
                    searchResult[i]["stars"][j]["name"]+"</a></div>"
            }
            rowHTML +=
                "</div>" +
                "</div>"+
                "</div>"+
                "</div>"

            searchResultDiv.append(rowHTML);
        }
    }
}




function sortResult(){

}

function submitSearch(formGetEvent){
    formGetEvent.preventDefault()
    let url = `api/searchResult?search=search&limit=${$("#limit").val()}&${searchForm.serialize()}`
    $.ajax(url, {
            method: "GET",
            success: result => handleSearchResult(result)
        }
    );
}

function submitSortSearch(){
    let sort = $("#sort");
    console.log(sort.val())
    let searchFormData = searchForm.serialize();
    let sortValue = sort.val();
    let url = `api/searchResult?sort=${sortValue}`;
    console.log(url)
    $.ajax(url, {
        method: "GET",
        success: result => handleSearchResult(result)
    });
}

function submitLimitSearch(){
    let sort = $("#sort")
    let limit = $("#limit")
    let page = $("#page")
    console.log(sort.val())
    console.log(limit.val())
    let searchFormData = searchForm.serialize();
    let url = `api/searchResult?sort=${sort.val()}&limit=${limit.val()}`;
    console.log(url)

    $.ajax(url, {
        method: "GET",
        success: result => handleSearchResult(result)
    });
}





// Some COde directly run
searchForm.submit(submitSearch)


