let searchForm = $("#searchForm");
let pages;
let current;

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
        for(let i=1; i < searchResult.length; i++){
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
                rowHTML +=      "<div class='yue-movie-row-item yue-star-link'>" +
                                    "<a href='#' onclick='submitGenreSearch("+
                                    searchResult[i]["genres"][j]["id"] + ")'>" +
                                    searchResult[i]["genres"][j]["name"]+"</a></div>"
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
            for(let j=0; j<Math.min(searchResult[i]["stars"].length, 3);j++){
                rowHTML +=      "<div class='yue-movie-row-item yue-star-link'><a href='single-star.html?id=" +
                                searchResult[i]["stars"][j]["id"]+"'>" +
                                searchResult[i]["stars"][j]["name"]+"</a></div>"
            }
            rowHTML +=
                        "</div>" +
                    "</div>"+
                "<div class='yue-seperate-line'></div>" +
                "<div class='yue-add-cart'><a href='#' onclick='submitCartAdd(\""+
                searchResult[i]["id"] + "\",\"" + searchResult[i]["title"]
                +"\")'>Add to cart</a></div>" +
                "</div>"+
            "</div>"

            searchResultDiv.append(rowHTML);
        }

        /**
         * Page buttons part
         */

        pages = Math.floor(searchResult[0]["count"]/searchResult[0]["limit"])
        current = searchResult[0]["current"]
        if (searchResult[0]["count"] % searchResult[0]["limit"] > 0){
            pages += 1
        }
        let pageButtons = "<div class='element-list'>"
        pageButtons += "<a href='#' class='element-page' onclick='submitPageSearch("+ (current-1) +")'>"+
            " Previous </a>"
        for(let i = 1; i <= pages; i++){
            if (i === searchResult[0]["current"]){
                pageButtons += "<a class='element-selected' href='#' style='background-color: #67b767' onclick='submitPageSearch("+ i +")'> "+
                    i + " </a>"
            }else{
                pageButtons += "<a class='element-page' href='#' onclick='submitPageSearch("+ i +")'> "+
                    i + " </a>"
            }
        }
        pageButtons += "<a href='#' class='element-page' onclick='submitPageSearch("+ (current+1) +")'>"+
            " Next </a>"
        pageButtons += "</div>"
        document.getElementById("sort").selectedIndex= parseInt(searchResult[0]["sortSelect"])
        document.getElementById("limit").selectedIndex= parseInt(searchResult[0]["limitSelect"])
        searchResultDiv.append(pageButtons)
    }
}




function sortResult(){

}

function submitSearch(formGetEvent){
    formGetEvent.preventDefault()
    let sort = $("#sort").val()
    let limit = $("#limit").val()
    let url = `api/searchResult?search=search&limit=${limit}&sort=${sort}&page=1`
    $.ajax(url, {
            method: "GET",
            data: searchForm.serialize(),
            success: result => handleSearchResult(result)
        }
    );
}


function submitLimitSearch(){
    let sort = $("#sort")
    let limit = $("#limit")
    let url = `api/searchResult?sort=${sort.val()}&limit=${limit.val()}&page=1`;
    console.log(url)

    $.ajax(url, {
        method: "GET",
        success: result => handleSearchResult(result)
    });
}

function submitPageSearch(page){
    let sort = $("#sort")
    let limit = $("#limit")
    let url = `api/searchResult?sort=${sort.val()}&limit=${limit.val()}&page=${page}`;
    console.log(url)
    if(page > 0 && page <= pages){
        $.ajax(url, {
            method: "GET",
            success: result => handleSearchResult(result)
        });
    }else if(page <= 0){
        alert("No more previous pages!")
    }else{
        alert("No more next pages!")
    }

}





// Some COde directly run
searchForm.submit(submitSearch)


