let htmlBody = $("#htmlBody")
let messageDiv = $("#messageDiv")

function displayAddStar(){
    htmlBody.empty()
    messageDiv.empty()
    let addStarHtml = "<h2>Insert Star</h2>\n" +
        "    <form id=\"insert-star\" method=\"post\" action=\"#\">\n" +
        "        <label><b>Name</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"name\" placeholder=\"Enter Star Name\" >\n" +
        "        </label>\n" +
        "        <br>\n" +
        "        <label><b>Birth Year</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"birth-year\" placeholder=\"Enter Birth Year\" >\n" +
        "        </label>\n" +
        "        <br>\n" +
        "        <input type=\"submit\" value=\"Add Star\">\n" +
        "    </form>"

    htmlBody.append(addStarHtml)
    let addStarFrom = $("#insert-star")
    addStarFrom.submit(submitAddStarForm)
}

function submitAddStarForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    let addStarFrom = $("#insert-star")
    console.log(addStarFrom.serialize())
    $.ajax(
        "api/addstar?type=addStar&", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: addStarFrom.serialize(),
            success: data=>{
                console.log(data)
                let messageDiv = $("#messageDiv")
                messageDiv.empty();
                if(data["success"] === "yes"){
                    messageDiv.append(data["message"])
                }else{
                    messageDiv.append("!!! ERROR ADDING NEW STAR !!!")
                }
            }
        }
    );
}



// ===============================================================
// Display meta data

function displayMeta(data){
    console.log(data)
    htmlBody.empty()
    messageDiv.empty()
    metaHtml = ""

    for(let key in data){
        console.log(key)
        metaHtml += "<br>" +
            "<div>" +
            "   <h2>Table: "+ key + "</h2>"
            for(let i = 0; i < data[key].length; i++){
                metaHtml +=
                    "<div>&nbsp;&nbsp;&nbsp;&nbsp;" + data[key][i]["name"] + " : " + data[key][i]["type"] + "</div>"
            }
        metaHtml +=
            "</div><br>"
    }
    htmlBody.append(metaHtml)

}

function requestMeta(){
    $.ajax("api/addstar",{
        method:"get",
        success: data=>displayMeta(data)
    })
}


//


// ===============================================================
// Display Add Movie
function displayAddMovie(){
    htmlBody.empty()
    messageDiv.empty()

    let addStarHtml = "<h2>Add Movie</h2>\n" +
        "    <form id=\"add-movie\" method=\"post\" action=\"#\">\n" +

        "        <label><b>Title</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"title\" placeholder=\"Enter Title\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <label><b>Year</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"year\" placeholder=\"Enter Year\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <label><b>Director</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"director\" placeholder=\"Enter director\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <label><b>Star Name</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"star-name\" placeholder=\"Enter Star Name\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <label><b>Star Year</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"star-year\" placeholder=\"Enter Star Year\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <label><b>Genre</b></label>\n" +
        "        <label>\n" +
        "            <input name=\"genre\" placeholder=\"Enter Genre\" >\n" +
        "        </label>\n" +
        "        <br>\n" +

        "        <input type=\"submit\" value=\"Add Movie\">\n" +
        "    </form>"

    htmlBody.append(addStarHtml)
    let addMovieForm = $("#add-movie")
    addMovieForm.submit(submitAddMovieForm)
}

function submitAddMovieForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    let addMovieForm = $("#add-movie")
    console.log(addMovieForm.serialize())
    $.ajax(
        "api/addstar?type=addMovie&", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: addMovieForm.serialize(),
            success: data=>{
                console.log(data)

                messageDiv.empty();
                if(data["success"] === "yes"){
                    messageDiv.append("Successfully added new Movie\n")
                    messageDiv.append(data["message"])
                }else{
                    messageDiv.append(data["message"])
                }
            }
        }
    );
}