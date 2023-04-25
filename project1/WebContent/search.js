let searchForm = $("#searchForm");

function handleSearchResult(searchResult) {
    console.log(searchResult);

    // If login succeeds, it will redirect the user to index.html
    if (searchResult["status"] === "success") {
        window.location.replace("index.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(searchResult["errorMessage"]);
        $("#errorMessage").text(searchResult["errorMessage"]);
    }
}

function submitSearch(formGetEvent){
    formGetEvent.preventDefault()
    $.ajax("api/search", {
            method: "GET",
            data: searchForm.serialize(),
            success: result => handleSearchResult(result)
        }
    );
}

searchForm.submit(submitSearch)


