#include <PubSubClient.h>
#include <ESP8266WiFi.h>
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


const int SOUNDDETECTOR_PIN   = A0;
const int CHILDRENLED_PIN     = D1;
const int CHILDRENSHUTTER_PIN = D6;
const int BATHROOMSHUTTER_PIN = D7;


int childrenBrightness      = 0;
int childrenShutterAngle    = 180;
int actChildrenShutterAngle = 0;
int bathroomShutterAngle    = 180;
int actBathroomShutterAngle = 0;
bool childrenLedState       = false;
const int LedNum            = 30;
const int soundThresholdMax = 1020;
const int soundThresholdMin = 100;
int counter = 0, sum = 0, avg = 0;

Servo childrenShutter;
Servo bathroomShutter;


unsigned long soundMeasureTimeNow = 0;
int soundMeasurePeriod = 10;

unsigned long soundMeasureTimer = 0;
int soundTimerInteraval = 1000;


void checkClap();
void ledHandler();
void shutterHandler();


void setup() {
  Serial.begin(115200);  

  pinMode(SOUNDDETECTOR_PIN,    INPUT);
  pinMode(CHILDRENLED_PIN,      OUTPUT);
  pinMode(CHILDRENSHUTTER_PIN,  OUTPUT);
  pinMode(BATHROOMSHUTTER_PIN,  OUTPUT);

  digitalWrite(CHILDRENLED_PIN, LOW);

  connectESP();
  subscription();

  childrenShutter.attach(CHILDRENSHUTTER_PIN);
  bathroomShutter.attach(BATHROOMSHUTTER_PIN);
}

void loop() {
  mqttClient.loop();


  ledHandler();
  shutterHandler();
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

    if (mqttClient.connect("nodeMCU3")) {
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
  mqttClient.subscribe("nodeMCU3/children/brightness");
  mqttClient.subscribe("nodeMCU3/children/shutter");
  mqttClient.subscribe("nodeMCU3/bathroom/shutter");
  Serial.println("feliratkozva");
}


void MQTTcallback(char* topic, byte* payload, unsigned int length) {

  Serial.println(topic);

  if (strcmp(topic, "nodeMCU3/children/brightness") == 0) {
    payload[length] = '\n';
    childrenBrightness = atoi((char*)payload);
  }


  if (strcmp(topic, "nodeMCU3/children/shutter") == 0) {
    payload[length] = '\n';
    actChildrenShutterAngle = childrenShutterAngle;
    childrenShutterAngle = atoi((char*)payload);
    Serial.println(childrenShutterAngle);
    childrenShutter.attach(CHILDRENSHUTTER_PIN);
  }


  if (strcmp(topic, "nodeMCU3/bathroom/shutter") == 0) {
    payload[length] = '\n';
    actBathroomShutterAngle = bathroomShutterAngle;
    bathroomShutterAngle = atoi((char*)payload);
    Serial.println(bathroomShutterAngle);
    bathroomShutter.attach(BATHROOMSHUTTER_PIN);
  }
}


void checkClap() {
  int soundVolume = analogRead(SOUNDDETECTOR_PIN);

  //Serial.println(soundVolume);

  if (soundVolume >= soundThresholdMax && counter == 0) {
    sum += soundVolume;
    counter++;
    soundMeasureTimer = millis();
  }


  if ( millis() > soundMeasureTimeNow + soundMeasurePeriod ) {
    soundMeasureTimeNow = millis();

    if ( soundMeasureTimer + soundTimerInteraval > millis() && counter != 0)  {
      // 1 mp-ig méri a hangokat
      if (soundVolume > soundThresholdMin) {
        sum += soundVolume;
        counter++;
      }
    }
    else if (counter != 0) {
      avg = sum / counter;
      // Serial.println("sum: " + String(sum) + " | avg: " + String(avg) + " - count: " + String(counter));
      if (avg > 680 && counter < 15) { 
        childrenLedState = !childrenLedState;
      }
      sum = avg = counter = 0;
    }
  }

  if (childrenLedState) digitalWrite(CHILDRENLED_PIN, HIGH);
  else digitalWrite(CHILDRENLED_PIN, LOW);
}


void ledHandler() {
  analogWrite(CHILDRENLED_PIN, childrenBrightness);
  childrenLedState = childrenBrightness > 0 ? true : false;
 // checkClap();
}


void shutterHandler() {
  if (actChildrenShutterAngle != childrenShutterAngle) {
    childrenShutter.write(actChildrenShutterAngle);
    delay(15);

    if (actChildrenShutterAngle < childrenShutterAngle) actChildrenShutterAngle++;
    else actChildrenShutterAngle--;
  }
  else childrenShutter.detach();

  if (actBathroomShutterAngle != bathroomShutterAngle) {
    bathroomShutter.write(actBathroomShutterAngle);
    delay(15);

    if (actBathroomShutterAngle < bathroomShutterAngle) actBathroomShutterAngle++;
    else actBathroomShutterAngle--;
  }
  else bathroomShutter.detach();
}
