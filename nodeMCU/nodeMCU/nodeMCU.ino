#include <PubSubClient.h>
#include <ESP8266WiFi.h>
#include <Wire.h>
#include <Adafruit_AMG88xx.h>
#include "DHT.h"
#include "MQ7.h"

const char* ssid     = "AdamPiNetwork";
const char* password = "RaspberryPiJelszo";

const char* mqttServer = "192.168.4.1";
const int   port       = 1883;

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);


void MQTTcallback(char* topic, byte* payload, unsigned int length);
void subscription();
void connectESP();

const int MQ7_PIN            = A0;
const int DHT_PIN            = D4;
const int KITCHENFAN_PIN     = D3;
const int BATHROOMLED_PIN    = D0;
const int DISTANCESENSOR_PIN = D5;
const int REDLED_PIN         = D6;
const int BLUELED_PIN        = D7;

const int Voltage            = 5;

int bathroomBrightness   = 0;
int distance             = 0;
int COVal                = 0;
int desiredTemperature   = 0;
float ppm                = 0;
float temperature        = 22;
float humidity           = 0;
const float coefficientA = 19.32;
const float coefficientB = -0.64;
const float v_in         = 5.0;


unsigned long MQ7TimeNow = 0;
int HighVoltagePeriod    = 6000;
int LowVoltagePeriod     = 9000;

unsigned long DHTTimeNow = 0;
int DHTMeasurePeriod     = 1000;

unsigned long AMGTimeNow = 0;
int AMGMeasurePeriod     = 1000;

MQ7 gasSensor(MQ7_PIN, Voltage);

enum MQ7States {
  HighVoltage,
  LowVoltage,
  Measure
};

MQ7States MQ7State = HighVoltage;

Adafruit_AMG88xx amg;
float pixels[AMG88xx_PIXEL_ARRAY_SIZE];


DHT DHTSensor(DHT_PIN, DHT11);


void ledHandler();
void connectPresenceSensor();
void presenceSensor();
void measureDistance();
void MQ7Measure();
void calculatePPM();
void measureTemperatureAndHumidity();
void termostat();
void fanControl();


void setup() {
  Serial.begin(115200);
  DHTSensor.begin();
  connectPresenceSensor();

  gasSensor.calibrate();

  pinMode(MQ7_PIN,              INPUT);
  pinMode(DHT_PIN,              OUTPUT);
  pinMode(KITCHENFAN_PIN,       OUTPUT);
  pinMode(BATHROOMLED_PIN,      OUTPUT);
  pinMode(DISTANCESENSOR_PIN,   OUTPUT);
  pinMode(REDLED_PIN,           OUTPUT);
  pinMode(BLUELED_PIN,          OUTPUT);

  digitalWrite(KITCHENFAN_PIN,     LOW);
  digitalWrite(BATHROOMLED_PIN,    LOW);
  digitalWrite(DISTANCESENSOR_PIN, LOW);
  digitalWrite(REDLED_PIN,         HIGH);
  digitalWrite(BLUELED_PIN,        HIGH);

  connectESP();
  subscription();

  MQ7TimeNow = millis();
}


void loop() {
  mqttClient.loop();
  
  presenceSensor();  
  measureDistance();  
  //calculatePPM();
  //measureTemperatureAndHumidity();  
  termostat();
  fanControl();  
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

    if (mqttClient.connect("nodeMCU1")) {
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
  mqttClient.subscribe("nodeMCU1/bathroom/brightness");
  mqttClient.subscribe("nodeMCU1/kitchen/termostat");
}


void MQTTcallback(char* topic, byte* payload, unsigned int length) {
  Serial.println(topic);


  if (strcmp(topic, "nodeMCU1/bathroom/brightness") == 0) {
    payload[length] = '\n';
    bathroomBrightness = atoi((char*)payload);
  }


  if (strcmp(topic, "nodeMCU1/kitchen/termostat") == 0) {
    payload[length] = '\n';
    desiredTemperature = atoi((char*)payload);

    Serial.println(desiredTemperature);
  }
}


void ledHandler() {
  analogWrite(BATHROOMLED_PIN, bathroomBrightness);
}


void connectPresenceSensor() {
  bool status = amg.begin();
  if (!status) {
    Serial.println("Could not find a valid AMG88xx sensor, check wiring!");
    while (1);
  }
}


void presenceSensor() {
  if (millis() > AMGTimeNow + AMGMeasurePeriod) {
    AMGTimeNow = millis();

    amg.readPixels(pixels);
    float tempAVGs[8] = {0, 0, 0, 0, 0, 0, 0, 0};    

    int index = 0;
    
    for (int i = 1; i <= AMG88xx_PIXEL_ARRAY_SIZE; i++) {
      
      tempAVGs[index] += pixels[i - 1];
      index++;

      if (i % 8 == 0) { index = 0; } 
    }
    
    for (int i = 0; i < 8; i++) {
      tempAVGs[i] /= 8.0;
    }

    int counter = 0;
    
    for (int i = 0; i < 8; i++) {
      if (tempAVGs[i] >= 26.0) {
        counter++;
      }
    }

    if (counter > 2) {
      digitalWrite(BATHROOMLED_PIN, HIGH);
    } else {
      digitalWrite(BATHROOMLED_PIN, LOW);
    }
  }
}


void measureDistance() {
  digitalWrite(DISTANCESENSOR_PIN, HIGH);
  delayMicroseconds(2);
  digitalWrite(DISTANCESENSOR_PIN, LOW);

  pinMode(DISTANCESENSOR_PIN, INPUT);
  long duration = pulseIn(DISTANCESENSOR_PIN, HIGH);

  distance = duration * 0.034 / 2;

  Serial.println("dst: " + String(distance));

  if (distance <= 40 && distance >= 5) {
    char arrDis[50];
    String strDis = String(distance);
    strDis.toCharArray(arrDis, strDis.length() + 1);
    mqttClient.publish("Raspberry/garden/distance", arrDis);
  }

  delay(200);
  pinMode(DISTANCESENSOR_PIN, OUTPUT);
  digitalWrite(DISTANCESENSOR_PIN, LOW);
}

/*
void MQ7Measure() {
  switch (MQ7State) {
    case HighVoltage :
      if ( MQ7TimeNow + HighVoltagePeriod > millis() ) {
        analogWrite(MQ7PWM_PIN, 255);
      }
      else {
        MQ7TimeNow = millis();
        MQ7State = LowVoltage;
      }
      break;

    case LowVoltage :
      if ( MQ7TimeNow + LowVoltagePeriod > millis() ) {
        analogWrite(MQ7PWM_PIN, 73);
      }
      else {
        MQ7State = Measure;
      }
      break;

    case Measure :
      digitalWrite(MQ7PWM_PIN, HIGH);
      delay(50);

 

      COVal = analogRead(MQ7_PIN);

      Serial.print("raw: ");
      Serial.println(COVal);

      calculatePPM();

      Serial.print("ppm: ");
      Serial.println(ppm);

      char arrPPM[50];
      String strPPM = String(ppm);
      strPPM.toCharArray(arrPPM, strPPM.length() + 1);

      mqttClient.publish("Raspberry/livingroom/COppm", arrPPM);

      if (ppm > 15.0) {
        Serial.println("Magas CO szint");
        mqttClient.publish("Raspberry/livingroom/HIGHCO", "1");
      }

      MQ7TimeNow = millis();
      MQ7State = HighVoltage;
      break;
  }
}
*/

void calculatePPM() {
  
  ppm = (int)gasSensor.readPpm();

  Serial.print("ppm: ");
  Serial.println(ppm);

  char arrPPM[50];
  String strPPM = String(ppm);
  strPPM.toCharArray(arrPPM, strPPM.length() + 1);

  mqttClient.publish("Raspberry/livingroom/COppm", arrPPM);
 
  if (ppm > 5000.0) {
    Serial.println("Magas CO szint");
    mqttClient.publish("Raspberry/livingroom/HIGHCO", "1");
  }
      
  delay(1000);
}


void measureTemperatureAndHumidity() {
  if (millis() > DHTTimeNow + DHTMeasurePeriod) {
    DHTTimeNow = millis();

    humidity    = DHTSensor.readHumidity();
    temperature = DHTSensor.readTemperature();

    Serial.println("hum: " + String(humidity) + "\ntemp: " + String(temperature) );

    String strHum = String(humidity), strTemp = String(temperature);
    char arrHum[50];
    char arrTemp[50];

    strHum.toCharArray(arrHum, strHum.length() + 1);
    strTemp.toCharArray(arrTemp, strTemp.length() + 1);

    mqttClient.publish("Raspberry/bathroom/humidity",    arrHum);
    mqttClient.publish("Raspberry/bathroom/temperature", arrTemp);
  }
}


void termostat() {
  if (desiredTemperature == 0 || temperature == desiredTemperature) {
    digitalWrite(REDLED_PIN,  HIGH);
    digitalWrite(BLUELED_PIN, HIGH);
  } else if (temperature < desiredTemperature) {
    digitalWrite(REDLED_PIN,  LOW);
    digitalWrite(BLUELED_PIN, HIGH);
  } else {
    digitalWrite(REDLED_PIN,  HIGH);
    digitalWrite(BLUELED_PIN, LOW);
  }
}


void fanControl() {
  int fanpwm = 0;
  if (ppm > 5000.0) {
    fanpwm = map((int)ppm, 5000, 40000, 90, 200);
  } 
  if (humidity > 65) {
    fanpwm = map(humidity, 65, 80, 90, 200);
  }
  
  analogWrite(KITCHENFAN_PIN, fanpwm);
}
