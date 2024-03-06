function validate_dish(inputs, error) {
    for (let i = 0; i < 4; ++i) {
        let el = inputs[i]

        if (i === 1 || i === 2) {
            let int = parseInt(el.value);
            if (isNaN(int)) {
                error.innerText = "Unable to convert input to a number"
                return false;
            }

            if (int < 0) {
                error.innerText = "Negative values are not allowed"
                return false;
            }
        } else if (i === 3) {
            let float = parseFloat(el.value);
            if (isNaN(float)) {
                error.innerText = "Unable to convert input to a number"
                return false;
            }

            if (float < 0) {
                error.innerText = "Negative values are not allowed"
                return false;
            }
        } else {
            if (el.value === "") {
                error.innerText = "Field is left unfilled"
                return false
            }

            if (el.value.length > 64) {
                error.innerText = "Dish name is too long"
                return false;
            }
        }
    }
    return true
}

function on_add() {
    let form = document.querySelector(".menu-item")
    let inputs = form.querySelectorAll("input")

    let error = form.querySelector(".error-message")

    if (validate_dish(inputs, error)) {
        form.submit()
    }
}

function add_dish(json, ws) {
    let parent = document.querySelector("#for-existing")
    let template = document.querySelector("#dish-template")

    let clone = template.content.cloneNode(true)
    let form = clone.querySelector("form")
    form.action = "/delete-menu-item"
    let button_div = clone.querySelector(".buttons")
    let error = clone.querySelector(".error-message")

    let inputs = clone.querySelectorAll("input")

    let init_inputs = function () {
        for (let i = 0; i < 4; ++i) {
            switch (i) {
                case 0:
                    inputs[i].value = json["name"];
                    break;
                case 1:
                    inputs[i].value = json["count"];
                    break;
                case 2:
                    inputs[i].value = json["cookingTime"];
                    break;
                case 3:
                    inputs[i].value = json["price"];
                    break;
            }
        }
    }

    init_inputs()

    let save_button = document.createElement("button")
    save_button.className = "dish-button"
    save_button.innerText = "Save"
    save_button.type = "button"
    save_button.disabled = true

    let dismiss_button = document.createElement("button")
    dismiss_button.className = "dish-button"
    dismiss_button.innerText = "Dismiss"
    dismiss_button.type = "button"
    dismiss_button.disabled = true

    let delete_button = document.createElement("button")
    delete_button.className = "dish-button"
    delete_button.innerText = "Delete"

    let id_input = document.createElement("input")
    id_input.name = "id"
    id_input.value = json["id"]
    id_input.style.display = "none"

    inputs.forEach(function (input) {
        input.oninput = function () {
            save_button.disabled = false
            dismiss_button.disabled = false
        }
    })

    dismiss_button.onclick = function () {
        init_inputs()
        dismiss_button.disabled = true
        save_button.disabled = true
        error.innerText = ""
    }

    save_button.onclick = function () {
        let res = {"id" : json["id"]}

        if (!validate_dish(inputs, error)) {
            return
        }

        for (let i = 0; i < 4; ++i) {
            switch (i) {
                case 0:
                    res["name"] = inputs[i].value;
                    break;
                case 1:
                    res["count"] = parseInt(inputs[i].value);
                    break;
                case 2:
                    res["cookingTime"] = parseInt(inputs[i].value);
                    break;
                case 3:
                    res["price"] = parseFloat(inputs[i].value);
                    break;
            }
        }

        save_button.disabled = true
        dismiss_button.disabled = true
        error.innerText = ""
        ws.send(JSON.stringify(res))
    }

    button_div.appendChild(save_button)
    button_div.appendChild(dismiss_button)
    button_div.appendChild(delete_button)
    button_div.appendChild(id_input)
    parent.appendChild(clone)
}

function configure_ws(ws) {
    ws.onopen = function () {
        console.log("connection ok")
    }
    ws.onerror = function () {
        error_popup("ws connection error")
    }

    ws.onmessage = function (event) {
        let data = JSON.parse(event.data)
        console.log(data)

        if (typeof data["error"] === "string") {
            error_popup(data["error"])
            return
        }
        data["dishes"].forEach(function (dish) {
            add_dish(JSON.parse(dish), ws)
        })
    }
}

function init() {
    let parent = document.querySelector("#for-add")
    let template = document.querySelector("#dish-template")

    let clone = template.content.cloneNode(true)
    let form = clone.querySelector("form")
    form.action = "/new-menu-item"
    let button_div = clone.querySelector(".buttons")

    let add_button = document.createElement("button")
    add_button.className = "dish-button"
    add_button.innerText = "Add"
    add_button.type = "button"
    add_button.onclick = on_add
    button_div.appendChild(add_button)
    parent.insertBefore(clone, parent.firstChild)

    let ws = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") +
        window.location.host + "/ws/menu")
    configure_ws(ws)
}

init()
