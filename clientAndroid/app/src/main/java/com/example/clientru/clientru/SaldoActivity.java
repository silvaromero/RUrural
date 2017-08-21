package com.example.clientru.clientru;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;



public class SaldoActivity extends AppCompatActivity implements ScreenConnectedI{
    private ConnectionManager connectionManager;
    private EditText saldoCpfEdtText;
    private TextView saldoTxtView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saldo);

        getSupportActionBar().setTitle("Saldo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.connectionManager = new ConnectionManager(SaldoActivity.this);
        this.connectionManager.setVisibleActivity(this);
        this.connectionManager.connectMQTT();

        saldoCpfEdtText = (EditText) findViewById(R.id.saldoCpfEdtText);
        saldoTxtView = (TextView) findViewById(R.id.saldotxtView);

    }

    private boolean validaFormulario(){
        boolean retorno = false;
        if(saldoCpfEdtText.getText().toString().equals("")){
            saldoCpfEdtText.setError("Campo obrigatório!");
        }
        else{

            JSONObject json = new JSONObject();
            try {
                json.put("OP", "SALDO");
                json.put("CPF",saldoCpfEdtText.getText().toString());
            } catch (JSONException e) {  e.printStackTrace();  }

            this.connectionManager.publish(json);
            retorno = true;
        }
        return retorno;
    }

    public void mostraSaldo(String saldo){
        saldoTxtView.setText(saldo);
    }

    public void clickBtn(View view){
        if(view.getId() == R.id.consultaSaldoBtn){
            validaFormulario();
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
        this.connectionManager.disconnectMQTT();
    }

    @Override
    public void showMessage(String message) {
        if(message.length() < 20){
            mostraSaldo(message);
            Toast.makeText(SaldoActivity.this, "Consulta realizada com sucesso!", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(SaldoActivity.this, message, Toast.LENGTH_LONG).show();
    }
}