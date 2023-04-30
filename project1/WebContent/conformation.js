let cartBody=$("#cartBody")
function handleConformation(data){
    console.log(data)
    cartBody.empty()
    cartBody.append("<h1>Conformation</h1>")
    let totalPrice = 0
    let appendHtml = "<div>"
    for(let key in data){
        totalPrice += 50 * parseInt(data[key]["num"])
        appendHtml += "<div>"+ key + " " + data[key]["num"]+" " + data[key]["name"] + (50 * parseInt(data[key]["num"])) + "</div>"
    }
    appendHtml+="</div>"
    appendHtml+="<h1> Total Price: $"+totalPrice+"</h1>"
    cartBody.append(appendHtml)
}