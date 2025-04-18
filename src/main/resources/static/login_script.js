// script.js

document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.querySelector('.input-group input[type="password"]');
    const togglePasswordButton = document.querySelector('.toggle-password');
    const loginButton = document.getElementById('normal_user');
    const guestLoginButton = document.getElementById('guest_user');
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
        postCredenitals(username, password, false);
    });

    // Обработчик для кнопки "Войти как гость"
    guestLoginButton.addEventListener("click", (event) => {
        console.log('Вход как гость');
        event.preventDefault(); // Отменяем стандартное поведение формы
        // Устанавливаем localStorage, что пользователь - гость
        localStorage.setItem('isGuestMode', 'true');
        // Перенаправляем на основную страницу
        location.href = "main.html";
    });

    if (adminLoginButton) {
        adminLoginButton.addEventListener("click", (event) => {
            console.log('Отправляю данные');
            event.preventDefault(); // Отменяем стандартное поведение формы
            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;
            postCredenitals(username, password, true);
        });
    }
});

async function postCredenitals(username, password, isGuest) {
    console.log("Отправляю JSON:    " + JSON.stringify({ "username":username, "password":password }));
    let url = "http://192.168.0.139:8080/login";
    // let url = "http://192.168.1.244:8080/login";

    if (isGuest == true) {
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
        body: JSON.stringify({ "username":username, "password":password, "isGuest":isGuest }),
    });
    const data = await response.json();
    console.log(data)
    const success = data["success"];
    if (success == true) {
        // Сбрасываем гостевой режим при обычном входе
        localStorage.removeItem('isGuestMode');
        if (isGuest == true) {
            location.href = "service_mode.html";
        } else {
            location.href = "main.html";
        }
    } else {
        if (isGuest == true) {
            console.log("Неверное имя пользователя или пароль. Возможно у вас нет прав администратора.");
            alert("Неверное имя пользователя или пароль. Возможно у вас нет прав администратора.");
        } else {
            console.log("Неверное имя пользователя или пароль");
            alert("Неверное имя пользователя или пароль");
        }
    }
}