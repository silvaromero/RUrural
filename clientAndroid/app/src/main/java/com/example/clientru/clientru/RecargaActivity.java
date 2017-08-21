package com.example.clientru.clientru;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class RecargaActivity extends AppCompatActivity implements ScreenConnectedI {
    private ConnectionManager connectionManager;
    private EditText valorEdtText;
    private EditText recargaCpfEdtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga);

        getSupportActionBar().setTitle("Recarga");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.connectionManager = new ConnectionManager(RecargaActivity.this);
        this.connectionManager.setVisibleActivity(this);
        this.connectionManager.connectMQTT();

        valorEdtText = (EditText) findViewById(R.id.valorEdtText);
        recargaCpfEdtText = (EditText) findViewById(R.id.recargaCpfEdtText);

    }

    private boolean validaFormulario(){
        boolean retorno = false;
        if(valorEdtText.getText().toString().equals("")){
            valorEdtText.setError("Campo obrigatório!");
        }
        else if(recargaCpfEdtText.getText().toString().equals("")){
            recargaCpfEdtText.setError("Campo obrigatório!");
        }
        else{

            JSONObject json = new JSONObject();
            try {
                json.put("OP", "RECARGA");
                json.put("CPF",recargaCpfEdtText.getText().toString());
                json.put("VALOR", Float.parseFloat(valorEdtText.getText().toString()));
            } catch (JSONException e) {  e.printStackTrace();  }

            this.connectionManager.publish(json);
            retorno = true;
        }
        return retorno;
    }


    public void clickBtn(View view){
        if(view.getId() == R.id.recarregarBtn){
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
        Toast.makeText(RecargaActivity.this, message, Toast.LENGTH_LONG).show();
    }
}