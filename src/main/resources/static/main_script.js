// script.js


document.addEventListener("DOMContentLoaded", () => {
    const devices = document.querySelectorAll(".toggle-button");

    devices.forEach((button) => {
        button.addEventListener("click", async () => {
            const device = button.dataset.device;
            const statusSpan = document.getElementById(`status-${device}`);

            // Определяем текущее состояние (включено/выключено)
            const isTurningOn = button.textContent === "Включить";

            try {
                // Отправляем запрос на сервер для смены состояния
                const response = await fetch(`http://192.168.0.139:8080/api/device`, {
                    method: "POST",
                    body: JSON.stringify({ "device" : device, "toggle": isTurningOn ? "on" : "off" }),
                    headers: { "Content-Type": "application/json" },
                });

                if (!response.ok) {
                    throw new Error(`Ошибка: ${response.status}`);
                }

                const data = await response.json();
                console.log("Сервер вернул   " + data[status])

                // Проверяем результат с сервера
                const isOn = data.status;
                console.log("Теперь статус устройства " + device + " - " + isOn)

                // Обновляем статус устройства
                statusSpan.textContent = isOn ? "Работает" : "Не работает";
                statusSpan.classList.toggle("on", isOn);
                statusSpan.classList.toggle("off", !isOn);

                // Меняем текст кнопки
                button.textContent = isOn ? "Выключить" : "Включить";
                button.classList.toggle("off", isOn);
            } catch (error) {
                console.error("Ошибка при обновлении устройства:", error);
                alert("Не удалось выполнить операцию. Проверьте соединение с сервером или обратитесь к системному администратору.");
            }
        });

        // Автоматическое обновление статуса устройства
        setInterval(async () => {
            const statusSpan = document.getElementById(`status-${button.dataset.device}`);

            try {
                const device = button.dataset.device
                console.log("Отправил серверу " + JSON.stringify( { "device" : device, "toggle":"" }))
                const response = await fetch(`http://192.168.0.139:8080/api/device`, {
                    method: "POST",
                    body: JSON.stringify( { "device" : device, "toggle":"" }),
                    headers:{ "Content-Type": "application/json" },
                });

                if (!response.ok) {
                    throw new Error(`Ошибка: ${response.status}`);
                }

                const data = await response.json();

                // Проверяем результат с сервера
                const isOn = data.status;

                // Обновляем текст статуса
                statusSpan.textContent = isOn ? "Работает" : "Не работает";
                statusSpan.classList.toggle("on", isOn);
                statusSpan.classList.toggle("off", !isOn);

                // Обновляем текст и цвет кнопки
                button.textContent = isOn ? "Выключить" : "Включить";
                button.classList.toggle("off", isOn);
            } catch (error) {
                console.error("Ошибка при обновлении статуса устройства:", error);
            }
        }, 10000);
    });
});

// Функция обновления прогресс-бара
function updateProgress(circleId, valueId, detailsId, percentage, details) {
    const circle = document.getElementById(circleId);
    const value = document.getElementById(valueId);
    const detailsElement = document.getElementById(detailsId);

    // Обновляем круговой индикатор
    const angle = (percentage / 100) * 360;
    circle.style.background = `conic-gradient(#00a000 ${angle}deg, #e0e0e0 ${angle}deg)`;

    // Обновляем текстовое значение
    value.textContent = `${percentage.toFixed(2)}%`;

    // Обновляем детали
    if (details) {
        detailsElement.textContent = details;
    }
}

// Функция для получения данных с сервера
// async function fetchData() {
//     try {
//         const response = await fetch('http://192.168.1.244:8080/api/sensors').then(response => response.text()).then(data => console.log(data));
//         const data = await response.json();

//         // Обновляем прогресс-бары
//         updateProgress('cpu-progress', 'cpu-value', 'cpu-details', data.cpuUsage, `1 Core`);
//         updateProgress('ram-progress', 'ram-value', 'ram-details', data.ramUsage, `${data.ramUsed} MB / ${data.ramTotal} GB`);
//         updateProgress('swap-progress', 'swap-value', 'swap-details', data.swapUsage, `${data.swapUsed} B / ${data.swapTotal} MB`);
//     } catch (error) {
//         console.error('Ошибка при получении данных:', error);
//     }
// }

// async function postData() {
//     try {
//         const response = await fetch
//     } catch (e) {
//         console.error("Ошибка при отправке данных", e);
//     }
// }

// Запуск обновления данных каждые 5 секунд
// setInterval(fetchData, 1000);

// Первый запуск
// fetchData();

// Список датчиков с описаниями
const sensors = [
    { id: 'water-level', name: 'Объем воды в колбе', format: 'мл' },//
    { id: 'water-flow', name: 'Датчик потока воды', format: 'л' },//
    { id: 'color', name: 'Датчик цвета (MGS-CLM60)', format: 'RGB' },//
    { id: 'temp-humid-press', name: 'Температура (MGS-THP80)', format: '°C' },
    { id: 'temp-humid-press', name: 'Влажность (MGS-THP80)', format: '%' },
    { id: 'light', name: 'Датчик освещённости (MGS-L75)', format: 'люксы' },//
    { id: 'current', name: 'Датчик тока', format: 'А' },//
    { id: 'voc-co2', name: 'Датчик ЛОС и СО2 (MGS-CO30)', format: 'ppm' },//
    { id: 'distance', name: 'Датчик расстояния (MGS-D20)', format: 'см' },//
    { id: 'reed1', name: 'Геркон 1', format: 'HIGH/LOW' },//
    { id: 'reed2', name: 'Геркон 2', format: 'HIGH/LOW' },//
];

// Генерация списка датчиков
const sensorListElement = document.getElementById('sensors-list');

function generateSensorsList() {
    sensors.forEach(sensor => {
        const li = document.createElement('li');
        li.innerHTML = `
            <span class="sensor-name">${sensor.name}</span>
            <span class="spacer"></span>
            <span class="sensor-value" id="${sensor.id}-value">Загрузка...</span>
        `;
        sensorListElement.appendChild(li);
    });
}

// Обновление значений датчиков
async function updateSensorValues() {
    try {
        const response = await fetch('http://192.168.0.139:8080/api/sensors');
        const data = await response.json();
        // console.log("Полученная информация с сенсоров:       " + response.text());

        sensors.forEach(sensor => {
            const valueElement = document.getElementById(`${sensor.id}-value`);
            const value = data[sensor.id];
            // console.log("Данные  " + sensor.id + " = " + value)

            // Форматируем статус (HIGH/LOW)
            if (sensor.format === 'HIGH/LOW') {
                valueElement.textContent = value;
                valueElement.className = value === 'HIGH' ? 'sensor-status' : 'sensor-status low';
            } else {
                valueElement.textContent = `${value} ${sensor.format}`;
            }
        });
    } catch (error) {
        console.error('Ошибка при обновлении значений датчиков:', error);
    }
}

// Первоначальная генерация списка
generateSensorsList();

// Периодическое обновление данных
setInterval(updateSensorValues, 1000);
updateSensorValues();