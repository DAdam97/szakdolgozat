#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Servo.h>


const char* ssid     = "AdamPiNetwork";
const char* password = "RaspberryPiJelszo";

const char* mqttServer = "192.168.4.1";
const int   port       = 1883;

WiFiClient   wifiClient;
PubSubClient mqttClient(wifiClient);


void MQTTcallback(char* topic, byte* payload, unsigned int length);
void subscription();
void connectESP();


int checkDayPeriod          = 1000;
unsigned long checkDayTime  = 0;
unsigned long ledOnDuration = 10000;
unsigned long ledOnTime     = 0;


const int PHOTORES_PIN       = A0;
const int GARDENLED_PIN      = D1;
const int LASER_PIN          = D2;
const int LASERDET_PIN       = D3;
const int LIVINGROOMLED_PIN  = D4;
const int PIR_PIN            = D5;
const int LIVINGROOMDOOR_PIN = D6;
const int KITCHENDOOR_PIN    = D7;
const int KITCHENLED_PIN     = D8;


int photoResVal          = 0;
int gardenBrightness     = 0;
int livingroomAngle      = 180;
int actLivingroomAngle   = 0;
int livingroomBrightness = 0;
int kitchenAngle         = 180;
int actKitchenAngle      = 0;
int kitchenBrightness    = 0;
bool isDay               = true;
bool PIREnabled          = true;
bool isMotion            = false;
bool ledEnable           = false;
bool securityEnabled     = false;
bool isBreachDetected    = false;
const int MaxDoorAngle   = 180;
Servo livingroomDoor;
Servo kitchenDoor;


void checkDay();
void checkMotion();
void turnLedOnMotion();
void securityHandler();
void ledHandler();
void doorHandler();


void setup() {
  Serial.begin(115200);

  pinMode(PHOTORES_PIN,       INPUT);
  pinMode(LASERDET_PIN,       INPUT);
  pinMode(PIR_PIN,            INPUT);
  pinMode(GARDENLED_PIN,      OUTPUT);
  pinMode(LASER_PIN,          OUTPUT);
  pinMode(LIVINGROOMDOOR_PIN, OUTPUT);
  pinMode(LIVINGROOMLED_PIN,  OUTPUT);
  pinMode(KITCHENDOOR_PIN,    OUTPUT);
  pinMode(KITCHENLED_PIN,     OUTPUT);

  digitalWrite(GARDENLED_PIN,     LOW);
  digitalWrite(LIVINGROOMLED_PIN, LOW);
  digitalWrite(KITCHENLED_PIN,    LOW);

  connectESP();
  subscription();

  livingroomDoor.attach(LIVINGROOMDOOR_PIN);
  kitchenDoor.attach(KITCHENDOOR_PIN);
}

void loop() {
  mqttClient.loop();

  checkDay();
  checkMotion();

  ledHandler();

  securityHandler();
  doorHandler();
}


void connectESP() {
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Csatlakozás a wifihez.");
  }

  mqttClient.setServer(mqttServer, port);
  mqttClient.setCallback(MQTTcallback);

  while (!mqttClient.connected()) {
    Serial.println("Csatlakozás a MQTT-hez.");

    if (mqttClient.connect("nodeMCU2")) {
      Serial.println("Csatlakozott a MQTT-hez.");

    } else {
      Serial.println("Hiba az mqtt csatlakozás során.");
      Serial.print("Status code: ");
      Serial.println(mqttClient.state());
      delay(500);
    }
  }
}


void subscription() {
  mqttClient.subscribe("nodeMCU2/garden/brightness");
  mqttClient.subscribe("nodeMCU2/garden/PIREnable");
  mqttClient.subscribe("nodeMCU2/garden/PIRDuration");
  mqttClient.subscribe("nodeMCU2/livingroom/securityEnable");
  mqttClient.subscribe("nodeMCU2/livingroom/breachSolved");
  mqttClient.subscribe("nodeMCU2/livingroom/door");
  mqttClient.subscribe("nodeMCU2/livingroom/brightness");
  mqttClient.subscribe("nodeMCU2/kitchen/door");
  mqttClient.subscribe("nodeMCU2/kitchen/brightness");
}


void MQTTcallback(char* topic, byte* payload, unsigned int length) {

  Serial.println(topic);

  if (strcmp(topic, "nodeMCU2/garden/PIREnable") == 0) {
    payload[length] = '\n';
    int value  = atoi((char*)payload);
    PIREnabled = (value == 1) ? true : false;
  }


  if (strcmp(topic, "nodeMCU2/garden/PIRDuration") == 0) {
    payload[length] = '\n';
    ledOnDuration  = atoi((char*)payload);    
  }


  if (strcmp(topic, "nodeMCU2/garden/brightness") == 0) {
    payload[length] = '\n';
    gardenBrightness = atoi((char*)payload);
  }


  if (strcmp(topic, "nodeMCU2/garden/duration") == 0) {
    payload[length] = '\n';
    ledOnDuration = atoi((char*)payload);
  }


  if (strcmp(topic, "nodeMCU2/livingroom/securityEnable") == 0) {
    payload[length] = '\n';
    int value  = atoi((char*)payload);
    securityEnabled = (value == 1) ? true : false;
    mqttClient.publish("Raspberry/livingroom/securityEnable", securityEnabled ? "1" : "0");
  }


  if (strcmp(topic, "nodeMCU2/lingroom/breachSolved") == 0) {
    securityEnabled  = false;
    isBreachDetected = false;
    mqttClient.publish("Raspberry/livingroom/securityEnable", "0");
    mqttClient.publish("Raspberry/livingroom/breachDetected", "0");

    // TODO: betörő elhárítva eljárási protokoll végrehajtása 
  }


  if (strcmp(topic, "nodeMCU2/livingroom/door") == 0) {
    payload[length] = '\n';
    actLivingroomAngle = livingroomAngle;
    livingroomAngle = atoi((char*)payload);
    livingroomDoor.attach(LIVINGROOMDOOR_PIN);

  }

  if (strcmp(topic, "nodeMCU2/livingroom/brightness") == 0) {
    payload[length] = '\n';
    livingroomBrightness = atoi((char*)payload);
  }


  if (strcmp(topic, "nodeMCU2/kitchen/door") == 0) {
    payload[length] = '\n';
    actKitchenAngle = kitchenAngle;
    kitchenAngle = atoi((char*)payload);
    kitchenDoor.attach(KITCHENDOOR_PIN);
  }

  if (strcmp(topic, "nodeMCU2/kitchen/brightness") == 0) {
    payload[length] = '\n';
    kitchenBrightness = atoi((char*)payload);
  }
}


void checkDay() {
  if (millis() > checkDayTime + checkDayPeriod) {
    checkDayTime = millis();
    photoResVal  = analogRead(PHOTORES_PIN);
    bool PrevVal = isDay;

    isDay = (photoResVal < 700) ? true : false;
    
    if (PrevVal != isDay) {
      mqttClient.publish("Raspberry/garden/PhotoRes", isDay ? "1" : "0");
    }
  }
}


void checkMotion() {
  if (PIREnabled && !isDay) {
    int PIRVal = digitalRead(PIR_PIN);

    if (PIRVal == HIGH) {
      isMotion  = true;
      ledOnTime = millis();
    }
  }
}


void turnLedOnMotion() {
  if ( ( ledOnTime + ledOnDuration > millis() ) && isMotion ) {
    digitalWrite(GARDENLED_PIN, HIGH);
  } else {
    isMotion = false;
    digitalWrite(GARDENLED_PIN, LOW);
  }
}

void securityHandler() {

  if (securityEnabled) {
    digitalWrite(LASER_PIN, HIGH);
  } else {
    digitalWrite(LASER_PIN, LOW);
  }

  int val = digitalRead(LASERDET_PIN);

  if (val == HIGH && securityEnabled) {
    isBreachDetected = true;
    mqttClient.publish("Raspberry/livingroom/breachDetected", "1");

    Serial.println("Betörő detektálva!!!!!!");
  }
}

void ledHandler() {
  if (gardenBrightness != 0) {
    analogWrite(GARDENLED_PIN, gardenBrightness);
  } else {
    turnLedOnMotion();
  }

  analogWrite(LIVINGROOMLED_PIN, livingroomBrightness);
  analogWrite(KITCHENLED_PIN,    kitchenBrightness);
}

void doorHandler() {

  if (actLivingroomAngle != livingroomAngle) {
    livingroomDoor.write(actLivingroomAngle);
    delay(15);

    if (actLivingroomAngle < livingroomAngle) actLivingroomAngle++;
    else actLivingroomAngle--;

  } 
  else livingroomDoor.detach();

  if (actKitchenAngle != kitchenAngle) {
    kitchenDoor.write(actKitchenAngle);
    delay(15);

    if (actKitchenAngle < kitchenAngle) actKitchenAngle++;
    else actKitchenAngle--;

  } 
  else kitchenDoor.detach();
}
