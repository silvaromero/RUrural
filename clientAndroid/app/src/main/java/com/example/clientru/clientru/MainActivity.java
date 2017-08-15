package com.example.clientru.clientru;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient client;
    MqttConnectOptions options;
    String textMessage;

    /**TODO
     *
     * Organizar pacotes e separar classes de acordo com suas devidas responsabilidades
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://m13.cloudmqtt.com:13988",
                        clientId);

        options = new MqttConnectOptions();
        options.setUserName("romero");
        options.setPassword("123".toCharArray());

        connectMQTT();

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {    }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                textMessage = new String(message.getPayload());
                Toast.makeText(MainActivity.this, textMessage, Toast.LENGTH_LONG).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {    }
        });

    }


    public void publish(){
        String topic = "acesso";
        String message = "Publicando na fila...!";
        try {

            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(){
        String topic = "retorno";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Inscrito", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(MainActivity.this, "Não inscrito", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void connectMQTT(){
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Toast.makeText(MainActivity.this, "Conectou", Toast.LENGTH_LONG).show();
                    try{
                        subscribe();
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "WebSocket offLine", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Falhou", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
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

    public void clickBtn(View view){
        if(view.getId() == R.id.cadastroABtn){
            Intent intent = new Intent(this, CadastroActivity.class);
            startActivity(intent);
        }
        if(view.getId() == R.id.saldoABtn){
            Intent intent = new Intent(this, SaldoActivity.class);
            startActivity(intent);
        }

        if(view.getId() == R.id.recargaABtn){
            Intent intent = new Intent(this, RecargaActivity.class);
            startActivity(intent);
        }
        if(view.getId() == R.id.button){
            publish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectMQTT();
    }
}
