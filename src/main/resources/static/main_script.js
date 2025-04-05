// Функциональность видеоплеера MJPEG
document.addEventListener('DOMContentLoaded', function() {
    // Видеоплеер MJPEG
    const mjpegStream = document.getElementById('mjpeg-stream');
    const fullscreenBtn = document.getElementById('fullscreen-toggle');
    
    // Кнопка полноэкранного режима
    if (fullscreenBtn) {
        fullscreenBtn.addEventListener('click', function() {
            if (document.fullscreenElement) {
                document.exitFullscreen();
            } else {
                const videoWrapper = document.querySelector('.video-player-wrapper');
                if (videoWrapper.requestFullscreen) {
                    videoWrapper.requestFullscreen();
                } else if (videoWrapper.webkitRequestFullscreen) { /* Safari */
                    videoWrapper.webkitRequestFullscreen();
                } else if (videoWrapper.msRequestFullscreen) { /* IE11 */
                    videoWrapper.msRequestFullscreen();
                }
            }
        });
    }
    
    // Обработка ошибок изображения
    if (mjpegStream) {
        mjpegStream.addEventListener('error', function(e) {
            console.error('Ошибка загрузки видеопотока:', e);
            // В случае ошибки показываем заглушку
            mjpegStream.src = 'data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22100%25%22%20height%3D%22100%25%22%3E%3Crect%20width%3D%22100%25%22%20height%3D%22100%25%22%20fill%3D%22%23333%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20font-family%3D%22Arial%22%20font-size%3D%2220%22%20fill%3D%22%23fff%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3EОшибка загрузки видео%3C%2Ftext%3E%3C%2Fsvg%3E';
            
            // Пробуем восстановить соединение через 5 секунд
            setTimeout(() => {
                mjpegStream.src = 'http://192.168.0.183:5000/video_feed';
            }, 5000);
        });
    }
    
    // Обработчики для RGB-ленты
    const colorPicker = document.getElementById('color-picker');
    const brightnessSlider = document.getElementById('brightness-slider');
    const brightnessValue = document.getElementById('brightness-value');
    const colorPreview = document.getElementById('rgb-color-preview');
    const toggleButton = document.getElementById('toggle-rgb');
    const statusSpan = document.getElementById('status-rgb');
    
    // Инициализация начальных значений
    let currentColor = '#ff0000';
    let currentBrightness = 100;
    let isRgbOn = false;
    
    colorPicker.value = currentColor;
    colorPreview.style.backgroundColor = currentColor;
    
    // Добавляем обработчик клика на кружок цвета для открытия палитры
    colorPreview.addEventListener('click', () => {
        colorPicker.click();
    });
    
    // Скрываем стандартный селектор цвета (он будет активироваться по клику на кружок)
    colorPicker.style.display = 'none';
    
    // Функция отправки настроек RGB на сервер
    async function sendRgbSettings() {
        if (!isRgbOn) return;
        
        try {
            const response = await fetch(
                // 'http://192.168.0.139:8080/api/rgb',
                'http://192.168.1.244:8080/api/rgb',
                {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    color: currentColor,
                    brightness: currentBrightness.toString()
                })
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }
            
            const data = await response.json();
            console.log('Настройки RGB применены:', data);
            
        } catch (error) {
            console.error('Ошибка при настройке RGB:', error);
        }
    }
    
    // Обработчик изменения цвета с задержкой для уменьшения количества запросов
    let colorTimeout;
    colorPicker.addEventListener('input', (e) => {
        currentColor = e.target.value;
        
        // Применяем яркость к предпросмотру
        const rgb = hexToRgb(currentColor);
        if (rgb) {
            const dimmedColor = applyBrightness(rgb, currentBrightness);
            colorPreview.style.backgroundColor = `rgb(${dimmedColor.r}, ${dimmedColor.g}, ${dimmedColor.b})`;
        }
        
        clearTimeout(colorTimeout);
        colorTimeout = setTimeout(() => {
            sendRgbSettings();
        }, 200); // Отправляем через 200мс после окончания выбора цвета
    });
    
    // Обработчик изменения яркости с задержкой
    let brightnessTimeout;
    brightnessSlider.addEventListener('input', (e) => {
        currentBrightness = e.target.value;
        brightnessValue.textContent = `${currentBrightness}%`;
        
        // Применяем яркость к предпросмотру
        const rgb = hexToRgb(currentColor);
        if (rgb) {
            const dimmedColor = applyBrightness(rgb, currentBrightness);
            colorPreview.style.backgroundColor = `rgb(${dimmedColor.r}, ${dimmedColor.g}, ${dimmedColor.b})`;
        }
        
        clearTimeout(brightnessTimeout);
        brightnessTimeout = setTimeout(() => {
            sendRgbSettings();
        }, 200); // Отправляем через 200мс после окончания движения ползунка
    });
    
    // Кнопка включения/выключения
    toggleButton.addEventListener('click', async () => {
        try {
            const isTurningOn = toggleButton.textContent === 'Включить';
            
            const response = await fetch(
                // `http://192.168.0.139:8080/api/device`,
                `http://192.168.1.244:8080/api/device`,
                {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    device: 'rgb',
                    toggle: isTurningOn ? 'on' : 'off'
                })
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Обновляем состояние
            isRgbOn = data.status;
            statusSpan.textContent = isRgbOn ? 'Работает' : 'Не работает';
            statusSpan.classList.toggle('on', isRgbOn);
            statusSpan.classList.toggle('off', !isRgbOn);
            
            // Обновляем текст кнопки
            toggleButton.textContent = isRgbOn ? 'Выключить' : 'Включить';
            toggleButton.classList.toggle('off', isRgbOn);
            
            // Если включили, отправляем текущие настройки цвета и яркости
            if (isRgbOn) {
                sendRgbSettings();
            }
            
        } catch (error) {
            console.error('Ошибка при управлении RGB:', error);
            alert('Не удалось изменить состояние RGB-ленты. Проверьте соединение с сервером.');
        }
    });
    
    // Функция для преобразования HEX в RGB
    function hexToRgb(hex) {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : null;
    }
    
    // Функция для применения яркости к RGB цвету
    function applyBrightness(rgb, brightness) {
        const factor = brightness / 100;
        return {
            r: Math.round(rgb.r * factor),
            g: Math.round(rgb.g * factor),
            b: Math.round(rgb.b * factor)
        };
    }
    
    // Регулярное обновление статуса RGB-ленты
    async function updateRgbStatus() {
        try {
            const response = await fetch(
                // 'http://192.168.0.139:8080/api/device',
                'http://192.168.0.244:8080/api/device',
                {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    device: 'rgb',
                    toggle: ''
                })
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Обновляем состояние
            isRgbOn = data.status;
            statusSpan.textContent = isRgbOn ? 'Работает' : 'Не работает';
            statusSpan.classList.toggle('on', isRgbOn);
            statusSpan.classList.toggle('off', !isRgbOn);
            
            // Обновляем текст кнопки
            toggleButton.textContent = isRgbOn ? 'Выключить' : 'Включить';
            toggleButton.classList.toggle('off', isRgbOn);
            
        } catch (error) {
            console.error('Ошибка при обновлении статуса RGB:', error);
        }
    }
    
    // Запускаем периодическое обновление статуса
    setInterval(updateRgbStatus, 5000);
    updateRgbStatus();
    
    // Работа с логами RFID
    const logsButton = document.getElementById('rfid-logs-btn');
    const modal = document.getElementById('rfid-logs-modal');
    const closeModalButton = document.getElementById('close-modal');
    const logsContent = document.getElementById('rfid-logs-content');
    
    // Открытие модального окна и загрузка логов
    logsButton.addEventListener('click', async function() {
        modal.classList.add('active');
        await loadRfidLogs();
    });
    
    // Закрытие модального окна
    closeModalButton.addEventListener('click', function() {
        modal.classList.remove('active');
    });
    
    // Закрытие модального окна при клике вне его содержимого
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.classList.remove('active');
        }
    });
    
    // Загрузка логов RFID
    async function loadRfidLogs() {
        try {
            logsContent.innerHTML = '<p class="loading">Загрузка логов...</p>';
            
            const response = await fetch(
                // 'http://192.168.0.139:8080/rfid/logs'
                'http://192.168.0.244:8080/rfid/logs'
            );
            
            if (!response.ok) {
                throw new Error(`Ошибка загрузки логов: ${response.status}`);
            }
            
            const data = await response.json();
            const list = data.list || [];
            
            // Отображение логов
            displayRfidLogs(list);
            
        } catch (error) {
            console.error('Ошибка при загрузке логов RFID:', error);
            logsContent.innerHTML = `<p class="error">Ошибка загрузки логов: ${error.message}</p>`;
        }
    }
    
    // Отображение логов RFID
    function displayRfidLogs(logs) {
        if (!logs || logs.length === 0) {
            logsContent.innerHTML = '<p class="no-logs-message">История сканирований пуста</p>';
            return;
        }
        
        const logsList = document.createElement('ul');
        logsList.classList.add('rfid-logs-list');
        
        logs.forEach(log => {
            const logItem = document.createElement('li');
            logItem.classList.add('rfid-log-item');
            
            // Создаем содержимое для записи лога
            let logContent = `<div class="rfid-log-tag">Метка: ${log.tag}</div>`;
            
            if (log.timestamp) {
                const date = new Date(parseInt(log.timestamp));
                logContent += `<div class="rfid-log-time">Время: ${date.toLocaleString()}</div>`;
            }
            
            logItem.innerHTML = logContent;
            logsList.appendChild(logItem);
        });
        
        logsContent.innerHTML = '';
        logsContent.appendChild(logsList);
    }
    
    // Управление машинкой с клавиатуры
    // Флаги для отслеживания нажатых клавиш
    const keysPressed = {
        'w': false, // Вперед
        'a': false, // Влево
        's': false, // Назад
        'd': false, // Вправо
        'ц': false, // Вперед (рус)
        'ф': false, // Влево (рус)
        'ы': false, // Назад (рус)
        'в': false  // Вправо (рус)
    };
    
    // Карта соответствия русских клавиш английским
    const russianToEnglish = {
        'ц': 'w',
        'ф': 'a',
        'ы': 's',
        'в': 'd'
    };
    
    // Обработчик нажатия клавиш
    document.addEventListener('keydown', function(event) {
        const key = event.key.toLowerCase();
        
        // Проверяем, есть ли клавиша в списке отслеживаемых
        if (keysPressed.hasOwnProperty(key) && !keysPressed[key]) {
            keysPressed[key] = true;
            
            // Определяем, какую команду отправлять (перевод с русской раскладки)
            const commandKey = russianToEnglish[key] || key;
            
            // Подсвечиваем клавишу в интерфейсе
            highlightKey(commandKey, true);
            
            // Отправляем команду
            sendVehicleCommand(commandKey, 'pressed');
            
            // Если нажата русская клавиша, также помечаем соответствующую латинскую как нажатую
            if (russianToEnglish[key]) {
                keysPressed[russianToEnglish[key]] = true;
            }
        }
    });
    
    // Обработчик отпускания клавиш
    document.addEventListener('keyup', function(event) {
        const key = event.key.toLowerCase();
        
        // Проверяем, есть ли клавиша в списке отслеживаемых
        if (keysPressed.hasOwnProperty(key) && keysPressed[key]) {
            keysPressed[key] = false;
            
            // Определяем, какую команду отправлять (перевод с русской раскладки)
            const commandKey = russianToEnglish[key] || key;
            
            // Убираем подсветку клавиши в интерфейсе
            highlightKey(commandKey, false);
            
            // Отправляем команду
            sendVehicleCommand(commandKey, 'released');
            
            // Если отпущена русская клавиша, также помечаем соответствующую латинскую как отпущенную
            if (russianToEnglish[key]) {
                keysPressed[russianToEnglish[key]] = false;
            }
        }
    });
    
    // Функция подсветки клавиши в интерфейсе
    function highlightKey(key, isActive) {
        const keyElement = document.querySelector(`.vehicle-key[data-key="${key}"]`);
        if (keyElement) {
            if (isActive) {
                keyElement.classList.add('active');
            } else {
                keyElement.classList.remove('active');
            }
        }
    }
    
    // Отправка команды управления на сервер
    async function sendVehicleCommand(key, action) {
        try {
            console.log(`Отправляем команду: ${key} - ${action}`);
            
            const response = await fetch(
                // 'http://192.168.0.139:8080/api/vehicle',
                'http://192.168.0.244:8080/api/vehicle',
                {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    key: key,
                    action: action
                })
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка отправки команды: ${response.status}`);
            }
            
            console.log(`Отправлена команда: ${key} - ${action}`);
        } catch (error) {
            console.error('Ошибка при отправке команды управления:', error);
        }
    }
    
    // Создаем визуальную индикацию на странице
    function createVehicleControls() {
        const controlsContainer = document.createElement('div');
        controlsContainer.className = 'vehicle-controls-container';
        
        // Заголовок
        const controlsTitle = document.createElement('h3');
        controlsTitle.textContent = 'Управление машинкой';
        controlsContainer.appendChild(controlsTitle);
        
        // Создаем контейнер для клавиш
        const keysContainer = document.createElement('div');
        keysContainer.className = 'vehicle-keys';
        
        // Создаем клавиши
        const keys = [
            { key: 'w', label: 'W', ruLabel: 'Ц', position: 'top' },
            { key: 'a', label: 'A', ruLabel: 'Ф', position: 'left' },
            { key: 's', label: 'S', ruLabel: 'Ы', position: 'bottom' },
            { key: 'd', label: 'D', ruLabel: 'В', position: 'right' },
            { key: 'left', label: '←', ruLabel: '', position: 'turn-left' },
            { key: 'right', label: '→', ruLabel: '', position: 'turn-right' }
        ];
        
        keys.forEach(keyInfo => {
            const keyElement = document.createElement('div');
            keyElement.className = `vehicle-key ${keyInfo.position}`;
            keyElement.dataset.key = keyInfo.key;
            
            // Добавляем основную метку
            keyElement.textContent = keyInfo.label;
            
            // Если есть русская метка, добавляем её
            if (keyInfo.ruLabel) {
                const ruSpan = document.createElement('span');
                ruSpan.className = 'ru-key';
                ruSpan.textContent = keyInfo.ruLabel;
                keyElement.appendChild(ruSpan);
            }
            
            // Для кнопок поворота используем другой URL
            const isServoControl = ['left', 'right'].includes(keyInfo.key);
            // const apiUrl = isServoControl ? 'http://192.168.0.139:8080/api/servo' : 'http://192.168.0.139:8080/api/vehicle';
            const apiUrl = isServoControl ? 'http://192.168.0.244:8080/api/servo' : 'http://192.168.0.244:8080/api/vehicle';

            // При клике на клавишу симулируем нажатие/отпускание
            keyElement.addEventListener('mousedown', () => {
                if (!keysPressed[keyInfo.key]) {
                    keysPressed[keyInfo.key] = true;
                    keyElement.classList.add('active');
                    if (isServoControl) {
                        sendServoCommand(keyInfo.key, 'pressed');
                    } else {
                        sendVehicleCommand(keyInfo.key, 'pressed');
                    }
                }
            });
            
            keyElement.addEventListener('mouseup', () => {
                if (keysPressed[keyInfo.key]) {
                    keysPressed[keyInfo.key] = false;
                    keyElement.classList.remove('active');
                    if (isServoControl) {
                        sendServoCommand(keyInfo.key, 'released');
                    } else {
                        sendVehicleCommand(keyInfo.key, 'released');
                    }
                }
            });
            
            // Обработка случая, когда мышь покидает кнопку во время нажатия
            keyElement.addEventListener('mouseleave', () => {
                if (keysPressed[keyInfo.key]) {
                    keysPressed[keyInfo.key] = false;
                    keyElement.classList.remove('active');
                    if (isServoControl) {
                        sendServoCommand(keyInfo.key, 'released');
                    } else {
                        sendVehicleCommand(keyInfo.key, 'released');
                    }
                }
            });
            
            keysContainer.appendChild(keyElement);
        });
        
        controlsContainer.appendChild(keysContainer);
        
        // Добавляем на страницу под видео
        document.getElementById('vehicle-controls-placeholder').appendChild(controlsContainer);
        
        console.log('Элементы управления машинкой созданы');
    }
    
    // Отправка команды поворота сервопривода на сервер
    async function sendServoCommand(key, action) {
        try {
            console.log(`Отправляем команду сервопривода: ${key} - ${action}`);
            
            const response = await fetch(
                // 'http://192.168.0.139:8080/api/servo',
                'http://192.168.0.244:8080/api/servo',
                {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    key: key,
                    action: action
                })
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка отправки команды сервопривода: ${response.status}`);
            }
            
            console.log(`Отправлена команда сервопривода: ${key} - ${action}`);
        } catch (error) {
            console.error('Ошибка при отправке команды сервопривода:', error);
        }
    }
    
    // Обработчик нажатия клавиш для поворота сервопривода
    document.addEventListener('keydown', function(event) {
        const key = event.key.toLowerCase();
        
        // Обработка стрелок влево/вправо
        if ((key === 'arrowleft' || key === 'arrowright') && !keysPressed[key]) {
            const servoKey = key === 'arrowleft' ? 'left' : 'right';
            keysPressed[servoKey] = true;
            
            // Подсвечиваем клавишу в интерфейсе
            highlightKey(servoKey, true);
            
            // Отправляем команду
            sendServoCommand(servoKey, 'pressed');
        }
    });
    
    // Обработчик отпускания клавиш для поворота сервопривода
    document.addEventListener('keyup', function(event) {
        const key = event.key.toLowerCase();
        
        // Обработка стрелок влево/вправо
        if (key === 'arrowleft' || key === 'arrowright') {
            const servoKey = key === 'arrowleft' ? 'left' : 'right';
            if (keysPressed[servoKey]) {
                keysPressed[servoKey] = false;
                
                // Убираем подсветку клавиши в интерфейсе
                highlightKey(servoKey, false);
                
                // Отправляем команду
                sendServoCommand(servoKey, 'released');
            }
        }
    });
    
    // Добавляем элементы управления на страницу
    createVehicleControls();
    
    // Предотвращаем действия браузера по умолчанию для клавиш управления
    window.addEventListener('keydown', function(e) {
        const key = e.key.toLowerCase();
        if (['w', 'a', 's', 'd', 'ц', 'ф', 'ы', 'в', 'arrowleft', 'arrowright'].includes(key)) {
            e.preventDefault();
        }
    });
    
    // Обработчик слайдера яркости фонарика
    const flashSlider = document.getElementById('flash-slider');
    const flashValue = document.getElementById('flash-value');
    
    if (flashSlider && flashValue) {
        let flashTimeout;
        
        flashSlider.addEventListener('input', function(e) {
            const brightnessValue = e.target.value;
            flashValue.textContent = `${brightnessValue}%`;
            
            // Очищаем предыдущий таймаут, чтобы не отправлять слишком много запросов
            clearTimeout(flashTimeout);
            
            // Устанавливаем новый таймаут для отправки запроса
            flashTimeout = setTimeout(() => {
                updateFlashBrightness(brightnessValue);
            }, 100);
        });
        
        // Функция для отправки значения яркости фонарика на сервер
        async function updateFlashBrightness(brightness) {
            try {
                console.log(`Обновляем яркость фонарика: ${brightness}%`);
                
                const response = await fetch(
                    // 'http://192.168.0.139:8080/api/flash',
                    'http://192.168.0.244:8080/api/flash',
                    {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        brightness: parseInt(brightness)
                    })
                });
                
                if (!response.ok) {
                    throw new Error(`Ошибка обновления яркости фонарика: ${response.status}`);
                }
                
                console.log(`Яркость фонарика обновлена: ${brightness}%`);
            } catch (error) {
                console.error('Ошибка при обновлении яркости фонарика:', error);
            }
        }
    }
});

// Список датчиков с описаниями
const sensors = [
    { id: 'temp', name: 'Температура (AMP-B045)', format: '°C', color: 'rgba(255, 99, 132, 1)', bgColor: 'rgba(255, 99, 132, 0.2)' },
    { id: 'humid', name: 'Влажность (AMP-B045)', format: '%', color: 'rgba(54, 162, 235, 1)', bgColor: 'rgba(54, 162, 235, 0.2)' }
];

// Хранилище для данных графиков
const sensorData = {
    temp: { values: [], labels: [] },
    humid: { values: [], labels: [] }
};

// Объекты графиков
const charts = {};

// Максимальное количество точек на графике
const MAX_DATA_POINTS = 20;

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

// Инициализация графиков
function initCharts() {
    sensors.forEach(sensor => {
        const ctx = document.getElementById(`${sensor.id}-chart`);
        if (!ctx) return;
        
        charts[sensor.id] = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: `${sensor.name} (${sensor.format})`,
                    data: [],
                    borderColor: sensor.color,
                    backgroundColor: sensor.bgColor,
                    borderWidth: 2,
                    tension: 0.2,
                    fill: true,
                    pointRadius: 3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false,  // Убираем легенду, так как заголовок графика находится в h3
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        ticks: {
                            font: {
                                size: 10
                            }
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 0,
                            font: {
                                size: 10
                            }
                        }
                    }
                },
                animation: {
                    duration: 500
                }
            }
        });
    });
}

// Функция обновления графика датчика
function updateChart(sensorId, value) {
    if (!charts[sensorId]) return;
    
    const now = new Date();
    const timeLabel = now.toLocaleTimeString();
    
    // Добавляем новые данные
    sensorData[sensorId].values.push(parseFloat(value));
    sensorData[sensorId].labels.push(timeLabel);
    
    // Ограничиваем количество точек на графике
    if (sensorData[sensorId].values.length > MAX_DATA_POINTS) {
        sensorData[sensorId].values.shift();
        sensorData[sensorId].labels.shift();
    }
    
    // Обновляем данные графика
    charts[sensorId].data.labels = sensorData[sensorId].labels;
    charts[sensorId].data.datasets[0].data = sensorData[sensorId].values;
    
    // Перерисовываем график
    charts[sensorId].update();
}

// Обновление значений датчиков
async function updateSensorValues() {
    try {
        const response = await fetch(
            // 'http://192.168.0.139:8080/api/get/data'
            'http://192.168.0.244:8080/api/get/data'
        );
        const data = await response.json();

        sensors.forEach(sensor => {
            const valueElement = document.getElementById(`${sensor.id}-value`);
            if (valueElement) {
                const value = data[sensor.id];
                
                // Форматируем значение датчика
                valueElement.textContent = `${value} ${sensor.format}`;
                
                // Обновляем график
                updateChart(sensor.id, value);
            }
        });
    } catch (error) {
        console.error('Ошибка при обновлении значений датчиков:', error);
    }
}

// Первоначальная генерация списка и инициализация графиков
document.addEventListener('DOMContentLoaded', function() {
    // Генерация списка датчиков (если не было сделано ранее)
    if (sensorListElement && sensorListElement.children.length === 0) {
        generateSensorsList();
    }
    
    // Инициализация графиков
    initCharts();
});

// Запускаем периодическое обновление данных (уже есть в основном коде)
// Первоначальное обновление данных
updateSensorValues();
setInterval(updateSensorValues, 1000);

// Контейнер для уведомлений
const notificationContainer = document.getElementById('notification-container');

// Функция для создания уведомления
function createNotification(title, message) {
    const notification = document.createElement('div');
    notification.classList.add('notification');
    notification.innerHTML = `
        <div class="title">${title}</div>
        <div>${message}</div>
    `;
    notificationContainer.appendChild(notification);

    // Удаляем уведомление после завершения анимации
    setTimeout(() => {
        notification.remove();
    }, 5000);
}

// Подписка на уведомления
async function subscribeToNotifications() {
    // const url = "http://192.168.0.139:8080/api/alert";
    const url = "http://192.168.1.244:8080/api/alert";
    try {
        const response = await fetch(url);
        if (!response.ok) {
            console.error("Ошибка получения уведомлений:", response.statusText);
            setTimeout(subscribeToNotifications, 5000);
        }

        const data = await response.json();

        // Проверка типа уведомления
        if (data.type === "gas") {
            createNotification(
                "Предупреждение о загазованности",
                "Обнаружено превышение нормы загазованности. Проветрите помещение!"
            );
            setTimeout(subscribeToNotifications, 5000);
        }
    } catch (error) {
        console.error("Ошибка подписки на уведомления:", error);
        setTimeout(subscribeToNotifications, 5000);
    }
}

// Запуск подписки
subscribeToNotifications();

// Функция для работы с RFID
async function updateRfidInfo() {
    try {
        const response = await fetch(
            // 'http://192.168.0.139:8080/rfid/logs/latest'
            'http://192.168.0.244:8080/rfid/logs/latest'
        );
        const data = await response.json();
        
        const rfidValueElement = document.getElementById('rfid-value');
        const rfidDetailsElement = document.getElementById('rfid-details');
        
        if (!rfidValueElement || !rfidDetailsElement) return;
        
        if (data.tag) {
            // Если метка считана
            rfidValueElement.textContent = data.tag;
            
            // Отображаем дополнительную информацию
            rfidDetailsElement.innerHTML = '';
            
            // Добавляем время сканирования
            if (data.timestamp) {
                const timeDiv = document.createElement('div');
                timeDiv.classList.add('rfid-detail-item');
                timeDiv.innerHTML = `
                    <span class="rfid-detail-label">Время:</span>
                    <span>${new Date(parseInt(data.timestamp)).toLocaleString()}</span>
                `;
                rfidDetailsElement.appendChild(timeDiv);
            }
        } else {
            // Если метка не считана
            rfidValueElement.textContent = 'Ожидание сканирования...';
            rfidDetailsElement.innerHTML = '';
        }
    } catch (error) {
        console.error('Ошибка при получении данных RFID:', error);
    }
}

// Обновление данных RFID каждые 2 секунды
setInterval(updateRfidInfo, 2000);
updateRfidInfo(); // Первоначальное обновление