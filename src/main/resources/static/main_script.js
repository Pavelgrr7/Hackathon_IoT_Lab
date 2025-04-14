// Функциональность видеоплеера MJPEG
document.addEventListener('DOMContentLoaded', function() {
    // Переменная для хранения текущей активной камеры
    let activeCamera = 1;
    
    // URL видеопотоков с камер
    const cameraStreams = {
        1: 'http://192.168.0.202:81/stream', // Камера машинки
        2: 'http://192.168.0.183:5000/video_feed'  // Камера шахты
    };
    
    // Проверка, находимся ли в режиме гостя
    const isGuestMode = localStorage.getItem('isGuestMode') === 'true';
    
    // Если режим гостя, скрываем элементы управления
    if (isGuestMode) {
        applyGuestMode();
    }
    
    // Видеоплеер MJPEG
    const mjpegStream = document.getElementById('mjpeg-stream');
    const fullscreenBtn = document.getElementById('fullscreen-toggle');
    
    // Кнопки переключения камер
    const camera1Btn = document.getElementById('camera1-btn');
    const camera2Btn = document.getElementById('camera2-btn');
    
    // Устанавливаем исходный URL видеопотока соответственно активной камере
    if (mjpegStream) {
        mjpegStream.src = cameraStreams[activeCamera];
        console.log(`Инициализирован видеопоток для камеры ${activeCamera}: ${cameraStreams[activeCamera]}`);
    }
    
    // Обработчики для кнопок переключения камер
    camera1Btn.addEventListener('click', () => switchCamera(1));
    camera2Btn.addEventListener('click', () => switchCamera(2));
    
    // Функция переключения камер
    function switchCamera(cameraId) {
        if (activeCamera === cameraId) return; // Нет необходимости переключать ту же камеру
        
        // Проверяем существование потока для выбранной камеры
        if (!cameraStreams[cameraId]) {
            console.error(`Ошибка: поток для камеры ${cameraId} не определен`);
            return;
        }
        
        activeCamera = cameraId;
        
        // Обновляем активный класс кнопок
        if (camera1Btn && camera2Btn) {
            camera1Btn.classList.toggle('active', cameraId === 1);
            camera2Btn.classList.toggle('active', cameraId === 2);
        }
        
        // Устанавливаем новый источник видеопотока
        if (mjpegStream) {
            // Сбрасываем текущий поток для исключения проблем с кешированием
            mjpegStream.src = '';
            // Устанавливаем новый URL
            setTimeout(() => {
                mjpegStream.src = cameraStreams[cameraId];
            }, 50);
        }
        
        // Показываем/скрываем элементы управления в зависимости от активной камеры
        updateControlsVisibility();
        
        console.log(`Переключено на камеру ${cameraId}: ${cameraStreams[cameraId]}`);
    }
    
    // Функция обновления видимости элементов управления
    function updateControlsVisibility() {
        console.log(`Обновление элементов управления для камеры ${activeCamera}`);
        
        const vehicleControls = document.getElementById('vehicle-controls-placeholder');
        const vehicleKeys = document.querySelectorAll('.vehicle-key:not(.turn-left):not(.turn-right)');
        
        if (activeCamera === 1) {
            // Камера 1 (машинка) - показываем все элементы управления
            if (vehicleControls) vehicleControls.style.display = 'block';
            vehicleKeys.forEach(key => key.style.display = 'flex');
            
            // Возвращаем обработчики событий для машинки
            setKeyHandlersForVehicle();
            console.log('Активированы органы управления для машинки');
        } else {
            // Камера 2 (шахта) - скрываем элементы управления машинкой, оставляя повороты
            if (vehicleControls) vehicleControls.style.display = 'block';
            vehicleKeys.forEach(key => key.style.display = 'none');
            
            // Меняем обработчики событий для шахты
            setKeyHandlersForServo2();
            console.log('Активированы органы управления для шахты');
        }
        
        // Обновляем заголовок блока управления
        const controlsTitle = document.querySelector('.vehicle-controls-container h3');
        if (controlsTitle) {
            controlsTitle.textContent = activeCamera === 1 ? 'Управление машинкой' : 'Управление шахтой';
            console.log('Обновлен заголовок блока управления: ' + controlsTitle.textContent);
        }
        
        // Если находимся в режиме гостя, применяем ограничения гостевого режима
        if (isGuestMode) {
            applyGuestMode();
        }
    }
    
    // Функция установки обработчиков для управления машинкой (камера 1)
    function setKeyHandlersForVehicle() {
        // Эта функция не требует изменений, так как обработчики уже установлены
        // Просто меняем URL для API фонарика, если нужно
        flashTargetDevice = 'flash'; // Для API использовать /api/flash
    }
    
    // Функция установки обработчиков для управления шахтой (камера 2)
    function setKeyHandlersForServo2() {
        // Изменяем целевое устройство для фонарика
        flashTargetDevice = 'flash2'; // Для API использовать /api/flash2
        
        // Обработчики для стрелок уже установлены в другом месте
        // Но нужно изменить логику отправки команд
    }
    
    // Переменная для целевого устройства фонарика
    let flashTargetDevice = 'flash';
    
    // Кнопка полноэкранного режима
    if (fullscreenBtn) {
        fullscreenBtn.addEventListener('click', function() {
            const videoWrapper = document.querySelector('.video-player-wrapper');
            
            // Проверяем, находимся ли мы в полноэкранном режиме
            const isFullScreen = Boolean(
                document.fullscreenElement || 
                document.webkitFullscreenElement || 
                document.mozFullScreenElement ||
                document.msFullscreenElement
            );
            
            if (isFullScreen) {
                // Выходим из полноэкранного режима
                if (document.exitFullscreen) {
                    document.exitFullscreen();
                } else if (document.webkitExitFullscreen) { /* Safari */
                    document.webkitExitFullscreen();
                } else if (document.msExitFullscreen) { /* IE11 */
                    document.msExitFullscreen();
                } else if (document.mozCancelFullScreen) { /* Firefox */
                    document.mozCancelFullScreen();
                }
                console.log('Выход из полноэкранного режима');
            } else if (videoWrapper) {
                // Входим в полноэкранный режим
                if (videoWrapper.requestFullscreen) {
                    videoWrapper.requestFullscreen();
                } else if (videoWrapper.webkitRequestFullscreen) { /* Safari */
                    videoWrapper.webkitRequestFullscreen();
                } else if (videoWrapper.msRequestFullscreen) { /* IE11 */
                    videoWrapper.msRequestFullscreen();
                } else if (videoWrapper.mozRequestFullScreen) { /* Firefox */
                    videoWrapper.mozRequestFullScreen();
                }
                console.log('Переход в полноэкранный режим');
            }
        });
        
        // Отслеживаем изменения состояния полноэкранного режима
        document.addEventListener('fullscreenchange', updateFullscreenUI);
        document.addEventListener('webkitfullscreenchange', updateFullscreenUI);
        document.addEventListener('mozfullscreenchange', updateFullscreenUI);
        document.addEventListener('MSFullscreenChange', updateFullscreenUI);
        
        // Функция обновления интерфейса при изменении состояния полноэкранного режима
        function updateFullscreenUI() {
            const isFullScreen = Boolean(
                document.fullscreenElement || 
                document.webkitFullscreenElement || 
                document.mozFullScreenElement ||
                document.msFullscreenElement
            );
            
            // Обновляем иконку кнопки в зависимости от состояния
            const icon = fullscreenBtn.querySelector('.icon');
            if (icon) {
                icon.textContent = isFullScreen ? '⤓' : '⤢'; // Разные символы для разных состояний
            }
            
            console.log('Состояние полноэкранного режима изменено:', isFullScreen);
        }
    }
    
    // Обработка ошибок изображения
    if (mjpegStream) {
        mjpegStream.addEventListener('error', function(e) {
            console.error('Ошибка загрузки видеопотока:', e);
            // В случае ошибки показываем заглушку
            mjpegStream.src = 'data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22100%25%22%20height%3D%22100%25%22%3E%3Crect%20width%3D%22100%25%22%20height%3D%22100%25%22%20fill%3D%22%23333%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20font-family%3D%22Arial%22%20font-size%3D%2220%22%20fill%3D%22%23fff%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3EОшибка загрузки видео%3C%2Ftext%3E%3C%2Fsvg%3E';
            
            // Пробуем восстановить соединение через 5 секунд с правильным URL активной камеры
            setTimeout(() => {
                mjpegStream.src = cameraStreams[activeCamera];
                console.log(`Повторная попытка загрузки видеопотока для камеры ${activeCamera}: ${cameraStreams[activeCamera]}`);
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
                'http://192.168.0.139:8080/api/rgb',
                // 'http://192.168.1.244:8080/api/rgb',
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
                `http://192.168.0.139:8080/api/device`,
                // `http://192.168.1.244:8080/api/device`,
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
    
    // Регулярное обновление статуса RGB-ленты - заменяем на подписку
    async function subscribeToRgbChanges() {
        try {
            console.log('Подписываемся на изменения RGB-ленты');
            const response = await fetch(
                'http://192.168.0.139:8080/api/get/rgb',
                // 'http://192.168.1.244:8080/api/get/rgb',
                {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            if (!response.ok) {
                throw new Error(`Ошибка подписки: ${response.status}`);
            }
            
            const data = await response.json();
            console.log('Получены обновленные данные RGB от сервера:', data);
            
            // Обновляем состояние вкл/выкл
            isRgbOn = data.toggle === 'on';
            statusSpan.textContent = isRgbOn ? 'Работает' : 'Не работает';
            statusSpan.classList.toggle('on', isRgbOn);
            statusSpan.classList.toggle('off', !isRgbOn);
            
            // Обновляем текст кнопки
            toggleButton.textContent = isRgbOn ? 'Выключить' : 'Включить';
            toggleButton.classList.toggle('off', isRgbOn);
            
            // Обновляем значение цвета, если оно изменилось
            if (data.color && data.color !== currentColor) {
                console.log(`Обновляем цвет: ${currentColor} → ${data.color}`);
                currentColor = data.color;
                colorPicker.value = currentColor;
                
                // Применяем яркость к предпросмотру
                const rgb = hexToRgb(currentColor);
                if (rgb) {
                    const dimmedColor = applyBrightness(rgb, currentBrightness);
                    colorPreview.style.backgroundColor = `rgb(${dimmedColor.r}, ${dimmedColor.g}, ${dimmedColor.b})`;
                }
            }
            
            // Обновляем значение яркости, если оно изменилось
            if (data.brightness && parseInt(data.brightness) !== currentBrightness) {
                console.log(`Обновляем яркость: ${currentBrightness} → ${data.brightness}`);
                currentBrightness = parseInt(data.brightness);
                brightnessSlider.value = currentBrightness;
                brightnessValue.textContent = `${currentBrightness}%`;
                
                // Применяем яркость к предпросмотру
                const rgb = hexToRgb(currentColor);
                if (rgb) {
                    const dimmedColor = applyBrightness(rgb, currentBrightness);
                    colorPreview.style.backgroundColor = `rgb(${dimmedColor.r}, ${dimmedColor.g}, ${dimmedColor.b})`;
                }
            }
            
            // После получения обновления сразу подписываемся снова
            setTimeout(subscribeToRgbChanges, 5000);
            
        } catch (error) {
            console.error('Ошибка при получении обновлений RGB:', error);
            // При ошибке пытаемся переподписаться через 5 секунд
            setTimeout(subscribeToRgbChanges, 5000);
        }
    }
    
    // Запускаем подписку на обновления вместо таймера
    subscribeToRgbChanges();
    
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
                'http://192.168.0.139:8080/rfid/logs'
                // 'http://192.168.1.244:8080/rfid/logs'
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
            
            // Проверяем, активна ли первая камера для отправки команд WASD
            if (activeCamera === 1) {
                // Отправляем команду для машинки
                sendVehicleCommand(commandKey, 'pressed');
            }
            
            // Если нажата русская клавиша, также помечаем соответствующую латинскую как нажатую
            if (russianToEnglish[key]) {
                keysPressed[russianToEnglish[key]] = true;
            }
        }
        
        // Обработка стрелок влево/вправо для управления сервоприводами
        if ((key === 'arrowleft' || key === 'arrowright') && !keysPressed[key]) {
            const servoKey = key === 'arrowleft' ? 'left' : 'right';
            keysPressed[servoKey] = true;
            
            // Подсвечиваем клавишу в интерфейсе
            highlightKey(servoKey, true);
            
            // Отправляем команду
            sendServoCommand(servoKey, 'pressed');
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
            
            // Проверяем, активна ли первая камера для отправки команд WASD
            if (activeCamera === 1) {
                // Отправляем команду для машинки
                sendVehicleCommand(commandKey, 'released');
            }
            
            // Если отпущена русская клавиша, также помечаем соответствующую латинскую как отпущенную
            if (russianToEnglish[key]) {
                keysPressed[russianToEnglish[key]] = false;
            }
        }
        
        // Обработка стрелок влево/вправо для управления сервоприводами
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
                'http://192.168.0.139:8080/api/vehicle',
                // 'http://192.168.1.244:8080/api/vehicle',
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
        controlsTitle.textContent = 'Управление';
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
        const vehicleControlsPlaceholder = document.getElementById('vehicle-controls-placeholder');
        if (vehicleControlsPlaceholder) {
            vehicleControlsPlaceholder.appendChild(controlsContainer);
        } else {
            console.error('Не найден контейнер для элементов управления vehicle-controls-placeholder');
        }
        
        console.log('Элементы управления машинкой созданы');
    }
    
    // Отправка команды поворота сервопривода на сервер
    async function sendServoCommand(key, action) {
        try {
            // Определяем endpoint в зависимости от активной камеры
            const endpoint = activeCamera === 1 ? 'servo' : 'servo2';
            
            console.log(`Отправляем команду сервопривода (${endpoint}): ${key} - ${action}`);
            
            const response = await fetch(
                `http://192.168.0.139:8080/api/${endpoint}`,
                // `http://192.168.1.244:8080/api/${endpoint}`,
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
            
            console.log(`Отправлена команда сервопривода (${endpoint}): ${key} - ${action}`);
        } catch (error) {
            console.error(`Ошибка при отправке команды сервопривода (${endpoint}):`, error);
        }
    }
    
    // Добавляем элементы управления на страницу (если контейнер существует)
    const vehicleControlsPlaceholder = document.getElementById('vehicle-controls-placeholder');
    if (vehicleControlsPlaceholder) {
        createVehicleControls();
    } else {
        console.error('Не найден контейнер для элементов управления vehicle-controls-placeholder');
    }
    
    // Предотвращаем действия браузера по умолчанию для клавиш управления
    window.addEventListener('keydown', function(e) {
        const key = e.key.toLowerCase();
        if (['w', 'a', 's', 'd', 'ц', 'ф', 'ы', 'в', 'arrowleft', 'arrowright'].includes(key)) {
            e.preventDefault();
        }
    });
    
    // Обработчик переключателя фонарика
    const flashToggle = document.getElementById('flash-toggle');
    const flashStatus = document.getElementById('flash-status');
    
    if (flashToggle && flashStatus) {
        // Состояние фонарика
        let isFlashOn = false;
        
        flashToggle.addEventListener('click', function() {
            // Инвертируем состояние
            isFlashOn = !isFlashOn;
            
            // Обновляем UI
            flashToggle.textContent = isFlashOn ? 'Выключить' : 'Включить';
            flashStatus.textContent = isFlashOn ? 'Включен' : 'Выключен';
            flashStatus.classList.toggle('on', isFlashOn);
            flashStatus.classList.toggle('off', !isFlashOn);
            
            // Отправляем команду на сервер
            updateFlashState(isFlashOn ? '1' : '0');
        });
        
        // Функция для отправки состояния фонарика на сервер
        async function updateFlashState(state) {
            try {
                console.log(`Изменение состояния фонарика (${flashTargetDevice}): ${state}`);
                
                const response = await fetch(
                    `http://192.168.0.139:8080/api/${flashTargetDevice}`,
                    // `http://192.168.1.244:8080/api/${flashTargetDevice}`,
                    {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        brightness: state
                    })
                });
                
                if (!response.ok) {
                    throw new Error(`Ошибка изменения состояния фонарика: ${response.status}`);
                }
                
                console.log(`Состояние фонарика (${flashTargetDevice}) изменено: ${state}`);
            } catch (error) {
                console.error(`Ошибка при изменении состояния фонарика (${flashTargetDevice}):`, error);
                // Возвращаем состояние UI обратно в случае ошибки
                isFlashOn = !isFlashOn;
                flashToggle.textContent = isFlashOn ? 'Выключить' : 'Включить';
                flashStatus.textContent = isFlashOn ? 'Включен' : 'Выключен';
                flashStatus.classList.toggle('on', isFlashOn);
                flashStatus.classList.toggle('off', !isFlashOn);
                
                alert('Ошибка изменения состояния фонарика. Проверьте соединение с сервером.');
            }
        }
    }
    
    // Функция для применения режима гостя
    function applyGuestMode() {
        console.log('Применяется режим гостя');
        
        // Скрываем управление машинкой, но оставляем возможность переключения камер
        const vehicleControls = document.getElementById('vehicle-controls-placeholder');
        if (vehicleControls) vehicleControls.style.display = 'none';
        
        // Скрываем кнопку управления фонариком, но оставляем отображение статуса
        const flashControl = document.querySelector('.flash-control');
        if (flashControl) {
            const flashToggle = document.getElementById('flash-toggle');
            if (flashToggle) flashToggle.style.display = 'none';
            
            // Меняем стиль отображения для статуса фонарика
            const flashStatus = document.getElementById('flash-status');
            if (flashStatus) {
                flashStatus.style.marginLeft = '10px';
            }
            
            // Не скрываем .flash-control полностью, чтобы статус оставался видимым
            // flashControl.style.display = 'none';
        }
        
        // Скрываем кнопки управления RGB-лентой, но оставляем видимыми статус, цвет и яркость
        const rgbControls = document.querySelector('.rgb-controls');
        if (rgbControls) {
            // Создаём информационную панель для режима гостя
            const rgbInfoContainer = document.createElement('div');
            rgbInfoContainer.className = 'rgb-guest-info';
            rgbInfoContainer.style.marginTop = '15px';
            rgbInfoContainer.style.padding = '10px';
            rgbInfoContainer.style.backgroundColor = '#f8f9fa';
            rgbInfoContainer.style.borderRadius = '5px';
            
            // Добавляем информацию о текущем цвете
            const colorInfo = document.createElement('div');
            colorInfo.style.display = 'flex';
            colorInfo.style.alignItems = 'center';
            colorInfo.style.marginBottom = '10px';
            
            colorInfo.innerHTML = `
                <span style="margin-right: 10px; font-weight: bold;">Текущий цвет:</span>
                <div id="rgb-guest-color-preview" style="width: 25px; height: 25px; border-radius: 50%; border: 1px solid #ccc;"></div>
            `;
            
            // Добавляем информацию о текущей яркости
            const brightnessInfo = document.createElement('div');
            brightnessInfo.style.display = 'flex';
            brightnessInfo.style.alignItems = 'center';
            
            brightnessInfo.innerHTML = `
                <span style="margin-right: 10px; font-weight: bold;">Текущая яркость:</span>
                <span id="rgb-guest-brightness">0%</span>
            `;
            
            rgbInfoContainer.appendChild(colorInfo);
            rgbInfoContainer.appendChild(brightnessInfo);
            
            // Сохраняем оригинальный статус
            const statusElement = rgbControls.querySelector('.rgb-status');
            
            // Очищаем родительский контейнер и добавляем только нужные элементы
            rgbControls.innerHTML = '';
            rgbControls.appendChild(statusElement);
            rgbControls.appendChild(rgbInfoContainer);
            
            // Устанавливаем начальный цвет превью
            const colorPreview = document.getElementById('rgb-guest-color-preview');
            const brightnessElement = document.getElementById('rgb-guest-brightness');
            
            // Функция обновления информации о цвете и яркости в режиме гостя
            async function subscribeToGuestRgbChanges() {
                // Обновляем данные на основе текущих значений
                colorPreview.style.backgroundColor = currentColor;
                brightnessElement.textContent = `${currentBrightness}%`;
                
                try {
                    console.log('Гостевой режим: подписываемся на изменения RGB-ленты');
                    const response = await fetch(
                        'http://192.168.0.139:8080/api/get/rgb',
                        {
                            method: 'GET',
                            headers: {
                                'Content-Type': 'application/json',
                            }
                        }
                    );
                    
                    if (!response.ok) {
                        throw new Error(`Ошибка подписки в гостевом режиме: ${response.status}`);
                    }
                    
                    const data = await response.json();
                    console.log('Гостевой режим: получены обновленные данные RGB:', data);
                    
                    // Обновляем статус включения/выключения
                    statusElement.textContent = data.toggle === 'on' ? 'Работает' : 'Не работает';
                    statusElement.classList.toggle('on', data.toggle === 'on');
                    statusElement.classList.toggle('off', data.toggle !== 'on');
                    
                    // Обновляем цвет
                    if (data.color) {
                        const rgb = hexToRgb(data.color);
                        if (rgb) {
                            const dimmedColor = applyBrightness(rgb, data.brightness ? parseInt(data.brightness) : 100);
                            colorPreview.style.backgroundColor = `rgb(${dimmedColor.r}, ${dimmedColor.g}, ${dimmedColor.b})`;
                        }
                    }
                    
                    // Обновляем яркость
                    if (data.brightness) {
                        brightnessElement.textContent = `${data.brightness}%`;
                    }
                    
                    // Подписываемся снова
                    setTimeout(subscribeToGuestRgbChanges, 5000);
                    
                } catch (error) {
                    console.error('Ошибка обновления RGB в гостевом режиме:', error);
                    // При ошибке пытаемся переподписаться через 5 секунд
                    setTimeout(subscribeToGuestRgbChanges, 5000);
                }
            }
            
            // Запускаем подписку вместо интервала
            subscribeToGuestRgbChanges();
        }
        
        // Скрываем элементы управления видеоплеером, кроме кнопок переключения камер
        const videoControls = document.querySelector('.video-controls');
        if (videoControls) videoControls.style.display = 'none';
        
        // Добавляем индикатор режима гостя в верхнюю часть страницы
        const mainContainer = document.querySelector('.main-container');
        if (mainContainer) {
            const guestBannerContainer = document.createElement('div');
            guestBannerContainer.className = 'guest-mode-banner';
            
            // Добавляем текст и кнопку выхода
            guestBannerContainer.innerHTML = `
                <span>Режим просмотра (гость)</span>
                <button id="exit-guest-mode" style="margin-left: 15px; padding: 5px 10px; background-color: white; color: #ff9800; border: none; border-radius: 3px; cursor: pointer;">Выйти</button>
            `;
            
            mainContainer.insertBefore(guestBannerContainer, mainContainer.firstChild);
            
            // Добавляем обработчик для кнопки выхода
            document.getElementById('exit-guest-mode').addEventListener('click', () => {
                // Удаляем флаг гостевого режима и перенаправляем на страницу входа
                localStorage.removeItem('isGuestMode');
                location.href = 'login.html';
            });
        }
    }
});

// Список датчиков с описаниями
const sensors = [
    { id: 'temp', name: 'Температура (AMP-B045)', format: '°C', color: 'rgba(255, 99, 132, 1)', bgColor: 'rgba(255, 99, 132, 0.2)' },
    { id: 'humid', name: 'Влажность (AMP-B045)', format: '%', color: 'rgba(54, 162, 235, 1)', bgColor: 'rgba(54, 162, 235, 0.2)' },
    { id: 'co2', name: 'CO2 (AMP-B057)', format: 'ppm', color: 'rgba(255, 206, 86, 1)', bgColor: 'rgba(255, 206, 86, 0.2)' },
    { id: 'lpg', name: 'LPG (AMP-B057)', format: 'ppm', color: 'rgba(75, 192, 192, 1)', bgColor: 'rgba(75, 192, 192, 0.2)' },
    { id: 'ch4', name: 'CH4 (AMP-B057)', format: 'ppm', color: 'rgba(153, 102, 255, 1)', bgColor: 'rgba(153, 102, 255, 0.2)' }
];

// Хранилище для данных графиков
const sensorData = {
    temp: { values: [], labels: [] },
    humid: { values: [], labels: [] },
    co2: { values: [], labels: [] },
    lpg: { values: [], labels: [] },
    ch4: { values: [], labels: [] }
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
        
        // Создаем обертку для информации о датчике
        const infoDiv = document.createElement('div');
        infoDiv.className = 'sensor-info';
        infoDiv.innerHTML = `
            <span class="sensor-name">${sensor.name}</span>
            <span class="spacer"></span>
            <span class="sensor-value" id="${sensor.id}-value">Загрузка...</span>
        `;
        
        li.appendChild(infoDiv);
        
        // Добавляем контейнер для графика внутри элемента списка только для температуры и влажности
        if (sensor.id === 'temp' || sensor.id === 'humid') { 
            const chartContainer = document.createElement('div');
            chartContainer.className = 'sensor-chart-container';
            chartContainer.innerHTML = `
                <div class="chart-container">
                    <canvas id="${sensor.id}-chart"></canvas>
                </div>
            `;
            li.appendChild(chartContainer);
        }
        
        sensorListElement.appendChild(li);
    });
}

// Инициализация графиков
function initCharts() {
    // Отфильтруем только датчики с графиками (температура и влажность)
    const sensorsWithCharts = sensors.filter(sensor => sensor.id === 'temp' || sensor.id === 'humid');
    
    sensorsWithCharts.forEach(sensor => {
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
    // Обновляем только графики температуры и влажности
    if (!charts[sensorId] || (sensorId !== 'temp' && sensorId !== 'humid')) return;
    
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
            'http://192.168.0.139:8080/api/get/data'
            // 'http://192.168.1.244:8080/api/get/data'
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
    const url = "http://192.168.0.139:8080/api/alert";
    // const url = "http://192.168.1.244:8080/api/alert";
    try {
        const response = await fetch(url);
        if (!response.ok) {
            console.error("Ошибка получения уведомлений:", response.statusText);
            setTimeout(subscribeToNotifications, 5000);
            return;
        }

        const data = await response.json();

        // Проверка типа уведомления
        if (data.type) {
            let title, message;
            
            switch (data.type) {
                case "co2":
                    title = "Предупреждение о превышении CO2";
                    message = "Обнаружено превышение нормы концентрации CO2. Проветрите помещение!";
                    break;
                case "lpg":
                    title = "Предупреждение о превышении LPG";
                    message = "Обнаружено превышение нормы LPG (сжиженный нефтяной газ). Проверьте помещение!";
                    break;
                case "ch4":
                    title = "Предупреждение о превышении CH4";
                    message = "Обнаружено превышение нормы CH4 (метан). Проверьте помещение!";
                    break;
                default:
                    title = "Предупреждение о загазованности";
                    message = "Обнаружено превышение нормы загазованности. Проветрите помещение!";
            }
            
            createNotification(title, message);
        }
        
        setTimeout(subscribeToNotifications, 5000);
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
            'http://192.168.0.139:8080/rfid/logs/latest'
            // 'http://192.168.1.244:8080/rfid/logs/latest'
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