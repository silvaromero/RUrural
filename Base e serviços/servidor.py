import json
import sys
import os, urlparse
import paho.mqtt.client as mqtt
import pymysql
from datetime import datetime


conn = pymysql.connect(
    db     = 'rurural',
    user   = 'root',
    passwd = 'root',
    host   = '127.0.0.1')

cursorBD = conn.cursor()

queries              = {}
queries['nodeSaldo'] = "SELECT saldo FROM usuario WHERE rfid = '%s' AND cpf <> ''"
queries['SALDO']     = "SELECT saldo FROM usuario WHERE cpf = '%s'"
queries['CADASTRO']  = "INSERT INTO usuario (rfid, nome, cpf, saldo) VALUES ('%s', '%s', '%s', '%s')"
queries['RECARGA']   = "UPDATE usuario SET saldo = '%s' WHERE cpf = '%s'"


#///////////
# id  AI   /
# rfid     /
# nome     /
# cpf      /
# saldo    /
#///////////

def decrementaSaldo(valor, rfid):
    queryDesconto = "UPDATE usuario SET saldo = %.2f WHERE rfid = '%s'" % (valor, rfid)
    cursorBD.execute(queryDesconto)
    conn.commit()
    print(cursorBD._last_executed)
    return



# VERIFICA SE O RFID PASSADO EXISTE NO BANCO, SE SIM, RETORNA SALDO JA DESCONTADO
def consultaNode(rfid):

    #TO DO     Testar charset JSON.
    #TO DO     Decrementar sal na base.
    #TO DO     Modificar caso para usuario inexistente, deve-ser verificar se os campos nome+cpf estao vazios.
    retornoJson = {}
    saldoDescontado = 0.00
    case = ""

    queryConsultaNode = queries['nodeSaldo'] % (rfid)
    cursorBD.execute(queryConsultaNode)
    retornoQuery = cursorBD.fetchall()

    if(len(retornoQuery) > 0):              #RFID valido
        saldoAtual = float(retornoQuery[0][0])
        if((datetime.now().hour-3) < 16):       #Almoco #Subtracao por tres dada a localizacao do servidor e o impacto do fuso horario
            if(saldoAtual >= 2.00):
                saldoDescontado = (saldoAtual - 2.00)
                decrementaSaldo(saldoDescontado, rfid)
                case = "sucesso_consultaNode"
            else:
                case = "erro_saldoInsuficienteNode"
        else:                               #Jantar
            if(saldoAtual >= 1.50):
                saldoDescontado = (saldoAtual - 1.50)
                decrementaSaldo(saldoDescontado, rfid)
                case = "sucesso_consultaNode"
            else:
                case = "erro_saldoInsuficienteNode"
    else:                                   #RFID invalido
        case = "erro_usuarioInexistente"



    if(case == "sucesso_consultaNode"):
        retornoJson["STATUS"] = 0
        retornoJson["saldoDescontado"] = "%.2f" % saldoDescontado
    elif(case == "erro_usuarioInexistente"):
        retornoJson["STATUS"] = 1
    elif(case == "erro_saldoInsuficienteNode"):
        retornoJson["STATUS"] = 2

    return retornoJson



def consultaAndroid(parametro):
    pass

#######Node
def on_connect_filaNode(self, mosq, obj, rc):
    print("rc: " + str(rc))

def on_message_filaNode(mosq, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))

    mensagemJson = json.loads(str(msg.payload))
    rfid =  mensagemJson['RFID']

    retornoJson = consultaNode(str(rfid))

    mqttcFilaAndroid.publish("retornoNode", json.dumps(retornoJson))
    print(retornoJson)

def on_publish_filaNode(mosq, obj, mid):
    print("Publish: " + str(mid))

def on_subscribe_filaNode(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))
#######Node


#######Android
def on_connect_filaAndroid(self, mosq, obj, rc):
    print("rc: " + str(rc))

def on_message_filaAndroid(mosq, obj, msg):
    pass

def on_publish_filaAndroid(mosq, obj, mid):
    print("Publish: " + str(mid))

def on_subscribe_filaAndroid(mosq, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_log(mosq, obj, level, string):
    print(string)
#######Android

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


url_str = os.environ.get('m13.cloudmqtt.com','mqtt://m13.cloudmqtt.com:13988')
url = urlparse.urlparse(url_str)


mqttcFilaNode.username_pw_set("romero", "123")
mqttcFilaNode.connect(url.hostname, url.port)
mqttcFilaAndroid.username_pw_set("romero", "123")
mqttcFilaAndroid.connect(url.hostname, url.port)


mqttcFilaNode.subscribe("acessoNode", 0)
mqttcFilaAndroid.subscribe("acessoAndroid", 0)


rcFilaNode = 0
rcFilaAndroid = 0


while rcFilaNode == 0 or rcFilaAndroid == 0:
    rcFilaNode = mqttcFilaNode.loop()
    rcFilaAndroid = mqttcFilaAndroid.loop()       
print("rcFilaNode:" + str(rcFilaNode) + " | rcFilaAndroid:" + str(rcFilaAndroid) )
