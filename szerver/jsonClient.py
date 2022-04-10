import json


def updateJson(hol, mit, mire):
    jsonFile = {}

    with open("running.json", "r") as read_file:
        jsonFile = json.load(read_file)
        
    jsonFile[hol][mit] = mire 
    
    with open("running.json", "w") as write_file:
        json.dump(jsonFile, write_file)
        

def loadJsonData(honnan, mit):
    with open("running.json", "r") as read_file:
        jsonFile = json.load(read_file)
    
    return jsonFile[honnan][mit]


def initJsonFiles():
    with open("Boot.json", "r") as read_file:
        data = json.load(read_file)
        
    with open("running.json", "w") as write_file:
        json.dump(data, write_file)
   

def sendAllToPhone():
    with open("running.json", "r") as read_file:
        jsonFile = json.load(read_file)
        return jsonFile


def sendBootDataToNodeMCUs(data):
    client.pub("", data["kert"]["photoRes"])
    print(data)



