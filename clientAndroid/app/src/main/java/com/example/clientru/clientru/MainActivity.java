package com.example.clientru.clientru;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isConnected()){
            Button cadastroABtn = (Button) findViewById(R.id.cadastroABtn);
            cadastroABtn.setEnabled(false);

            Button saldoABtn = (Button) findViewById(R.id.saldoABtn);
            saldoABtn.setEnabled(false);

            Button recargaABtn = (Button) findViewById(R.id.recargaABtn);
            recargaABtn.setEnabled(false);

            Toast.makeText(MainActivity.this, "Conecte-se à internet para ter acesso às funcionalidades!", Toast.LENGTH_LONG).show();
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
    }

    public  boolean isConnected() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        } else {
            connected = false;
        }
        return connected;
    }
}