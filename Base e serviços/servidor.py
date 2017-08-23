import json
import sys
import os, urlparse
import paho.mqtt.client as mqtt
import pymysql
from datetime import datetime
 
conn = pymysql.connect(
    db='rurural',
    user='root',
    passwd='root',
    host='127.0.0.1')
 
cursorBD = conn.cursor()
 
queries = {}
queries['nodeSaldo']      = "SELECT u.saldo FROM Usuario u INNER JOIN Rfid r ON u.rfid = r.id WHERE r.tag = '%s' AND u.cpf <> ''"
queries['SALDO']          = "SELECT saldo FROM Usuario WHERE cpf = '%s'"
queries['CADASTRO']       = "INSERT INTO Usuario (rfid, nome, cpf, saldo) VALUES ('%s', '%s', '%s', '%s')"
queries['RECARGA']        = "UPDATE Usuario SET saldo = '%s' WHERE cpf = '%s'"
queries['CPF']            = "SELECT cpf FROM Usuario WHERE cpf = '%s'"
queries['CHECK_RFID']     = "SELECT u.cpf FROM Usuario u INNER JOIN Rfid r ON u.rfid = r.id WHERE r.tag = '%s'"
queries['RFID_VALIDACAO'] = "SELECT * FROM Rfid WHERE tag = '%s'"
queries['DESCONTO']       = "UPDATE Usuario u SET u.saldo = %.2f WHERE u.rfid = '%s'"
queries['RFID_USUARIO']   = "SELECT r.id from Rfid r INNER JOIN Usuario u WHERE r.id = u.rfid AND r.tag = '%s'"
 
 
#///////////
# id  AI   /
# rfid     /
# nome     /
# cpf      /
# saldo    /
#///////////
 
def decrementaSaldo(valor, rfid):
    querySelectRFIDUsuario = queries['RFID_USUARIO'] % (rfid)
    cursorBD.execute(querySelectRFIDUsuario)
    print(cursorBD._last_executed)
    retornoQuerySelectRFIDUsuario = cursorBD.fetchall()
    if(len(retornoQuerySelectRFIDUsuario) > 0):
        idRFID = retornoQuerySelectRFIDUsuario[0][0]
        queryDesconto = queries['DESCONTO'] % (valor, idRFID)
        cursorBD.execute(queryDesconto)
        conn.commit()
        print(cursorBD._last_executed)
    return
 
# VERIFICA SE O RFID PASSADO EXISTE NO BANCO, SE SIM, RETORNA SALDO JA DESCONTADO
def consultaNode(rfid):
 
    retornoJson = {}
    saldoDescontado = 0.0
    case = ""
 
    queryConsultaNode = queries['nodeSaldo'] % (rfid)
    cursorBD.execute(queryConsultaNode)
    print(cursorBD._last_executed)
    retornoQuery = cursorBD.fetchall()
 
    if(len(retornoQuery) > 0):              #RFID valido
        saldoAtual = float(retornoQuery[0][0])
        print(saldoAtual)
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
 
 
 
def consultaAndroidSaldo(cpf):
 
    #TO DO     Testar charset JSON
    retornoJson = {}
    saldoDescontado = 0.0
    case = ""
 
    queryConsultaAndroid = queries['SALDO']  % (cpf)
    cursorBD.execute(queryConsultaAndroid)
    retornoQuery = cursorBD.fetchall()
 
    if(len(retornoQuery) > 0):              #CPF valido
        saldoAtual = float(retornoQuery[0][0])
        print "Saldo === " ,saldoAtual
        case = "sucesso_consultaAndroid"
    else:                                   #CPF invalido
        case = "erro_usuarioInexistente"
 
    if(case == "sucesso_consultaAndroid"):
        retornoJson["STATUS"] = 5
        retornoJson["SALDO"] = "%.2f" % saldoAtual
    elif(case == "erro_usuarioInexistente"):
        retornoJson["STATUS"] = 1
 
    return retornoJson
 
 
def recargaAndroid(cpf,valor):
 
    retornoJson = {}
    saldoDescontado = 0.0
    case = ""
 
    queryConsultaAndroid = queries['SALDO']  % (cpf)
    cursorBD.execute(queryConsultaAndroid)
    retornoQuery = cursorBD.fetchall()
 
    if(len(retornoQuery) > 0):              #CPF valido
        saldoAtual = float(retornoQuery[0][0])
        novoSaldo = saldoAtual + float(valor)
 
        queryConsultaAndroidRecarga = queries['RECARGA']  % (novoSaldo,cpf)
        cursorBD.execute(queryConsultaAndroidRecarga)
        conn.commit()
 
        print "novoSaldo === " ,novoSaldo
        case = "sucesso_recargaAndroid"
    else:                                   #CPF invalido
        case = "erro_usuarioInexistente"
 
    if(case == "sucesso_recargaAndroid"):
        retornoJson["STATUS"] = 6
    elif(case == "erro_usuarioInexistente"):
        retornoJson["STATUS"] = 1
 
    return retornoJson
 
def cadastroAndroid(rfid,nome,cpf):
    retornoJson = {}
    saldo = 0.0
    case = ""
 
    queryConsultaAndroidCPF = queries['CPF']  % (cpf)
    cursorBD.execute(queryConsultaAndroidCPF)
    retornoQueryCPF = cursorBD.fetchall()
   
    queryConsultaAndroidRFID = queries['CHECK_RFID']  % (rfid)
    cursorBD.execute(queryConsultaAndroidRFID)
    retornoQueryRFID = cursorBD.fetchall()
   
    if(len(retornoQueryCPF) > 0):              #CPF ja cadastrado
        retornoJson["STATUS"] = 7
        case = "erro_usuarioJaCadastrado"
    elif (len(retornoQueryRFID) > 0):
        retornoJson["STATUS"] = 8
        case = "erro_RfidJaEmUso"
    else:
        queryConsultaAndroid = queries['RFID_VALIDACAO']  % (rfid)
        cursorBD.execute(queryConsultaAndroid)
        retornoQuery = cursorBD.fetchall()
 
        if(len(retornoQuery) > 0):              #RFID valido
            queryConsultaAndroid = queries['CADASTRO']  % (retornoQuery[0][0],nome,cpf,saldo)
            cursorBD.execute(queryConsultaAndroid)
            conn.commit()
   
            case = "sucesso_cadastroAndroid"
        else:                                   #RFID invalido
            case = "erro_rfidInvalido"
   
        if(case == "sucesso_cadastroAndroid"):
            retornoJson["STATUS"] = 3
        elif(case == "erro_rfidInvalido"):
            retornoJson["STATUS"] = 4
 
    return retornoJson
 
#######Node
def on_connect_filaNode(self, mosq, obj, rc):
    print("rc_FilaNode: " + str(rc))
 
def on_message_filaNode(mosq, obj, msg):
    print("MSG_FilaNode: " + msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
 
    mensagemJson = json.loads(msg.payload)
    rfid =  mensagemJson['RFID']
    print "rfid = ==== ",rfid  
    retornoJson = consultaNode(str(rfid))
 
    mqttcFilaAndroid.publish("retornoNode", json.dumps(retornoJson))
    print(retornoJson)
 
def on_publish_filaNode(mosq, obj, mid):
    print("Publish_FilaNode: " + str(mid))
 
def on_subscribe_filaNode(mosq, obj, mid, granted_qos):
    print("Subscribed_FilaNode: " + str(mid) + " " + str(granted_qos))
#######Node
 
#######Android
def on_connect_filaAndroid(self, mosq, obj, rc):
    print("rc_FilaAndroid: " + str(rc))
 
 
def on_message_filaAndroid(mosq, obj, msg):
    print("MSG_FilaAndroid: " + msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
 
    mensagemJson = json.loads(msg.payload)
    op =  mensagemJson['OP']
    cpf =  mensagemJson['CPF']
 
    if (op == 'SALDO'):
        retornoJson = consultaAndroidSaldo(str(cpf))
    elif (op == 'RECARGA'):
        valor = mensagemJson['VALOR']
        retornoJson = recargaAndroid(str(cpf),str(valor))
    elif (op == 'CADASTRO'):
        nome = mensagemJson['NOME']
        rfid = mensagemJson['RFID']
        retornoJson = cadastroAndroid(str(rfid),str(nome),str(cpf))
 
    mqttcFilaAndroid.publish("retornoAndroid",  json.dumps(retornoJson))
    print("retorno da operacao = ",retornoJson)
 
def on_publish_filaAndroid(mosq, obj, mid):
    print("Publish_FilaAndroid: " + str(mid))
 
def on_subscribe_filaAndroid(mosq, obj, mid, granted_qos):
    print("Subscribed_FilaAndroid: " + str(mid) + " " + str(granted_qos))
#######Android
 
#######Node / Android
def on_log(mosq, obj, level, string):
    print("On_Log: " + string)
#######Node / Android
 
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
