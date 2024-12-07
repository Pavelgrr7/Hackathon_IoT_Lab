// script.js

document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.querySelector('.input-group input[type="password"]');
    const togglePasswordButton = document.querySelector('.toggle-password');
    const loginButton = document.getElementById('normal_user');
    const adminLoginButton = document.getElementById('admin_user');

    // Переключение видимости пароля
    togglePasswordButton.addEventListener("click", () => {
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            togglePasswordButton.textContent = "🙈"; // Иконка "глаз закрыт"
            console.log("Пароль раскрыт");
        } else {
            passwordInput.type = "password";
            togglePasswordButton.textContent = "👁"; // Иконка "глаз открыт"
            console.log("Пароль скрыт");
        }
    });

    // Переход на другую страницу
    loginButton.addEventListener("click", (event) => {
        console.log('Отправляю данные');
        event.preventDefault(); // Отменяем стандартное поведение формы
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
        let success = false;
        postCredenitals(username, password);
    });

    adminLoginButton.addEventListener("click", (event) => {
        console.log('Отправляю данные');
        event.preventDefault(); // Отменяем стандартное поведение формы
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
        postCredenitals(username, password, false);
    });
});

async function postCredenitals(username, password, isAdmin) {
    console.log("Отправляю JSON:    " + JSON.stringify({ "username":username, "password":password }));
    let url = "http://192.168.0.139:8080/login";
    // let url = "http://192.168.1.244:8080/login";

    if (isAdmin == true) {
        url = "http://192.168.0.139:8080/login";
        // url = "http://192.168.1.244:8080/login";

    } else {
        url = "http://192.168.0.139:8080/login";
        // url = "http://192.168.1.244:8080/login";
    }
    const response = await fetch(url, {
        method:"POST",
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ "username":username, "password":password }),
    });
    const data = await response.json();
    console.log(data)
    const success = data["success"];
    if (success == true) {
        location.href = "main.html";
    } else {
        console.log("Неверное имя пользователя или пароль");
    }
}