#!/usr/bin/python
# -*- coding: utf-8 -*-
# FAZENDO OS IMPORTS NECESSARIOS PARA A APLICACAO

import os
import urlparse
import paho.mqtt.client as mqtt
import pymysql
import cgitb
from datetime import datetime

# CONEXAO COM O BANCO - DATABASE, USUARIO, SENHA E HOST

conn = pymysql.connect(db='dbru', user='root', passwd='root',
                       host='127.0.0.1')
c = conn.cursor()

cgitb.enable()


# CODIGO DE CONSULTA AO BANCO

# VERIFICA SE O RFID PASSADO EXISTE NO BANCO
# SE SIM, RETORNA UMA LISTA CONTENDO NOME E ID DO USUARIO CADASTRADO
# NAQUELE RFID

def consulta(num):
    retorno = {}
    retorno['userId'] = 0
    retorno['userName'] = ''
    sql = "SELECT id,nome FROM Usuario WHERE rfid = '%s'" % num

    c.execute(sql)

    r = c.fetchall()

    if len(r) > 0:
        retorno['userId'] = int(r[0][0])
        retorno['userName'] = r[0][1] + ''

    return retorno


# VERIFICA SE DADO USUARIO POSSUI REGISTRO ABERTO ASSOCIADO A SEU RFID
# CASO NAO HAJA, O HORARIO E REGISTRADO E TEM SEU SATUS DEFINIDO COMO ABERTO (1).
# CASO HAJA, O HORARIO E REGISTRADO E O STATUS DEFINIDO COMO FECHADO (0)

# SOBREESCREVEMOS O COMPORTAMENTO DE ALGUMAS
# FUNCOES PROPRIAS DO MQTT

# EXECUTADA QUANDO UMA NOVA CONEXAO E FEITA

def on_connect(
    self,
    mosq,
    obj,
    rc,
    ):
    print 'rc: ' + str(rc)


# EXECUTADA QUANDO UMA NOVA MENSAGEM E LIDA NA FILA
# PUBLICA NA FILA DE RESPOSTA SE O ACESSO FOI/NAO FOI LIBERADO
# + O NOME DO CADASTRADO PARA EXIBICAO NO LCD

def on_message(mosq, obj, msg):
    print msg.topic + ' ' + str(msg.qos) + ' ' + str(msg.payload)

    cons = consulta(str(msg.payload))

    # print "cons J2 = %s" % cons

    if cons['userName'] != '':

        # retorno = registro(cons)

        retorno = '%s' % cons
    else:
        retorno = 'Usuario nao cadastrado.'
    mqttc.publish('retorno', retorno)
    print retorno


# EXECUTADO A CADA PUBLICACAO

def on_publish(mosq, obj, mid):
    print 'Publish: J1' + str(mid)


# EXECUTADO A CADA FILA QUE UM SUBSCRIBE E DADO

def on_subscribe(
    mosq,
    obj,
    mid,
    granted_qos,
    ):
    print 'Subscribed: ' + str(mid) + ' ' + str(granted_qos)


# EXECUTADO EM CADA ESCRITA NO LOG

def on_log(
    mosq,
    obj,
    level,
    string,
    ):
    print string


# CRIACAO DO OBJETO DO TIPO mqtt.Client

mqttc = mqtt.Client()

# SOBRESCRITA DOS METODOS NATIVOS DO MQTT

mqttc.on_message = on_message
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
mqttc.on_subscribe = on_subscribe

# URL DO CLOUDMQTT E DA INSTANCIA AONDE AS FILAS ESTAO
# A URL DA INSTANCIA E COMPOSTA POR: mqtt://m12.cloudmqtt.com: + PORTA
# PORTA PODE SER ENCONTRADO NAS INFORMACOES DA INSTANCIA

url_str = os.environ.get('m13.cloudmqtt.com',
                         'mqtt://m13.cloudmqtt.com:13988')
url = urlparse.urlparse(url_str)

# ATRIBUICAO DO USUARIO COM ACESSO AS FILAS
# os parametros do username_pw_set sao os dados usuario e senha do MQTT

mqttc.username_pw_set('adm', '54321')
mqttc.connect(url.hostname, url.port)

# SUBSCRIBE NA FILA ACESSO

mqttc.subscribe('acesso', 0)

# LOOP ENQUANTO UM ERRO NAO FOR ENCONTRADO O NOSSO SERVIDOR ESTARA OUVINDO A FILA
# ACESSO E ESCREVENDO AS RESPOSTAS NA FILA RETORNO

rc = 0
while rc == 0:
    rc = mqttc.loop()
print 'rc: ' + str(rc)