package com.example.clientru.clientru;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CadastroActivity extends AppCompatActivity implements ScreenConnectedI{
    private ConnectionManager connectionManager;
    private EditText nomeEdtText;
    private EditText cpfEdtText;
    private EditText cartaoEdtText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        getSupportActionBar().setTitle("Cadastro");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.connectionManager = new ConnectionManager(CadastroActivity.this);
        this.connectionManager.setVisibleActivity(this);
        this.connectionManager.connectMQTT();

        nomeEdtText = (EditText) findViewById(R.id.nomeEdtText);
        cpfEdtText = (EditText) findViewById(R.id.cadastroCpfEdtText);
        cartaoEdtText = (EditText) findViewById(R.id.cartaoEdtText);
    }

    private boolean validaFormulario(){
            boolean retorno = false;
            if(nomeEdtText.getText().toString().equals("")){
                nomeEdtText.setError("Campo obrigatório!");
            }
            else if(cpfEdtText.getText().toString().equals("")){
                cpfEdtText.setError("Campo obrigatório!");
            }
            else if(cartaoEdtText.getText().toString().equals("")){
                cartaoEdtText.setError("Campo obrigatório!");
            }
            else{

                JSONObject json = new JSONObject();
                try {
                    json.put("OP", "CADASTRO");
                    json.put("NOME", nomeEdtText.getText().toString());
                    json.put("CPF",cpfEdtText.getText().toString());
                    json.put("RFID",cartaoEdtText.getText().toString());

                } catch (JSONException e) {  e.printStackTrace();  }

                this.connectionManager.publish(json);
                retorno = true;
            }
            return retorno;
        }


    public void clickBtn(View view){
        if(view.getId() == R.id.salvarBtn){
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
        Toast.makeText(CadastroActivity.this, message, Toast.LENGTH_LONG).show();
    }
}