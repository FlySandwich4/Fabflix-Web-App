let cartBody = $("#cart-body")
function handleCartGet(data){
    console.log(data)
    cartBody.empty()
    let totalPrice = 0
    let appendHtml = "<div class='header-font'>Your Shopping Cart</div>"
    appendHtml +=  "<div class='card'>" +
        "<div class='card-item title purple'>Movie Title</div>" +
        "<div class='card-item small purple'>Add One</div>" +
        "<div class='card-item small purple'>Movie Counts</div>" +
        "<div class='card-item small purple'>Remove One</div>" +
        "<div class='card-item small purple'>Price</div>" +
        "<div class='card-item small purple'>Delete</div>"+
        "</div>"
    for(let key in data){
        totalPrice += 50 * parseInt(data[key]["num"])
        appendHtml += "<div class='line'></div>"
        appendHtml +=  "<div class='card'>" +
            "<div class='card-item title'>" + data[key]["name"] + "</div>" +
            "<div class='card-item small action'><a href='#' onclick='submitCartIncrement(\"" + key +  "\")'> +1 item </a></div>" +
            "<div class='card-item small'>" +data[key]["num"] + "</div>" +
            "<div class='card-item small action'><a href='#' onclick='submitCartDecrement(\"" + key +  "\")'> -1 item </a></div>" +
            "<div class='card-item small'> $" + (50 * parseInt(data[key]["num"])) +"</div>" +
            "<div class='card-item small action'><a href='#' onclick='submitCartDelete(\"" + key +  "\")'> Delete </a></div>"+
            "</div>"

    }
    appendHtml+="<h1> Total Price: $"+totalPrice+"</h1>" +
        "<div class='proceed'><a href=\"payment.html\">Proceed to Payment</a></div>"

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
