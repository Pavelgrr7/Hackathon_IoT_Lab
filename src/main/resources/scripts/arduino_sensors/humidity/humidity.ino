#include <DHT.h>

#define DHTPIN 2          // Пин, куда подключен DATA
#define DHTTYPE DHT11     // Или DHT22, если у тебя он

DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(9600);
  dht.begin();
}

void loop() {
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature(); // По Цельсию

  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Ошибка чтения с датчика");
    return;
  }

  Serial.print("Влажность: ");
  Serial.print(humidity);
  Serial.print(" %\tТемпература: ");
  Serial.print(temperature);
  Serial.println(" °C");

  delay(200); // Пауза между замерами
}
