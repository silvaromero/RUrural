import json
import os
import urlparse
import paho.mqtt.client as mqtt

def on_connect(
    self,
    mosq,
    obj,
    rc,
    ):
    print 'rc: ' + str(rc)

'''def on_message(mosq, obj, msg):
    print msg.topic + ' ' + str(msg.qos) + ' ' + str(msg.payload)

    cons = consulta(str(msg.payload))

    # print "cons J2 = %s" % cons

    if cons['userName'] != '':

        # retorno = registro(cons)

        retorno = '%s' % cons
    else:
        retorno = 'Usuario nao cadastrado.'
    mqttc.publish('retorno', retorno)
    print retorno'''

def on_publish(mosq, obj, mid):
    print 'Publish: J1' + str(mid)

'''def on_subscribe(
    mosq,
    obj,
    mid,
    granted_qos,
    ):
    print 'Subscribed: ' + str(mid) + ' ' + str(granted_qos)'''


def on_log(
    mosq,
    obj,
    level,
    string,
    ):
    print string

mqttc = mqtt.Client()

#mqttc.on_message = on_message
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
# mqttc.on_subscribe = on_subscribe


url_str = os.environ.get('m10.cloudmqtt.com',
                         'mqtt://m10.cloudmqtt.com:13988')

url = urlparse.urlparse(url_str)

mqttc.username_pw_set('adm', '54321')
mqttc.connect(url.hostname, url.port)

usuario = {}
usuario['nome'] = 'Romero'
usuario['id'] = '0'

mqttc.publish('retorno', json.dumps(usuario))

'''rc = 0
while rc == 0:
    rc = mqttc.loop()
print 'rc: ' + str(rc)'''