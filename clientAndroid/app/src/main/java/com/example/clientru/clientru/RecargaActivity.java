package com.example.clientru.clientru;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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


public class RecargaActivity extends AppCompatActivity {
    MqttAndroidClient client;
    MqttConnectOptions options;
    String textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga);

        getSupportActionBar().setTitle("Recarga");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://m11.cloudmqtt.com:12871",
                clientId);

        options = new MqttConnectOptions();
        options.setUserName("igor");
        options.setPassword("123".toCharArray());

        connectMQTT();

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {    }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                textMessage = new String(message.getPayload());
                Toast.makeText(RecargaActivity.this, textMessage, Toast.LENGTH_LONG).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {    }
        });
    }

    public void publish(){
        EditText valorEdtText = (EditText)findViewById(R.id.valorEdtText);
        EditText recargaCpfEdtText = (EditText)findViewById(R.id.recargaCpfEdtText);

        String valor = valorEdtText.getText().toString();
        String cpf = recargaCpfEdtText.getText().toString();

        String topic = "acesso";
        String message = String.format("Recarga para cpf: %s, valor: R$%s", cpf, valor);

        try {
            client.publish(topic, message.getBytes(), 0, false);
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

                    Toast.makeText(RecargaActivity.this, "Preparando sua recarga. . . ", Toast.LENGTH_LONG).show();
                    try{
                        subscribe();
                    }catch (Exception e){
                        Toast.makeText(RecargaActivity.this, "WebSocket offLine", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(RecargaActivity.this, "Falha na conexao.", Toast.LENGTH_LONG).show();
                }
            });
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
                    Toast.makeText(RecargaActivity.this, "Inscrito", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(RecargaActivity.this, "Não inscrito", Toast.LENGTH_LONG).show();
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
        if(view.getId() == R.id.recarregarBtn){
            Intent intent = new Intent(this, MainActivity.class);
            publish();
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectMQTT();
    }
}


    