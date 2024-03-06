function check_password_repeat() {
    let password = document.querySelector("#password")
    let repeat = document.querySelector("#repeat-password")

    if (repeat === null) {
        return true
    }

    return password.value === repeat.value
}

function check_login_length() {
    let login = document.querySelector("#login")
    return login.value.length <= 128
}

function check_valid_login() {
    let login = document.querySelector("#login")
    return /[A-Za-z0-9_]*/.test(login.value)
}

function check_empty() {
    let login = document.querySelector("#login")
    let password = document.querySelector("#password")

    return login.value !== "" && password.value !== ""
}

function on_submit() {
    let err = document.querySelector("#error-message")
    if (!check_password_repeat()) {
        err.innerText = "Passwords do not match"
        return
    }
    if (!check_empty()) {
        err.innerText = "A field is left unfilled"
        return;
    }
    if (!check_login_length()) {
        err.innerText = "The login is too long"
    }

    if (!check_valid_login()) {
        err.innerText = "Invalid symbols in login"
        return;
    }

    let form = document.querySelector("#sign-form")
    form.submit()
}