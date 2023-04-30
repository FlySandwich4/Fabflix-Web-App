let searchResultDiv = jQuery("#searchResult");

searchResultDiv.empty()

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

function handleGenreList(genreList){
    console.log(genreList)
    let innerHtml = ""
    for(let i=0; i<genreList.length;i++){
        innerHtml += "<div><a href='#' onclick='submitGenreSearch("+
            genreList[i]["id"] +")'> " +
            genreList[i]["name"] +
            " </a></div>"
    }

    innerHtml += "<div><a href='#' onclick='submitLetterSearch(\"*\")'> " +
        "*" +
        " </a></div>"

    for(let i = 0; i < 10; i++){
        innerHtml += "<div><a href='#' onclick='submitLetterSearch(\""+
            i+"\")'> " +
            i +
            " </a></div>"
    }

    for(let charI=97; charI<=122; charI++){
        innerHtml += "<div><a href='#' onclick='submitLetterSearch(\""+
            String.fromCharCode(charI)+"\")'> " +
            String.fromCharCode(charI) +
            " </a></div>"
    }
    searchResultDiv.append(innerHtml)
}

function handleGenreRequest(searchResult){

}
function submitGenreSearch(genreId){
    let sort = $("#sort").val()
    let limit = $("#limit").val()
    let page = $("#page").val()
    if(page === undefined || page === null){
        page = 1;
    }
    let url = `api/searchResult?sort=${sort}&limit=${limit}&page=${page}&search=genre&genre=${genreId}`
    console.log(url)
    $.ajax(url,{
        method : "Get",
        success : result=>handleSearchResult(result)
    })
}



function submitLetterSearch(letter){
    let sort = $("#sort").val()
    let limit = $("#limit").val()
    let page = $("#page").val()
    if(page === undefined || page === null){
        page = 1;
    }
    let url = `api/searchResult?sort=${sort}&limit=${limit}&page=${page}&search=letter&letter=${letter}`
    console.log(url)
    $.ajax(url,{
        method : "Get",
        success : result=>handleSearchResult(result)
    })
}

let back = getParameterByName("back")
let searchType = getParameterByName("search")
if(back === null || back === undefined){
    $.ajax("api/searchInit",{
        method: "Get",
        success: result => handleGenreList(result)
    })
}else{

    if(searchType===null || searchType===undefined){

        $.ajax("api/searchResult?back=1",{
            method: "Get",
            success: result => handleSearchResult(result)
        })
    }else if(searchType==="genre"){
        submitGenreSearch(getParameterByName("genre"))
    }
}
