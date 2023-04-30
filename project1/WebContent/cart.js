let cartBody = $("#cart-body")
function handleCartGet(data){
    console.log(data)
    cartBody.empty()
    cartBody.append("welcome to cart")
    let totalPrice = 0
    let appendHtml = "<div>"
    for(let key in data){
        totalPrice += 50 * parseInt(data[key]["num"])
        appendHtml += "<div>"+ key + " " + data[key]["num"]+" " + data[key]["name"] + (50 * parseInt(data[key]["num"])) + "</div>"
        + "<a href='#' onclick='submitCartIncrement(\"" + key +  "\")'> +1 to cart </a>" +
            "<a href='#' onclick='submitCartDecrement(\"" + key +  "\")'> -1 to cart </a>" +
            "<a href='#' onclick='submitCartDelete(\"" + key +  "\")'> Delete this movie </a>"
    }
    appendHtml+="</div>"
    appendHtml+="<h1> Total Price: $"+totalPrice+"</h1>"
    cartBody.append(appendHtml)

}

function submitCartAdd(id,name){
    $.ajax("api/cart",{
        method: "POST",
        data: {"movieId":id, "name":name},
        success: data => {
            console.log("Post added: " + data)
            window.alert("Movie Added to Cart!")
        }
    })
}

function submitCartIncrement(id){
    $.ajax("api/cart",{
        method: "GET",
        data: {"movieId":id,"updateType":"increment"},
        success: data => {
            console.log("Increment added: " + data)
            handleCartGet(data)
        }
    })
}

function submitCartDecrement(id){
    $.ajax("api/cart",{
        method: "GET",
        data: {"movieId":id,"updateType":"decrement"},
        success: data => {
            console.log("Decrement added: " + data)
            handleCartGet(data)
        }
    })
}


function submitCartDelete(id){
    $.ajax("api/cart",{
        method: "GET",
        data: {"movieId":id,"updateType":"delete"},
        success: data => {
            console.log("Deleted: " + data)
            handleCartGet(data)
        }
    })
}
