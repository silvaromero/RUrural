package com.example.clientru.clientru;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class CadastroActivity extends AppCompatActivity {
    MqttAndroidClient client;
    MqttConnectOptions options;
    String textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        getSupportActionBar().setTitle("Cadastro");
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
                Toast.makeText(CadastroActivity.this, textMessage, Toast.LENGTH_LONG).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {    }
        });

//        final Button salvarBtn = (Button)findViewById(R.id.salvarBtn);
//        salvarBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                publish();
//            }
//        });
    }

    public void publish(){
        EditText nomeEdtText = (EditText)findViewById(R.id.nomeEdtText);
        EditText cadastroCpfEdtText = (EditText)findViewById(R.id.cadastroCpfEdtText);
        EditText cartaoEdtText = (EditText)findViewById(R.id.cartaoEdtText);

        String nome = nomeEdtText.getText().toString();
        String cpf = cadastroCpfEdtText.getText().toString();
        String rfid = cartaoEdtText.getText().toString();

        String topic = "acesso";
        String message = String.format("Usuario %s , cpf: %s, cartao: %s, cadastrado com sucesso!", nome, cpf, rfid);

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

                    Toast.makeText(CadastroActivity.this, "Preparando seu cadastro. . . ", Toast.LENGTH_LONG).show();
                    try{
                        subscribe();
                    }catch (Exception e){
                        Toast.makeText(CadastroActivity.this, "WebSocket offLine", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(CadastroActivity.this, "Falha na conexao.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(CadastroActivity.this, "Inscrito", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(CadastroActivity.this, "Não inscrito", Toast.LENGTH_LONG).show();
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
        if(view.getId() == R.id.salvarBtn){
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
