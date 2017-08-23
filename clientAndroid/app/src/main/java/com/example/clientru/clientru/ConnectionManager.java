package com.example.clientru.clientru;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionManager {

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private Context mContext;
    private ScreenConnectedI visibleActivity;


    private static final int ERRO_USUARIO_INEXISTENTE = 1;
    private static final int SUCESSO_CADASTRO = 3;
    private static final int ERRO_RFID_INVALIDO = 4;
    private static final int SUCESSO_SALDO = 5;
    private static final int SUCESSO_RECARGA = 6;
    private static final int ERRO_USUARIO_JA_CADASTRADO = 7;
    private static final int ERRO_RFID_VINCULADO = 8;

    public ConnectionManager(Context mcontext){
        this.mContext = mcontext;

        String clientId = MqttClient.generateClientId();
        this.client = new MqttAndroidClient(mContext, "tcp://m13.cloudmqtt.com:13988",
                clientId);

        this.options = new MqttConnectOptions();
        this.options.setUserName("romero");
        this.options.setPassword("123".toCharArray());
    }

    public void connectMQTT(){
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    visibleActivity.showMessage("Conexão não estabelecida! Procure o suporte.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {    }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                visibleActivity.showMessage(decodificaJson(new String(message.getPayload())));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {    }
        });
    }

    public void disconnectMQTT(){
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {}

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(JSONObject message){
        String topic = "acessoAndroid";

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.toString().getBytes());

        try {
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(){
        String topic = "retornoAndroid";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    visibleActivity.showMessage("WebSocket offLine!");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private String decodificaJson(String response){
        JSONObject json = null;
        int status = 0;
        String message = "";


        try {
            json = new JSONObject(response);
            status = json.getInt("STATUS");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(json != null){
            switch (status){
                case ERRO_USUARIO_INEXISTENTE:
                    message = "Usuário não cadastrado!";
                    break;
                case ERRO_RFID_INVALIDO:
                    message = "RFID inválido!";
                    break;
                case ERRO_USUARIO_JA_CADASTRADO:
                    message = "Usuário já cadastrado!";
                    break;
                case ERRO_RFID_VINCULADO:
                    message = "RFID já vinculado a um usuário!";
                    break;
                case SUCESSO_CADASTRO:
                    message = "Cadastro realizado com sucesso!";
                    break;
                case SUCESSO_SALDO:
                    try {
                        message = json.getString("SALDO");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SUCESSO_RECARGA:
                    message = "Recarga realizada com sucesso!";
            }
        }
        return message;
    }

    public void setVisibleActivity(ScreenConnectedI visibleActivity) {
        this.visibleActivity = visibleActivity;
    }
}