function error_popup(mes) {
    let popup = document.querySelector("#error-popup")
    let textEl = popup.querySelector("p")
    textEl.innerText = mes
    popup.style.display = "flex"
}

function hide_popup() {
    let popup = document.querySelector("#error-popup")
    popup.style.display = "none"
}
