document.addEventListener("DOMContentLoaded", () => {
    const checkButtons = document.querySelectorAll(".service-check-button");
    const checkAllButton = document.getElementById("check-all-button");

    // Функция проверки устройства
    const checkDevice = async (button, checkbox) => {
        const device = button.dataset.device;

        try {
            // Делаем запрос на сервер
            const response = await fetch(`http://192.168.0.139:8080/api/service/`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify( { "device" : device }),
            });

            if (!response.ok) {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }

            const data = await response.json();

            // Если проверка успешна
            if (data.status === "ok") {
                checkbox.checked = true;
                button.disabled = true; // Отключаем кнопку после успешной проверки
            } else {
                alert(`Устройство ${device} не прошло проверку.`);
            }
        } catch (error) {
            console.error("Ошибка при проверке устройства:", error);
            alert("Не удалось выполнить операцию. Проверьте соединение с сервером.");
        }
    };

    // Обработчик для проверки одного устройства
    checkButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const checkbox = document.getElementById(`${button.dataset.device}-checked`);

            // Проверяем, не отмечено ли устройство как проверенное
            if (!checkbox.checked) {
                checkDevice(button, checkbox);
            }
        });
    });

    // Обработчик для проверки всех устройств
    checkAllButton.addEventListener("click", () => {
        checkButtons.forEach((button) => {
            const checkbox = document.getElementById(`${button.dataset.device}-checked`);

            // Проверяем только непроверенные устройства
            if (!checkbox.checked) {
                checkDevice(button, checkbox);
            }
        });
    });
});