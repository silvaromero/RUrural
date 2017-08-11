package com.example.clientru.clientru;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
