function init_more_button(ws) {
    let button = document.querySelector("#more-button")
    button.onclick = function () {
        ws.send("more")
    }
}

function _show_more_button() {
    let button = document.querySelector(".more-div")
    button.style.display = "flex"
}

function _hide_more_button() {
    let button = document.querySelector(".more-div")
    button.style.display = "none"
}

function display_more_button(flag) {
    if (flag) {
        _show_more_button()
    } else {
        _hide_more_button()
    }
}