function get_status(st) {
    switch (st) {
        case null:
            return "in progress"
        case "paid":
            return "finished"
        default:
            return "cancelled"
    }
}

function add_order(json) {
    let orders_div = document.querySelector("#orders")
    let template = document.querySelector("#order-template")

    let clone = template.content.cloneNode(true)
    let items = clone.querySelector(".order-items")

    json["dishes"].forEach(function (dish) {
        let p = document.createElement("p")
        p.innerText = dish["name"] + " x" + dish["count"]
        items.appendChild(p)
    })

    let status_div = clone.querySelector(".status-div")
    let status = json["finishReason"]
    status_div.children[0].innerText += get_status(status)

    if (status === null) {
        let link = document.createElement("a")
        link.href = "/my-order"
        link.innerText = "Go"
        status_div.appendChild(link)
    } else {
        let p = document.createElement("p")
        p.innerText = "Finished: " + json["endTime"]
        status_div.appendChild(p)
    }
    orders_div.appendChild(clone)
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
        console.log(data)

        if (typeof data["error"] === "string") {
            error_popup(data["error"])
            return
        }

        data["orders"].forEach(add_order)

        display_more_button(data["more"])
    }
}

function init() {
    let ws = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") +
        window.location.host + "/ws/my-orders")

    configure_ws(ws)

    init_more_button(ws)
}

init()
