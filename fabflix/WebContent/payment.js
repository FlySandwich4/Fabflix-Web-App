let paymentShow = $("#cart-body")
let creditCardForm = $("#creditCardForm")

function handlePaymentGet(data){
    paymentShow.empty()
    let totalPrice = 0
    for(let key in data){
        totalPrice += 50 * parseInt(data[key]["num"])
    }
    let appendHtml = "<h1> Total Price: $"+totalPrice+"</h1>"
    paymentShow.append(appendHtml)
}

function submitCreditCardForm(event){
    event.preventDefault()
    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: creditCardForm.serialize(),
            success: data => {
                console.log("success")

                if(data["success"] === 1 ){
                    window.location.replace("conformation.html")
                }else{
                    let errDiv = $("#error")
                    errDiv.empty()
                    errDiv.append("Incorrect Credit Card Information")
                }

            }
        }
    );
}

creditCardForm.submit(submitCreditCardForm)