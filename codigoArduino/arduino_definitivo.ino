#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ThingSpeak.h>

// Pines del sensor ultrasónico
const int triggerPin = 4;
const int echoPin = 5;

// Pines de control de la bomba
const int IN1 = 16;
const int IN2 = 14;

// Pines de los LEDs
const int LED_ROJO = 0;    // Nivel bajo
const int LED_AMARILLO = 13; // Nivel medio
const int LED_VERDE = 12;   // Nivel lleno

// Configuración de niveles
const int nivelMaximo = 10;
const int nivelMinimo = 3;

// Configuración WiFi y ThingSpeak
const char* ssid = "iPhone Jorge";         
const char* password = "scaresito123456";  
const char* server = "api.thingspeak.com";
unsigned long myChannelNumber = 2762327;
const char* myWriteAPIKey = "JCDUQ3EI7FSH71ES";

WiFiClient client;
ESP8266WebServer webServer(80);

unsigned long tiempoEscritura = 0;

bool estadoBomba = false;  // Estado de la bomba (encendida/apagada)
bool controlManual = false;  // Indica si la bomba está siendo controlada manualmente

void setup() {
  // Configuración de pines
  pinMode(triggerPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);

  pinMode(LED_ROJO, OUTPUT);
  pinMode(LED_AMARILLO, OUTPUT);
  pinMode(LED_VERDE, OUTPUT);

  // Apagar LEDs al inicio
  digitalWrite(LED_ROJO, LOW);
  digitalWrite(LED_AMARILLO, LOW);
  digitalWrite(LED_VERDE, LOW);

  Serial.begin(9600);

  // Conexión a WiFi
  WiFi.begin(ssid, password);
  Serial.print("Conectando a WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\nConexión establecida!");
  Serial.print("IP del dispositivo: ");
  Serial.println(WiFi.localIP());

  // Inicialización de ThingSpeak
  ThingSpeak.begin(client);

  // Configuración del servidor web
  webServer.on("/encender", []() {
    if (!estadoBomba) {
      encenderBomba();
      estadoBomba = true;
      controlManual = true;
      webServer.send(200, "text/plain", "Bomba encendida");
    } else {
      webServer.send(200, "text/plain", "La bomba ya está encendida");
    }
  });

  webServer.on("/apagar", []() {
    if (estadoBomba) {
      apagarBomba();
      estadoBomba = false;
      controlManual = true;
      webServer.send(200, "text/plain", "Bomba apagada");
    } else {
      webServer.send(200, "text/plain", "La bomba ya está apagada");
    }
  });

  webServer.begin();
  Serial.println("Servidor iniciado");
}

void loop() {
  webServer.handleClient();

  // Medir distancia
  long distancia = medirDistancia();
  Serial.println("Distancia: " + String(distancia) + " cm");

  // Control de LEDs según la distancia
  if (distancia <= nivelMinimo) { // Nivel bajo
    digitalWrite(LED_ROJO, HIGH);
    digitalWrite(LED_AMARILLO, LOW);
    digitalWrite(LED_VERDE, LOW);
  } else if (distancia > nivelMinimo && distancia <= nivelMaximo) { // Nivel intermedio
    digitalWrite(LED_ROJO, LOW);
    digitalWrite(LED_AMARILLO, HIGH);
    digitalWrite(LED_VERDE, LOW);
  } else { // Nivel lleno
    digitalWrite(LED_ROJO, LOW);
    digitalWrite(LED_AMARILLO, LOW);
    digitalWrite(LED_VERDE, HIGH);
  }

  // Control automático de la bomba
  if (!controlManual) {
    if (distancia > nivelMaximo && !estadoBomba) {
      encenderBomba();
      estadoBomba = true;
    } else if (distancia <= nivelMinimo && estadoBomba) {
      apagarBomba();
      estadoBomba = false;
    }
  } else {
    if (distancia < nivelMinimo && estadoBomba) {
      apagarBomba();
      estadoBomba = false;
      controlManual = false;
    }
  }

  // Enviar datos a ThingSpeak cada 15 segundos
  if (millis() - tiempoEscritura > 15000) {
    int response = ThingSpeak.writeField(myChannelNumber, 1, distancia, myWriteAPIKey);
    if (response == 200) {
      Serial.println("Datos enviados a ThingSpeak correctamente.");
    } else {
      Serial.println("Error al enviar datos a ThingSpeak: " + String(response));
    }
    tiempoEscritura = millis();
  }
}

long medirDistancia() {
  digitalWrite(triggerPin, LOW);
  delayMicroseconds(2);
  digitalWrite(triggerPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(triggerPin, LOW);

  long duracion = pulseIn(echoPin, HIGH);
  return duracion * 0.034 / 2;
}

void encenderBomba() {
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  Serial.println("Bomba encendida");
}

void apagarBomba() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  Serial.println("Bomba apagada");
}
