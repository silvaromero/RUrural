package com.example.clientru.clientru;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity{

//    private ConnectionManager connectionManager;
    /**TODO
     *
     * Organizar pacotes e separar classes de acordo com suas devidas responsabilidades
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try{
//            this.connectionManager = new ConnectionManager(MainActivity.this);
//            this.connectionManager.setVisibleActivity(this);
//            this.connectionManager.connectMQTT();
//        }catch (Exception e){
//            Toast.makeText(MainActivity.this, "Conexão não estabelecida!", Toast.LENGTH_LONG).show();
//
//        }

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
//        if(view.getId() == R.id.button){
//            JSONObject json = new JSONObject();
//
//            try {
//                json.put("RFID", "123123");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            this.connectionManager.publish(json);
//        }
    }

//    @Override
//    public void showMessage(String message) {
//        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        this.connectionManager.disconnectMQTT();
    }
}