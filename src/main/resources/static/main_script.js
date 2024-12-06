// script.js

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
async function fetchData() {
    try {
        const response = await fetch('http://localhost:8080/api/hello').then(response => response.text()).then(data => console.log(data));
        const data = await response.json();

        // Обновляем прогресс-бары
        updateProgress('cpu-progress', 'cpu-value', 'cpu-details', data.cpuUsage, `1 Core`);
        updateProgress('ram-progress', 'ram-value', 'ram-details', data.ramUsage, `${data.ramUsed} MB / ${data.ramTotal} GB`);
        updateProgress('swap-progress', 'swap-value', 'swap-details', data.swapUsage, `${data.swapUsed} B / ${data.swapTotal} MB`);
    } catch (error) {
        console.error('Ошибка при получении данных:', error);
    }
}

async function postData() {
    try {
        const response = await fetch
    } catch (e) {
        console.error("Ошибка при отправке данных", e);
    }
}

// Запуск обновления данных каждые 5 секунд
setInterval(fetchData, 1000);

// Первый запуск
fetchData();

// script.js

// Список датчиков с описаниями
const sensors = [
    { id: 'water-level', name: 'Датчик уровня воды', format: 'мл' },
    { id: 'water-flow', name: 'Датчик потока воды', format: 'л' },
    { id: 'leakage', name: 'Датчик протечки воды (MGS-WT1)', format: 'HIGH/LOW' },
    { id: 'color', name: 'Датчик цвета (MGS-CLM60)', format: 'RGB' },
    { id: 'temp-humid-press', name: 'Датчик температуры, влажности и давления (MGS-THP80)', format: '°C, %, гПа' },
    { id: 'light', name: 'Датчик освещённости (MGS-L75)', format: 'люксы' },
    { id: 'current', name: 'Датчик тока', format: 'А' },
    { id: 'voc-co2', name: 'Датчик ЛОС и СО2 (MGS-CO30)', format: 'ppm' },
    { id: 'distance', name: 'Датчик расстояния (MGS-D20)', format: 'см' },
    { id: 'gyro', name: 'Гироскоп/акселерометр (MGS-A6)', format: 'м/с², градусы' },
    { id: 'reed', name: 'Геркон', format: 'HIGH/LOW' },
    { id: 'rotation', name: 'Датчик оборотов', format: 'Гц' },
    { id: 'led-light', name: 'Датчик освещённости LED', format: 'люксы' },
];

// Генерация списка датчиков
const sensorListElement = document.getElementById('sensors-list');

function generateSensorsList() {
    sensors.forEach(sensor => {
        const li = document.createElement('li');
        li.innerHTML = `
            <span class="sensor-name">${sensor.name}</span>
            <span class="sensor-value" id="${sensor.id}-value">Загрузка...</span>
        `;
        sensorListElement.appendChild(li);
    });
}

// Обновление значений датчиков
async function updateSensorValues() {
    try {
        const response = await fetch('/api/sensors'); // Замените на реальный API
        const data = await response.json();

        sensors.forEach(sensor => {
            const valueElement = document.getElementById(`${sensor.id}-value`);
            const value = data[sensor.id];

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
setInterval(updateSensorValues, 5000);
updateSensorValues();