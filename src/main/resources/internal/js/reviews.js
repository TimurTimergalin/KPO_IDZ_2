function add_review(json) {
    let template = document.querySelector("#review-template")
    let reviews_div = document.querySelector("#other-reviews")

    let clone = template.content.cloneNode(true)
    let fields = clone.querySelectorAll(".field-value")

    for (let i = 0; i < 3; ++i) {
        switch (i) {
            case 0:
                fields[i].innerText = json["authorName"]; break
            case 1:
                fields[i].innerText = json["rating"]; break
            case 2:
                fields[i].innerText = json["text"]; break
        }
    }
    reviews_div.appendChild(clone)
}

function validate_input() {
    let error = document.querySelector("#error-message")
    let rating = document.querySelector("#input-rating")

    if (rating.value === "" || rating.value < 1 || rating.value > 5) {
        error.innerText = "Invalid rating value"
        return false
    }
    return true
}

function set_input(json, ws) {
    if (!json) {
        json = {
            rating: "",
            text: ""
        }
    }
    console.log(json)
    document.querySelector("#input-rating").value = json["rating"]
    document.querySelector("#input-review").value = json["text"]

    let save_button = document.querySelector("#save-button")
    let dismiss_button = document.querySelector("#dismiss-button")
    let error_message = document.querySelector("#error-message")
    let rating = document.querySelector("#input-rating")
    let text = document.querySelector("#input-review")
    save_button.onclick = function () {
        if (validate_input()) {
            let res = {
                rating: rating.value,
                text: text.value
            }

            save_button.disabled = true
            dismiss_button.disabled = true
            error_message.innerText = "ㅤ"
            ws.send(JSON.stringify(res))
        }
    }

    dismiss_button.onclick = function () {
        console.log(json)
        rating.value = json["rating"]
        text.value = json["text"]
        save_button.disabled = true
        dismiss_button.disabled = true
        error_message.innerText = "ㅤ"
    }

    rating.oninput = function () {
        save_button.disabled = false
        dismiss_button.disabled = false
    }

    text.oninput = rating.oninput
}

function configure_ws(ws) {
    ws.onopen = function () {
        console.log("connection ok")
        ws.send("more")
    }
    ws.onerror = function () {
        error_popup("ws connection error")
    }

    ws.onmessage = function (event) {
        let data = JSON.parse(event.data)

        if (typeof data["error"] === "string") {
            error_popup(data["error"])
            return
        }

        if (data["reviews"] !== undefined) {
            data["reviews"].forEach(add_review)
            display_more_button(data["more"])
            return
        }

        set_input(data["user-review"], ws)
    }
}


function init() {
    let ws = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") +
        window.location.host + "/ws" + window.location.pathname)

    configure_ws(ws)

    init_more_button(ws)
}

init()