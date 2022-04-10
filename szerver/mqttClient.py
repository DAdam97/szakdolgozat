import paho.mqtt.client as mqtt
import paho.mqtt.subscribe as subscribe

class Client:
    def __init__(self):
        self.hostname = "192.168.1.106"
        self.client = mqtt.Client()
        

    def connect(self):
        self.client.connect(self.hostname)        # MQTT kapcsolat letrehozasa
        self.client.loop_start()                  # MQTT callback-ek figyelese
    
    
    def pub(self, topic, message):
        print(topic + " : " + str(message))
        self.client.publish(topic, message)
        
    def sub(self, topics):
        self.client.subscribe(topics)
        
    def setOn_message(self, on_message):
        self.client.on_message = on_message
    
    def createClientSub(self):
        self.sub([
            ("Raspberry/garden/PhotoRes",0),
            ("Raspberry/garden/distance",0), 
            ("Raspberry/livingroom/breachDetected",0),
            ("Raspberry/livingroom/securityEnable",0),            
            ("Raspberry/livingroom/COppm",0),            
            ("Raspberry/livingroom/HIGHCO",0),
            ("Raspberry/bathroom/temperature",0),
            ("Raspberry/bathroom/humidity",0)
        ])