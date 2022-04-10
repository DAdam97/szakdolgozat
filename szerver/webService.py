from flask import Flask, request
from flask_socketio import SocketIO, send, emit
from mqttClient import *
from HelperFunctions import *
from jsonClient import *
from pygame import mixer
import time


app = Flask(__name__)
socketio = SocketIO(app)

client = Client()


def message_callback(client, userdata, message):
    topic = message.topic
    
    print(topic)
    
    if (topic == "Raspberry/garden/PhotoRes"):
        photoResState = bool(int(message.payload.decode("utf-8")))
        updateJson("garden", "photoRes", photoResState)
        
        bathroomShutterState = loadJsonData("bathroom", "shutterAuto")
        childrenShutterState = loadJsonData("children", "shutterAuto")
                
        if photoResState:
            if bathroomShutterState:
                client.publish("nodeMCU3/bathroom/shutter", percentToAngle(0))
                print("fürdő fel")
            if childrenShutterState:
                client.publish("nodeMCU3/children/shutter", percentToAngle(0))
                print("gyerek fel")
        else: 
            if bathroomShutterState:
                client.publish("nodeMCU3/bathroom/shutter", percentToAngle(100))
                print("fürdő le")
            if childrenShutterState:
                client.publish("nodeMCU3/children/shutter", percentToAngle(100))
                print("gyerek le")              
        
        
    if (topic == "Raspberry/garden/distance"):
        distance = int(message.payload.decode("utf-8"))
        
        volume = distanceToVolume(distance, 5, 40, 0, 1)
        
        mixer.init()
        mixer.music.set_volume(volume)
        
        if 40 >= distance > 30:
            mixer.music.load('./sounds/beep.mp3')            
        elif 30 >= distance > 20:
            mixer.music.load('./sounds/beep2.mp3')
        elif 20 >= distance > 10:
            mixer.music.load('./sounds/beep3.mp3')
        elif 10 >= distance:
            mixer.music.load('./sounds/beep4.mp3')
            
        mixer.music.play() 
               
       
    if (topic == "Raspberry/livingroom/breachDetected"):
        breachDetected = str(message.payload.decode("utf-8")) 
        
        updateJson("livingroom", "breachDetected", breachDetected)
        
        with app.app_context():
            emit("breachDetected", "BEHATOLÓ!!!", namespace="/msg", broadcast=True)
        
        mixer.init()
        mixer.music.load('./sounds/alarm.mp3') 
        mixer.music.play()        
        
    
    if (topic == "Raspberry/livingroom/HIGHCO"):
        highCO = bool(int(message.payload.decode("utf-8")))
        updateJson("livingroom", "highCO", highCO)
        
        with app.app_context():
            emit("gasDetected", "MAGAS CO SZINT!!!", namespace="/msg", broadcast=True)
        
        mixer.init()
        mixer.music.load('./sounds/alarm.mp3') 
        mixer.music.play()
    
    if (topic == "Raspberry/livingroom/securityEnable"):
        securityEnable = str(message.payload.decode("utf-8")) 
        updateJson("livingroom", "securityEnable", securityEnable)
                
        
    if (topic == "Raspberry/bathroom/temperature"):
        temperature = float(message.payload.decode("utf-8"))
        updateJson("bathroom", "temperature", temperature)
        
        print("temp:"+str(temperature))
        
        with app.app_context():
            emit("temperatureChanged", temperature, namespace="/msg", broadcast=True)
        
        
    if (topic == "Raspberry/bathroom/humidity"):
        humidity = float(message.payload.decode("utf-8"))
        updateJson("bathroom", "humidity", humidity)

        print("hum:" + str(humidity))

        with app.app_context():
            emit("humidityChanged", humidity, namespace="/msg", broadcast=True)
           
           
    if (topic == "Raspberry/livingroom/COppm"):
        COppm = float(message.payload.decode("utf-8")) 
        updateJson("livingroom", "COppm", COppm)

        with app.app_context():
            emit("coPPMChanged", COppm, namespace="/msg", broadcast=True)
              

@app.route('/light', methods = ['POST'])
def light():
    brightness = int(request.form["brightness"])
    room = request.form["room"]  

    print(brightness)

    if(room == 'livingroom'):
        client.pub("nodeMCU2/livingroom/brightness", percentToPWMVal(brightness))        
    elif(room == 'kitchen'):
        client.pub("nodeMCU2/kitchen/brightness", percentToPWMVal(brightness))
    elif(room == 'bathroom'):
        client.pub("nodeMCU1/bathroom/brightness", percentToPWMVal(brightness))
    elif(room == 'children'):
        client.pub("nodeMCU3/children/brightness", percentToPWMVal(brightness))
    elif(room == 'garden'):
        client.pub("nodeMCU2/garden/brightness", percentToPWMVal(brightness))
    
    updateJson(room, "brightness", brightness)
    
    return str(brightness)


## Telefonos teszt ##
@app.route('/light', methods = ['GET'])
def lightTest():
    brightnessVal = int(request.args['brightness'])
    room = request.args['room']
    print(brightnessVal) 
    

    if(room == 'livingroom'):
        client.pub("nodeMCU2/livingroom/brightness", percentToPWMVal(brightnessVal))        
    elif(room == 'kitchen'):
        client.pub("nodeMCU2/kitchen/brightness", percentToPWMVal(brightnessVal))
    elif(room == 'bathroom'):
        client.pub("nodeMCU1/bathroom/brightness", percentToPWMVal(brightnessVal))
    elif(room == 'children'):
        client.pub("nodeMCU3/children/brightness", percentToPWMVal(brightnessVal))
    elif(room == 'garden'):
        client.pub("nodeMCU2/garden/brightness", percentToPWMVal(brightnessVal))
    
    updateJson(room, "brightness", brightnessVal)
    
    return 'room: ' + room + ' - ' + str(brightnessVal)



@app.route('/door', methods = ['POST'])
def door():
    doorAngle = int(request.form['angle'])
    room = request.form['room']
    
    print(doorAngle)
    print(room)
        
    if(room == 'livingroom'):
        client.pub("nodeMCU2/livingroom/door", percentToAngle(doorAngle))        
    elif(room == 'kitchen'):
        client.pub("nodeMCU2/kitchen/door", percentToAngle(doorAngle))
    
    updateJson(room, "doorAngle", doorAngle)
    
    return str(doorAngle)



@app.route('/PIREnable', methods = ['POST'])
def PIREnable():
    state = bool(int(request.form['enable']))
    client.pub("nodeMCU2/garden/PIREnable", state)
    
    updateJson("garden", "PIREnable", state)
    
    return str(state)


@app.route('/PIRDuration', methods = ['POST'])
def PIRDuration():    
    duration = int(request.form['durationVal'])
    duration *= 1000
    client.pub("nodeMCU2/garden/PIRDuration", duration)
    
    updateJson("garden", "duration", duration)
    
    return str(duration)


@app.route('/shutter', methods = ['POST'])
def shutter():
    shutterVal = int(request.form["shutterVal"])
    room = request.form["room"]
    
    print(shutterVal)
    print(room)

    if(room == 'bathroom'):
        client.pub("nodeMCU3/bathroom/shutter", percentToAngle(shutterVal))        
    elif(room == 'children'):
        client.pub("nodeMCU3/children/shutter", percentToAngle(shutterVal))    
    
    return str(shutterVal)


@app.route('/shutterAuto', methods = ['POST'])
def shutterAuto():
    state = bool(int(request.form['auto']))          
    room = request.form['room']  
    
    print(room +" - "+ str(state))
    
    updateJson(room, "shutterAuto", state)   
    
    return str(state)


@app.route('/termostat', methods = ['POST'])
def termostat():
    termostatState = bool(int(request.form['termostat']))
    
    updateJson("kitchen", "termostat", termostatState)
    
    print("termostat: " + str(loadJsonData("kitchen", "termostat")))
    
    if termostatState:
        client.pub("nodeMCU1/kitchen/termostat", loadJsonData("kitchen", "desiredTemp"))
    else:
        client.pub("nodeMCU1/kitchen/termostat", 0)
      
    return str(termostatState)


@app.route('/desiredTemperature', methods = ['POST'])
def desiredTemperature():
    strTemp = str(request.form['desiredTemperature'])    
    print("desired temp: " + strTemp)
    
    if strTemp != '':
        desTemp = int(strTemp)
        updateJson("kitchen", "desiredTemp", desTemp)
        client.pub("nodeMCU1/kitchen/termostat", desTemp)
    
    return str(strTemp)


@app.route('/securityEnable', methods = ['POST'])
def securityEnable():
    state = bool(int(request.form['enable']))
    
    print(state)
    
    client.pub("nodeMCU2/livingroom/securityEnable", state)
    updateJson("livingroom", "securityEnable", state)

    return str(tate)


@app.route('/warningOff', methods = ['POST'])
def warningOff():
    reason = str(request.form["warn"])
    
    if reason == "gas":
        updateJson("livingroom", "highCO", False)
    elif reason == "intruder":
        client.pub("nodeMCU2/livingroom/breachSolved", "1")
        updateJson("livingroom", "breachDetected", False)
        updateJson("livingroom", "securityEnabled", False)

    mixer.music.stop()

    return  str(reason)
    
if __name__=='__main__':
    
    client.connect()
    client.setOn_message(message_callback)
    client.createClientSub()  
    
    initJsonFiles()
    
    # ez után nem szabad irni semmit  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    app.run(debug=True, use_reloader=False,
        host='192.168.1.106', port=8080)    # Web service elinditasa
    