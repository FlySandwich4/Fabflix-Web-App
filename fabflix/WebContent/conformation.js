let cartBody=$("#cartBody")
function handleConformation(data){
    console.log(data)
    cartBody.empty()
    cartBody.append("<div class='header-font'>Payment Conformation</div>")
    let totalPrice = 0
    let appendHtml = ""
    appendHtml +=  "<div class='card'>" +
        "<div class='card-item title purple'>SaleId</div>" +
        "<div class='card-item title purple'>Movie Name</div>" +
        "<div class='card-item title purple'>Quantity</div>" +
        "<div class='card-item title purple'>Price</div>" +
        "<div class='card-item title purple'>Total</div>" +
        "</div>"
    for(let key in data){
        totalPrice += 50 * parseInt(data[key]["num"])
        appendHtml += "<div class='line'></div>"
        appendHtml += "<div class='card'>"+
            "<div class='card-item title purple'>"
                for(let each=0; each<data[key]["saleId"].length;each++){
                    appendHtml += data[key]["saleId"][each].toString()
                    if(each !== data[key]["saleId"].length-1){
                        appendHtml += ", "
                    }
                }
                appendHtml += "</div>" +
            "<div class='card-item title '>" + data[key]["name"] +"</div>" +
            "<div class='card-item title '>" + data[key]["num"] +"</div>" +
            "<div class='card-item title '>" + "$50" +"</div>" +
            "<div class='card-item title '>$" + parseInt(data[key]["num"])*50 +"</div>" +
            "</div>"
    }
    appendHtml+="<h1> Total Price: $"+totalPrice+"</h1>"
    cartBody.append(appendHtml)
}