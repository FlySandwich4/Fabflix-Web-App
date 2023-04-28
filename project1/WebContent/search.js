let searchResultDiv = jQuery("#searchResult");

searchResultDiv.empty()


function handleGenreList(genreList){
    console.log(genreList)
    let innerHtml = ""
    for(let i=0; i<genreList.length;i++){
        innerHtml += "<div><a href='#' onclick='submitGenreSearch("+
            genreList[i]["id"] +")'>" +
            genreList[i]["name"] +
            "</a></div>"
    }

    for(let charI=97; charI<=122; charI++){
        innerHtml += "<div><a href='#' onclick='submitLetterSearch(\""+
            String.fromCharCode(charI)+"\")'>" +
            String.fromCharCode(charI) +
            "</a></div>"
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


$.ajax("api/searchInit",{
    method: "Get",
    success: result => handleGenreList(result)
})