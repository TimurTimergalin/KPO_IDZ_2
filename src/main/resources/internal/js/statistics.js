let stats = [];
let connected = false;

function clear_children(node) {
    while (node.lastElementChild != null) {
        node.removeChild(node.lastElementChild)
    }
}

function sort() {
    let sortBy = document.querySelector("#sort-by").value
    let center = document.querySelector("#stats-list")

    clear_children(center)

    stats.sort(function (a, b) {
        if (a[sortBy] < b[sortBy]) {
            return -1;
        }
        if (a[sortBy] === b[sortBy]) {
            return 0;
        }
        return 1;
    })

    stats.forEach(function (el) {
        add_stat(el)
    })
}

function add_stat(stat) {
    let template = document.querySelector("#dish-statistics-template")
    let clone = template.content.cloneNode(true)

    let ps = clone.querySelectorAll(".stats-value")

    for (let i = 0; i < 4; ++i) {
        switch (i) {
            case 0:
                ps[i].innerText = stat["name"]; break
            case 1:
                ps[i].innerText = stat["ordersCount"]; break
            case 2:
                ps[i].innerText = stat["averageRating"];break
            case 3:
                ps[i].innerText = stat["revenue"]; break
        }
    }

    let a = clone.querySelector("a")
    a.href = "/reviews/" + stat["id"]

    let center = document.querySelector("#stats-list")
    center.appendChild(clone)
}

function configure_ws(ws) {
    ws.onopen = function () {
        console.log("connection ok")
        connected = true
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

        stats = data["dishes"]
        sort()
    }
}

function init() {
    let ws = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") +
        window.location.host + "/ws/statistics")

    configure_ws(ws)

    let sorted = document.querySelector("#sort-by")
    sorted.onchange = function () {
        if (connected) {
            sort()
        }
    }
}

init()
