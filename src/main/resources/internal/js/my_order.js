let menu_items = new Map()
let cart_items = new Map()
let order_items = new Map()
let order_finished = false
let accepting_updates = true

function add_menu_item(json) {
    let template = document.querySelector("#menu-item-template")
    let clone = template.content.firstElementChild.cloneNode(true)

    let ps = clone.querySelectorAll("p")
    ps[1].innerText = json["name"]
    ps[3].innerText = json["count"]
    ps[5].innerText = json["cookingTime"]
    ps[7].innerText = json["price"]
    let a = clone.querySelector("a")
    a.href = "/reviews/" + json["id"]
    menu_items.set(json["id"], clone)
    let b = clone.querySelector(".add-to-cart-button")
    b.onclick = () => {
        add_cart_item(json)
        b.disabled = true
    }
    b.disabled = parseInt(json["count"]) === 0
    document.querySelector(".menu-holder").appendChild(clone)
}

function add_order_item(json, ws) {
    let template = document.querySelector("#order-item-template")
    let clone = template.content.firstElementChild.cloneNode(true)

    let ps = clone.querySelectorAll("p")
    ps[0].innerText = json["name"]
    ps[1].innerText = json["status"]

    order_items.set(json["id"], clone)
    clone.querySelector("button").onclick = () => remove_order_item(json["id"], ws)
    document.querySelector(".order-list").appendChild(clone)
}

function remove_order_item(id, ws){
    let obj = order_items.get(id)
    order_items.delete(id)
    document.querySelector(".order-list").removeChild(obj)
    ws.send(JSON.stringify({
        cancel : {
            id: id
        }
    }))

    if (order_items.size === 0) {
        restart()
    }
}

function add_cart_item(json) {
    let template = document.querySelector("#cart-item-template")
    let clone = template.content.firstElementChild.cloneNode(true)

    let p = clone.querySelector("p")
    p.innerText = json["name"]
    let inp = clone.querySelector("input")
    inp.max = parseInt(menu_items.get(json["id"]).querySelectorAll("p")[3].innerText)
    inp.value = 1

    let list = document.querySelector(".cart-list")
    let button = clone.querySelector("button")
    button.onclick = () => remove_cart_item(json["id"])
    cart_items.set(json["id"], clone)
    list.appendChild(clone)

    document.querySelectorAll("#dismiss-button, #order-button").forEach((el) => el.disabled = false)
}

function remove_cart_item(id) {
    let obj = cart_items.get(id)

    cart_items.delete(id)

    let list = document.querySelector(".cart-list")
    list.removeChild(obj)


    if (cart_items.size === 0) {
        document.querySelectorAll("#dismiss-button, #order-button").forEach((el) => el.disabled = true)
    }

    let dish = menu_items.get(id)
    let b = dish.querySelector(".add-to-cart-button")
    b.disabled = false
}

function update_count(json) {
    let menu_item = menu_items.get(json["id"])
    if (menu_item === undefined) {
        return
    }

    let b = menu_item.querySelector(".add-to-cart-button")
    b.disabled = json["count"] === 0

    let item_ps = menu_item.querySelectorAll("p")
    item_ps[3].innerText = json["count"]

    let cart_item = cart_items.get(json["id"])
    if (cart_item === undefined) {
        return
    }

    if (json["count"] > 0) {
        let inp = cart_item.querySelector("input")
        inp.max = json["count"]
        let v = parseInt(inp.value)
        if (v !== undefined && v > json["count"]) {
            inp.value = json["count"]
        }
    } else {
        remove_cart_item(json["id"])
    }
}

function update_status(json) {
    let order_item = order_items.get(json["id"])
    if (order_item === undefined) {
        console.log("no such order item")
        return
    }
    let p = order_item.querySelectorAll("p")[1]
    p.innerText = json["status"]
}

function disable_updates() {
    if (accepting_updates) {
        return
    }
    let buttons = document.querySelectorAll(".add-to-cart-button")
    dismiss()
    buttons.forEach(b => b.disabled = true)
}

function finish_order() {
    if (!order_finished) {
        return
    }

    let buttons = document.querySelectorAll(".cancel-button")
    buttons.forEach(b => b.disabled = true)
    document.querySelector("#pay-button").disabled = false
}

function validate_cart_item(el) {
    let inp = el.querySelector("input")
    let v = inp.value
    let int = parseInt(v)
    if (isNaN(int)) {
        return false
    }

    return int >= inp.min && int <= inp.max
}

function jsonify_cart_item(id, el) {
    return {
        dishId: id,
        count: el.querySelector("input").value
    }
}


function order(ws) {
    let keys = Array.from(cart_items.keys())

    let res = []
    for (const key of keys) {
        let el = cart_items.get(key)

        if (!validate_cart_item(el)) {
            let err = document.querySelector("#order-error-message")
            err.innerText = "Invalid count value"
            return
        }

        res.push(jsonify_cart_item(key, el))
    }

    dismiss()
    ws.send(JSON.stringify({newOrder: res}))
}

function restart() {
    cart_items.clear()
    order_items.clear()
    order_finished = false
    accepting_updates = true

    let order_list = document.querySelector(".order-list")
    while (order_list.firstChild !== null) {
        order_list.removeChild(order_list.firstChild)
    }

    document.querySelectorAll("#cancel-all-button, #pay-button").forEach((el) => el.disabled = true)
    dismiss()
    for (let obj of menu_items.values()) {
        let count_p = obj.querySelectorAll("p")[3]
        obj.querySelector(".add-to-cart-button").disabled = parseInt(count_p.innerText) === 0
    }
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

        if (data["dishes"] !== undefined) {
            data["dishes"].forEach(add_menu_item)
        } else if (data["order-items"] !== undefined) {
            data["order-items"].forEach((el) => add_order_item(el, ws))
            accepting_updates = data["accept-updates"]
            order_finished = data["order-finished"]
            document.querySelector("#cancel-all-button").disabled = false
            disable_updates()
            finish_order()
        } else if (data["order-dishes"] !== undefined) {
            data["order-dishes"].forEach((el) => add_order_item(el, ws))
            document.querySelector("#cancel-all-button").disabled = false
        } else if (data["dish-count"] !== undefined) {
            update_count(data["dish-count"])
        } else if (data["updates-unavailable"] !== undefined) {
            accepting_updates = !data["updates-unavailable"]
            disable_updates()
        } else if (data["order-finished"] !== undefined) {
            order_finished = data["order-finished"]
            finish_order()
        } else if (data["status-update"] !== undefined) {
            update_status(data["status-update"])
        }
    }
}

function dismiss() {
    let keys = Array.from(cart_items.keys())

    keys.forEach(key => remove_cart_item(key))

    document.querySelector("#order-error-message").innerText = ""
}


function init() {
    let ws = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") +
        window.location.host + "/ws/my-order")

    configure_ws(ws)

    document.querySelector("#dismiss-button").onclick = dismiss
    document.querySelector("#order-button").onclick = () => order(ws)
    document.querySelector("#cancel-all-button").onclick = () => {
        ws.send(JSON.stringify(
            {
                cancelAll: true
            }
        ))
        restart()
    }

    document.querySelector("#pay-button").onclick = () => {
        ws.send(JSON.stringify(
            {
                paid: true
            }
        ))
        restart()
    }
}

init()