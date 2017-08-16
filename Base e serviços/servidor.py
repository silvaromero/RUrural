import json
import sys
import os, urlparse
import paho.mqtt.client as mqtt
import pymysql
from datetime import datetime

ipMV = sys.argv[1]

conn = pymysql.connect(
    db='dbru',
    user='admin',
    passwd='admin123',
    host=ipMV)

cursorBD = conn.cursor()

queries = {}
queries['nodeSaldo'] = "SELECT saldo FROM Usuario WHERE rfid = '%s'";
queries['SALDO'] = "SELECT saldo FROM Usuario WHERE cpf = '%s'"
queries['CADASTRO'] = "INSERT INTO Usuario (rfid, nome, cpf, saldo) VALUES ('%s', '%s', '%s', '%s')"
queries['RECARGA'] = "UPDATE Usuario SET saldo = '%s' WHERE cpf = '%s'"


#///////////
# id  AI   /
# rfid     /
# nome     /
# cpf      /
# saldo    /
#///////////



# VERIFICA SE O RFID PASSADO EXISTE NO BANCO, SE SIM, RETORNA SALDO JÀ DESCONTADO
def consultaNode(rfid):

    #TO DO     Testar charset JSON
    retornoJson = {}
    saldoDescontado = 0.0
    case = ""

    queryConsultaNode = queries['nodeSaldo'] % (rfid)
    cursorBD.execute(queryConsultaNode)
    retornoQuery = cursorBD.fetchall()

    if(len(retornoQuery) > 0):              #RFID válido
        saldoAtual = float(retornoQuery[0][0])
        if(datetime.now().hour < 16):       #Almoço
            if(saldoAtual >= 2.00):
                saldoDescontado = (saldoAtual - 2.00)
                case = "sucesso_consultaNode"
            else:
                case = "erro_saldoInsuficienteNode"
        else:                               #Jantar
            if(saldoAtual >= 1.50):
                saldoDescontado = (saldoAtual - 1.50)
                case = "sucesso_consultaNode"
            else:
                case = "erro_saldoInsuficienteNode"
    else:                                   #RFID inválido
        case = "erro_usuarioInexistente"



    if(case == "sucesso_consultaNode"):
        retornoJson["STATUS"] = 0
        retornoJson["saldoDescontado"] = saldoDescontado
    elif(case == "erro_usuarioInexistente"):
        retornoJson["STATUS"] = 1
    elif(case == "erro_saldoInsuficienteNode"):
        retornoJson["STATUS"] = 2

    return retornoJson



def consultaAndroid(parametro):
    pass


def on_connect_filaNode(self, mosq, obj, rc):
    print("rc: " + str(rc))

def on_message_filaNode(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))

    mensagemJson = json.loads(msg.payload)
    rfid =  mensagemJson['RFID']

    retornoJson = consultaNode(str(rfid))

    mqttcFilaAndroid.publish("retornoNodeMCU", json.dumps(retornoJson))
    print(retornoJson)

def on_publish_filaNode(mosq, obj, mid):
    print("Publish: " + str(mid))

def on_subscribe_filaNode(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_connect_filaAndroid(self, mosq, obj, rc):
    print("rc: " + str(rc))


def on_message_filaAndroid(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))

    cons = consulta(str(msg.payload))

    if(cons["userName"] != ""):
    retornoNodeMCU = "%s" % cons
    else:
        retornoNodeMCU = "Usuario nao cadastrado."
    mqttcFilaAndroid.publish("retornoAndroid", retornoNodeMCU)
    print(retornoNodeMCU)

def on_publish_filaAndroid(mosq, obj, mid):
    print("Publish: " + str(mid))

def on_subscribe_filaAndroid(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_log(mosq, obj, level, string):
    print(string)


mqttcFilaNode = mqtt.Client()

mqttcFilaAndroid = mqtt.Client()

mqttcFilaNode.on_message = on_message_filaNode
mqttcFilaNode.on_connect = on_connect_filaNode
mqttcFilaNode.on_publish = on_publish_filaNode
mqttcFilaNode.on_subscribe = on_subscribe_filaNode

mqttcFilaAndroid.on_message = on_message_filaAndroid
mqttcFilaAndroid.on_connect = on_connect_filaAndroid
mqttcFilaAndroid.on_publish = on_publish_filaAndroid
mqttcFilaAndroid.on_subscribe = on_subscribe_filaAndroid


url_str = os.environ.get('m10.cloudmqtt.com','mqtt://m10.cloudmqtt.com:16184')
url = urlparse.urlparse(url_str)


mqtFilaNode.username_pw_set("adm", "54321")
mqttcFilaNode.connect(url.hostname, url.port)
mqttcFilaAndroid.username_pw_set("adm", "54321")
mqttcFilaAndroid.connect(url.hostname, url.port)


mqttcFilaNode.subscribe("acessoNodeMCU", 0)
mqttcFilaAndroid.subscribe("acessoAndroid", 0)


rc = 0
while rc == 0:
    rcFilaNode = mqttcFilaNode.loop()
    rcFilaAndroid = mqttcFilaAndroid.loop()       
    rc = mqttcFilaNode.loop() + mqttcFilaAndroid.loop()
print("rcFilaNode:" + str(rcFilaNode) + " | rcFilaAndroid:" + str(rcFilaAndroid) )